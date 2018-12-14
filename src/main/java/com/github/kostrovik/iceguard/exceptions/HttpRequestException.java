package com.github.kostrovik.iceguard.exceptions;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-14
 * github:  https://github.com/kostrovik/iceguard
 */
public class HttpRequestException extends RuntimeException {
    public HttpRequestException() {
    }

    public HttpRequestException(String message) {
        super(message);
    }

    public HttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpRequestException(Throwable cause) {
        super(cause);
    }
}
