package com.github.kostrovik.iceguard.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kostrovik.iceguard.converters.TokenConverter;
import com.github.kostrovik.iceguard.converters.UserRoleConverter;
import com.github.kostrovik.iceguard.dictionaries.AuthEventType;
import com.github.kostrovik.iceguard.exceptions.HttpRequestException;
import com.github.kostrovik.iceguard.interfaces.AuthServiceInterface;
import com.github.kostrovik.iceguard.interfaces.ServerSettingsInterface;
import com.github.kostrovik.iceguard.models.CurrentUser;
import com.github.kostrovik.iceguard.models.Token;
import com.github.kostrovik.iceguard.utils.ConnectionUtil;
import com.github.kostrovik.useful.models.AbstractObservable;
import com.github.kostrovik.useful.utils.InstanceLocatorUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
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
    private static final String ERROR_PARSE_JSON_MESSAGE = "Ошибка чтения JSON строки.";

    private Preferences preferences;

    /**
     * Общий объект блокировки для операций чтения/записи пользовательских данных при сохранении авторизации.
     */
    private final Object lock = new Object();
    private volatile ServerSettingsInterface settings;

    private String detailsAttribute;
    private Charset charset;
    private ConnectionUtil util;
    private HttpClient client;

    private AuthService() {
        this.preferences = Preferences.userRoot().node("auth/login");
        this.detailsAttribute = "";
        this.charset = Charset.forName("UTF-8");
        this.util = new ConnectionUtil();
        this.client = HttpClient.newBuilder().build();
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
    public synchronized void authenticateUser(String login, String password) {
        Map<String, String> data = new HashMap<>();
        data.put("login", login);
        data.put("password", password);

        try {
            String json = new ObjectMapper().writeValueAsString(data);

            Map<String, String> headers = getHeaders();
            headers.remove("Authorization");

            HttpResponse<String> answer = sendRequest(getServerSettings().getTokenApi(), "post", headers, json, new HashMap<>());
            if (answer.statusCode() == HttpURLConnection.HTTP_OK) {
                Token token = new TokenConverter().fromMap((Map) parseAnswer(answer));
                currentUser = new CurrentUser(login, password, token);
                prepareUserRoles();
            }
        } catch (JsonProcessingException error) {
            logger.log(Level.WARNING, ERROR_CREATE_JSON_MESSAGE, error);
            notifyListeners(AuthEventType.AUTHENTICATION_ERROR);
        } catch (HttpRequestException e) {
            logger.log(Level.WARNING, "Ошибка аутентификации пользователя.", e);
            notifyListeners(AuthEventType.AUTHENTICATION_ERROR);
        }
    }

    private void prepareUserRoles() {
        Map<String, String> headers = getHeaders();
        try {
            HttpResponse<String> answer = sendRequest(getServerSettings().getUserRolesApi(), "post", headers, "", new HashMap<>());
            if (answer.statusCode() == HttpURLConnection.HTTP_OK) {
                List<Map> rolesList = (List<Map>) ((Map) parseAnswer(answer)).get("items");
                UserRoleConverter converter = new UserRoleConverter();

                rolesList.forEach(roleMap -> currentUser.addRole(converter.fromMap(roleMap)));
                notifyListeners(AuthEventType.AUTHENTICATED);
            } else {
                logout();
            }
        } catch (HttpRequestException e) {
            logger.log(Level.WARNING, "Ошибка получения ролей пользователя.", e);
            notifyListeners(AuthEventType.AUTHENTICATION_ERROR);
        }
    }

    @Override
    public Object sendGet(String apiUrl, Map<String, List<String>> urlParams) {
        try {
            return repeater(apiUrl, "get", "", urlParams);
        } catch (HttpRequestException e) {
            logger.log(Level.WARNING, "Ошибка отправки запроса.", e);
            throw e;
        }
    }

    @Override
    public Object sendPost(String apiUrl, String json) {
        return sendPost(apiUrl, json, new HashMap<>());
    }

    @Override
    public Object sendPost(String apiUrl, String json, Map<String, List<String>> urlParams) {
        try {
            return repeater(apiUrl, "post", json, urlParams);
        } catch (HttpRequestException e) {
            logger.log(Level.WARNING, "Ошибка отправки запроса.", e);
            throw e;
        }
    }

    private Object repeater(String apiUrl, String method, String json, Map<String, List<String>> urlParams) {
        HttpResponse<String> answer = sendRequest(apiUrl, method, getHeaders(), json, urlParams);
        if (answer.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            refreshToken();
            return parseAnswer(sendRequest(apiUrl, "post", getHeaders(), json, urlParams));
        }
        return parseAnswer(answer);
    }

    @Override
    public void logout() {
        synchronized (lock) {
            currentUser = null;
            notifyListeners(AuthEventType.LOGOUT);
        }
    }

    private void refreshToken() {
        Map<String, String> headers = getHeaders();
        headers.remove("Authorization");
        headers.put("refresh-token", currentUser.getRefreshToken());

        try {
            HttpResponse<String> answer = sendRequest(getServerSettings().getRefreshTokenApi(), "post", headers, "", new HashMap<>());
            if (answer.statusCode() == HttpURLConnection.HTTP_OK) {
                Token token = new TokenConverter().fromMap((Map) parseAnswer(answer));
                currentUser.setToken(token);
            } else {
                logger.info("Ошибка обновления токена.");
                notifyListeners(AuthEventType.REFRESH_TOKEN_ERROR);
            }
        } catch (HttpRequestException e) {
            logger.log(Level.WARNING, "Ошибка обновления токена.", e);
            notifyListeners(AuthEventType.REFRESH_TOKEN_ERROR);
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
            if (Objects.isNull(currentUser)) {
                currentUser = new CurrentUser("", "", new Token("", preferences.get("token", "")));
            } else {
                currentUser.setToken(new Token("", preferences.get("token", "")));
            }
            refreshToken();
            prepareUserRoles();
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

    private HttpResponse<String> sendRequest(String apiUrl, String method, Map<String, String> headers, String json, Map<String, List<String>> urlParams) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder.uri(util.buildUri(apiUrl, urlParams, charset));
        if (Objects.nonNull(method) && method.equalsIgnoreCase("post")) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(json));
        } else {
            requestBuilder.GET();
        }
        headers.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString(charset));
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Запрос прерван.", e);
            throw new HttpRequestException(e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка чтения данных.", e);
            throw new HttpRequestException(e);
        }
    }

    private synchronized ServerSettingsInterface getServerSettings() {
        if (Objects.isNull(settings)) {
            settings = ServiceLoader.load(ModuleLayer.boot(), ServerSettingsInterface.class).findFirst().orElse(null);
        }
        return settings;
    }

    private Object parseAnswer(HttpResponse<String> response) {
        try {
            Map<String, Object> details = new ObjectMapper().readValue(response.body(), new TypeReference<Map<String, Object>>() {
            });
            return details.get(detailsAttribute);
        } catch (IOException error) {
            logger.log(Level.WARNING, ERROR_PARSE_JSON_MESSAGE, error);
            throw new HttpRequestException(error);
        }
    }
}