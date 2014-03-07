/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package org.vertx.java.spi.cluster.impl.zookeeper.util;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * A Helper class for injecting configuration on a bean from a properties file
 */
public class ConfigInjection {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigInjection.class);

    /**
     * Applies configuration specified in {@link java.util.Map} to the specified target.
     *
     * @param configuration The configuration.
     * @param target        The target.
     * @param <T>
     * @throws Exception
     */
    public static <T> void applyConfiguration(Map configuration, T target) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            applyConfiguration(configuration, target, clazz);
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Applies configuration specified in {@link java.util.Map} to the specified target.
     *
     * @param configuration The configuration.
     * @param target        The target.
     * @param clazz         The target Class.
     * @param <T>
     * @throws Exception
     */
    private static <T> void applyConfiguration(Map configuration, T target, Class<?> clazz) throws Exception {
        injectValues(clazz, target, configuration);

    }

    static void injectValues(Class<?> clazz, Object instance, Map configuration) throws Exception {
        Set<Map.Entry> entries = configuration.entrySet();
        for (Map.Entry entry : entries) {
            Object keyValue = entry.getKey();
            if (keyValue != null) {
                String name = keyValue.toString();
                Object value = entry.getValue();
                try {
                    Field field = clazz.getDeclaredField(normalizePropertyName(name));
                    if (field != null) {
                        Object convertedValue = ConverterHelper.convertValue(value, field.getGenericType());
                        if (convertedValue != null) {
                            ReflectionHelper.setField(field, instance, convertedValue);
                        }
                    }
                } catch (NoSuchFieldException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No matching field for property with name " + name);
                    }
                }
            }
        }
    }


    /**
     * Utility to transform name containing dots to valid java identifiers.
     *
     * @param name
     * @return
     */
    static String normalizePropertyName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        } else if (!name.contains(".") && !name.contains("-")) {
            return name;
        } else {
            String[] parts = name.replaceAll(" ", "").split("-|\\.");
            StringBuilder sb = new StringBuilder();
            if (parts.length > 0) {
                sb.append(parts[0]);
                for (int i = 1; i < parts.length; i++) {
                    String s = parts[i].length() > 0 ? parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1) : "";
                    sb.append(s);
                }
            }
            return sb.toString();
        }
    }
}
