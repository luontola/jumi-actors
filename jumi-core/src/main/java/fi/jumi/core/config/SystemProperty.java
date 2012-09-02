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

    public SystemProperty(String beanProperty, String systemProperty, DaemonConfiguration defaults) {
        this.beanProperty = beanProperty;
        this.systemProperty = systemProperty;
        this.defaults = defaults;
    }

    public void toSystemProperty(DaemonConfiguration config, Map<String, String> map) {
        try {
            // TODO: remove duplication
            Method get = defaults.getClass().getMethod(beanProperty);

            Object value = get.invoke(config);
            Object defaultValue = get.invoke(defaults);
            if (!value.equals(defaultValue)) {
                map.put(systemProperty, String.valueOf(value));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void parseSystemProperty(DaemonConfigurationBuilder builder, Properties systemProperties) {
        try {
            // TODO: remove duplication
            Method get = builder.getClass().getMethod(beanProperty);
            Class<?> type = get.getReturnType();
            Method set = builder.getClass().getMethod(beanProperty, type);

            String value = systemProperties.getProperty(systemProperty);
            if (value != null) {
                set.invoke(builder, parse(type, value));
            }
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
