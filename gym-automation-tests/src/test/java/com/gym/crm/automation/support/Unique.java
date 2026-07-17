package com.gym.crm.automation.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Unique {

    private static final AtomicLong COUNTER = new AtomicLong(System.currentTimeMillis());

    public static String name(String prefix) {
        return prefix + COUNTER.incrementAndGet();
    }

    public static String digits() {
        return String.valueOf(COUNTER.incrementAndGet());
    }

    public static String username(String firstNamePart, String lastNamePart) {
        return firstNamePart + "." + lastNamePart + COUNTER.incrementAndGet();
    }
}
