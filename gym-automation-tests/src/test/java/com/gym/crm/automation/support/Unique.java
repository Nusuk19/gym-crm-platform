package com.gym.crm.automation.support;

import java.util.concurrent.atomic.AtomicLong;

public final class Unique {

    private static final AtomicLong COUNTER = new AtomicLong();

    private Unique() {
    }

    public static String name(String prefix) {
        return prefix + COUNTER.incrementAndGet();
    }

    public static String digits() {
        return String.valueOf(COUNTER.incrementAndGet());
    }
}
