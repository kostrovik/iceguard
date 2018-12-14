package com.github.kostrovik.iceguard.interfaces;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-14
 * github:  https://github.com/kostrovik/iceguard
 */
public interface ServerSettingsInterface {
    String getHostUrl();

    String getTokenApi();

    String getRefreshTokenApi();

    String getUserRolesApi();
}
