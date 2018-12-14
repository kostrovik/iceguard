package com.github.kostrovik.iceguard.models;

import java.util.Objects;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class Account {
    private String login;
    private String password;

    public Account(String login, String password) {
        Objects.requireNonNull(login);
        this.login = login;
        Objects.requireNonNull(password);
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return login.equals(account.login) &&
                password.equals(account.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password);
    }
}
