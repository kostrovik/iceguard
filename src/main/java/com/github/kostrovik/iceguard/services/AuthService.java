package com.github.kostrovik.iceguard.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kostrovik.http.client.common.HttpClient;
import com.github.kostrovik.http.client.common.HttpRequest;
import com.github.kostrovik.http.client.common.HttpResponse;
import com.github.kostrovik.iceguard.converters.TokenConverter;
import com.github.kostrovik.iceguard.converters.UserRoleConverter;
import com.github.kostrovik.iceguard.exceptions.HttpRequestException;
import com.github.kostrovik.iceguard.interfaces.AuthServiceInterface;
import com.github.kostrovik.iceguard.interfaces.ServerSettingsInterface;
import com.github.kostrovik.iceguard.models.CurrentUser;
import com.github.kostrovik.iceguard.models.Token;
import com.github.kostrovik.useful.models.AbstractObservable;
import com.github.kostrovik.useful.utils.InstanceLocatorUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class AuthService extends AbstractObservable implements AuthServiceInterface {
    private Logger logger = InstanceLocatorUtil.getLocator().getLogger(AuthService.class);
    private CurrentUser currentUser;
    private static volatile AuthService service;

    private static final String ERROR_CREATE_JSON_MESSAGE = "Не возможно создать JSON.";

    private Preferences preferences;

    /**
     * Общий объект блокировки для операций чтения/записи пользовательских данных при сохранении авторизации.
     */
    private Object lock = new Object();
    private volatile ServerSettingsInterface settings;
    private String detailsAttribute;

    private AuthService() {
        this.preferences = Preferences.userRoot().node("auth/login");
    }

    public static AuthService provider() {
        return getInstance();
    }

    public static synchronized AuthService getInstance() {
        if (Objects.isNull(service)) {
            service = new AuthService();
        }
        return service;
    }

    @Override
    public void setResponseDetailsAttribute(String attribute) {
        this.detailsAttribute = Objects.requireNonNullElse(attribute, "");
    }

    @Override
    public synchronized HttpResponse authenticateUser(String login, String password) {
        Map<String, String> data = new HashMap<>();
        data.put("login", login);
        data.put("password", password);

        try {
            String json = new ObjectMapper().writeValueAsString(data);

            Map<String, String> headers = getHeaders();
            headers.remove("Authorization");

            HttpResponse answer = sendRequest(getServerSettings().getTokenApi(), "post", headers, json, new HashMap<>());
            if (Objects.nonNull(answer) && answer.getStatus() == HttpURLConnection.HTTP_OK) {
                TokenConverter converter = new TokenConverter();
                Token token = converter.fromMap((Map) answer.getDetails());
                currentUser = new CurrentUser(login, password, token);
                prepareCurrentUser();
            }

            return answer;
        } catch (JsonProcessingException error) {
            logger.log(Level.WARNING, ERROR_CREATE_JSON_MESSAGE, error);
            throw new HttpRequestException(error);
        }
    }

    private void prepareCurrentUser() {
        Map<String, String> headers = getHeaders();

        HttpResponse answer = sendRequest(getServerSettings().getUserRolesApi(), "post", headers, "", new HashMap<>());
        if (Objects.nonNull(answer) && answer.getStatus() == HttpURLConnection.HTTP_OK) {
            List<Map> rolesList = (List<Map>) ((Map) answer.getDetails()).get("items");
            UserRoleConverter converter = new UserRoleConverter();

            rolesList.forEach(roleMap -> currentUser.addRole(converter.fromMap(roleMap)));
        } else {
            logout();
        }
    }

    @Override
    public HttpResponse sendGet(String apiUrl, Map<String, List<String>> urlParams) {
        HttpResponse answer = sendRequest(apiUrl, "get", getHeaders(), "", urlParams);
        if (Objects.isNull(answer) || answer.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            refreshToken();
            return sendRequest(apiUrl, "get", getHeaders(), "", urlParams);
        }
        return answer;
    }

    @Override
    public HttpResponse sendPost(String apiUrl, String json) {
        return sendPost(apiUrl, json, new HashMap<>());
    }

    @Override
    public HttpResponse sendPost(String apiUrl, String json, Map<String, List<String>> urlParams) {
        HttpResponse answer = sendRequest(apiUrl, "post", getHeaders(), json, urlParams);
        if (Objects.isNull(answer) || answer.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            refreshToken();
            return sendRequest(apiUrl, "post", getHeaders(), json, urlParams);
        }
        return answer;
    }

    @Override
    public void logout() {
        synchronized (lock) {
            if (currentUser != null) {
                currentUser = null;
                notifyLlisteners("logout");
            }
        }
    }

    private void refreshToken() {
        Map<String, String> headers = getHeaders();
        headers.remove("Authorization");
        headers.put("refresh-token", currentUser.getRefreshToken());

        HttpResponse answer = sendRequest(getServerSettings().getRefreshTokenApi(), "post", headers, "", new HashMap<>());
        if (Objects.nonNull(answer) && answer.getStatus() == HttpURLConnection.HTTP_OK) {
            TokenConverter converter = new TokenConverter();
            Token token = converter.fromMap((Map) answer.getDetails());
            currentUser.setToken(token);
        } else {
            notifyLlisteners(answer);
        }
    }

    @Override
    public void saveUserCredentials() {
        synchronized (lock) {
            preferences.put("token", currentUser.getRefreshToken());
        }
    }

    @Override
    public void useSavedCredentials() {
        synchronized (lock) {
            currentUser.setToken(new Token("", preferences.get("token", "")));
        }
    }

    @Override
    public void clearSavedCredentials() {
        synchronized (lock) {
            preferences.remove("token");
        }
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Accept", "application/json");
        if (Objects.nonNull(currentUser)) {
            headers.put("Authorization", "Bearer " + currentUser.getAccessToken());
        }

        return headers;
    }

    private HttpResponse sendRequest(String apiUrl, String method, Map<String, String> headers, String json, Map<String, List<String>> urlParams) {
        HttpRequest request = new HttpRequest(getClient());
        if (Objects.nonNull(method) && method.equalsIgnoreCase("post")) {
            request.POST(apiUrl);
        } else {
            request.GET(apiUrl);
        }

        try {
            return request.setData(json).setHeaders(headers).setQueryParams(urlParams).build().getResponse();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    private HttpClient getClient() {
        HttpClient client = new HttpClient(getServerSettings().getHostUrl());
        client.setAnswerDetailsAttribute(detailsAttribute);
        return client;
    }

    private synchronized ServerSettingsInterface getServerSettings() {
        if (Objects.isNull(settings)) {
            settings = ServiceLoader.load(ModuleLayer.boot(), ServerSettingsInterface.class).findFirst().orElse(null);
        }
        return settings;
    }
}