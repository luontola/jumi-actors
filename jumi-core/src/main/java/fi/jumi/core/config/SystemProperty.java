// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import java.lang.reflect.Method;
import java.util.*;

public class SystemProperty {

    private final String beanProperty;
    private final String systemProperty;
    private final DaemonConfiguration defaults;
    private final Method getter;
    private final Class<?> type;

    public SystemProperty(String beanProperty, String systemProperty, DaemonConfiguration defaults) {
        this.beanProperty = beanProperty;
        this.systemProperty = systemProperty;
        this.defaults = defaults;
        try {
            getter = defaults.getClass().getMethod(beanProperty);
            type = getter.getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void toSystemProperty(DaemonConfiguration source, Map<String, String> target) {
        Object value = get(source);
        Object defaultValue = get(defaults);

        if (!value.equals(defaultValue)) {
            target.put(systemProperty, String.valueOf(value));
        }
    }

    public void parseSystemProperty(DaemonConfigurationBuilder target, Properties source) {
        String value = source.getProperty(systemProperty);
        if (value != null) {
            set(target, value);
        }
    }

    private Object get(DaemonConfiguration source) {
        try {
            return getter.invoke(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void set(DaemonConfigurationBuilder target, String value) {
        try {
            Method setter = target.getClass().getMethod(beanProperty, type);
            setter.invoke(target, parse(type, value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parse(Class<?> type, String value) {
        if (type == long.class) {
            return Long.parseLong(value);
        }
        if (type == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }
}
