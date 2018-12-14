package com.github.kostrovik.iceguard.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class CurrentUser {
    private Account account;
    private Token token;
    private List<UserRole> roles;

    public CurrentUser(String login, String password, Token token) {
        Objects.requireNonNull(login);
        this.account = new Account(login, Objects.requireNonNullElse(password, ""));
        Objects.requireNonNull(token);
        this.token = token;

        this.roles = new ArrayList<>();
    }

    public CurrentUser(String login, String password, Token token, List<UserRole> roles) {
        this(login, password, token);

        if (Objects.nonNull(roles)) {
            this.roles.addAll(roles);
        }
    }

    public String getLogin() {
        return account.getLogin();
    }

    public String getPassword() {
        return account.getPassword();
    }

    public String getAccessToken() {
        return token.getAccess();
    }

    public String getRefreshToken() {
        return token.getRefresh();
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<UserRole> getRoles() {
        return new ArrayList<>(roles);
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    public boolean hasPermission(Permission permission) {
        return roles.stream().anyMatch(userRole -> userRole.hasPermission(permission));
    }
}
