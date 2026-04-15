package com.aliyun.dypnsapi20170525.external.org.slf4j.impl;

import com.aliyun.dypnsapi20170525.external.org.slf4j.ILoggerFactory;
import com.aliyun.dypnsapi20170525.external.org.slf4j.helpers.NOPLoggerFactory;

/**
 * Provide a minimal SLF4J 1.7-style binder for Aliyun's shaded SLF4J classes
 * so they do not emit StaticLoggerBinder warnings at runtime.
 */
public final class StaticLoggerBinder {

    public static String REQUESTED_API_VERSION = "1.7.36";

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    private final ILoggerFactory loggerFactory = new NOPLoggerFactory();

    private StaticLoggerBinder() {
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public String getLoggerFactoryClassStr() {
        return NOPLoggerFactory.class.getName();
    }
}
