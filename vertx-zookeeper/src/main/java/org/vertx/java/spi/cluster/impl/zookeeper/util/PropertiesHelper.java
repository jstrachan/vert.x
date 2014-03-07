/*
 * Copyright (c) 2011-2014 The original author or authors
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
import org.vertx.java.spi.cluster.impl.zookeeper.ZooKeeperClusterManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 */
public class PropertiesHelper {
    private static final Logger log = LoggerFactory.getLogger(PropertiesHelper.class);

    /**
     * Get the properties file from the classpath
     */
    public static Properties getResourceProperties(String resourceName) {
        Properties answer = new Properties();
        InputStream is = getResourceStream(resourceName);
        try {
            if (is != null) {
                answer.load(is);
            }
        } catch (IOException ex) {
            log.error("Failed to read config", ex);
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception e) {
                // ignore
            }
        }
        return answer;
    }

    private static InputStream getResourceStream(String resourceName) {
        ClassLoader ctxClsLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = null;
        if (ctxClsLoader != null) {
            is = ctxClsLoader.getResourceAsStream(resourceName);
        }
        if (is == null) {
            is = PropertiesHelper.class.getClassLoader().getResourceAsStream(resourceName);
        }
        if (is == null) {
            log.error("Could not find configuration file " + resourceName);
        }
        return is;
    }


}
