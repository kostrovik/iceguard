package com.github.kostrovik.iceguard.interfaces;

import com.github.kostrovik.http.client.common.HttpResponse;
import com.github.kostrovik.useful.interfaces.Observable;

import java.util.List;
import java.util.Map;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public interface AuthServiceInterface extends Observable {
    HttpResponse authenticateUser(String login, String password);

    HttpResponse sendGet(String apiUrl, Map<String, List<String>> urlParams);

    HttpResponse sendPost(String apiUrl, String json);

    HttpResponse sendPost(String apiUrl, String json, Map<String, List<String>> urlParams);

    void logout();

    void saveUserCredentials();

    void useSavedCredentials();

    void clearSavedCredentials();

    void setResponseDetailsAttribute(String attribute);
}
