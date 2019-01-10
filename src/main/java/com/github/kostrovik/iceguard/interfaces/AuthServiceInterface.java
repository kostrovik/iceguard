package com.github.kostrovik.iceguard.interfaces;

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
    void authenticateUser(String login, String password);

    Object sendGet(String apiUrl, Map<String, List<String>> urlParams);

    Object sendPost(String apiUrl, String json);

    Object sendPost(String apiUrl, String json, Map<String, List<String>> urlParams);

    void logout();

    void saveUserCredentials();

    void useSavedCredentials();

    void clearSavedCredentials();

    void setResponseDetailsAttribute(String attribute);
}
