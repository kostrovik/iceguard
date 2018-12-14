package com.github.kostrovik.iceguard.models;

import java.util.Objects;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class Token {
    private String access;
    private String refresh;

    public Token(String access, String refresh) {
        Objects.requireNonNull(access);
        this.access = access;
        Objects.requireNonNull(refresh);
        this.refresh = refresh;
    }

    public String getAccess() {
        return access;
    }

    public String getRefresh() {
        return refresh;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return access.equals(token.access) &&
                refresh.equals(token.refresh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(access, refresh);
    }
}
