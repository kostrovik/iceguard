import com.github.kostrovik.iceguard.interfaces.ServerSettingsInterface;

module com.github.kostrovik.iceguard {
    requires java.prefs;
    requires java.logging;
    requires com.github.kostrovik.useful.utils;
    requires com.github.kostrovik.http.client;
    requires com.fasterxml.jackson.databind;

    exports com.github.kostrovik.iceguard.models;
    exports com.github.kostrovik.iceguard.interfaces;
    exports com.github.kostrovik.iceguard.exceptions;
    exports com.github.kostrovik.iceguard.services;

    uses com.github.kostrovik.useful.interfaces.LoggerConfigInterface;
    uses ServerSettingsInterface;

    provides com.github.kostrovik.iceguard.interfaces.AuthServiceInterface with com.github.kostrovik.iceguard.services.AuthService;
}