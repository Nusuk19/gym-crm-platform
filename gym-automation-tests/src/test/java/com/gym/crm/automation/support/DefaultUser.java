package com.gym.crm.automation.support;

public final class DefaultUser {

    private static String username;
    private static String password;

    private DefaultUser() {
    }

    public static void set(String username, String password) {
        DefaultUser.username = username;
        DefaultUser.password = password;
    }

    public static String username() {
        requireInitialized();

        return username;
    }

    public static String password() {
        requireInitialized();

        return password;
    }

    private static void requireInitialized() {
        if (username == null || password == null) {
            throw new IllegalStateException(
                    "Default user was not bootstrapped - Hooks.registerDefaultUser() must run before any scenario");
        }
    }
}
