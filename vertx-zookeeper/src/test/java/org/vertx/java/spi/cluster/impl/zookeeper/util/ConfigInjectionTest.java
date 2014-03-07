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

import org.junit.Test;
import org.vertx.java.spi.cluster.impl.zookeeper.ZKClientFactory;
import org.vertx.java.spi.cluster.impl.zookeeper.ZooKeeperClusterManager;

import java.util.Map;
import java.util.Properties;

import static org.vertx.testtools.VertxAssert.assertEquals;

/**
 * @author <a href="https://twitter.com/jstrachan">James Strachan</a>
 */
public class ConfigInjectionTest {

    @Test
    public void testInjection() throws Exception {
        Properties properties = PropertiesHelper.getResourceProperties(ZooKeeperClusterManager.CONFIG_FILE);
        Map<String, Object> clientProperties = Maps.getSubMap(properties, "client.");
        ZKClientFactory clientFactoryBean = new ZKClientFactory();
        ConfigInjection.applyConfiguration(clientProperties, clientFactoryBean);

        assertEquals("getConnectString()", "localhost:3181", clientFactoryBean.getConnectString());
        assertEquals("getTimeout()", 20000, clientFactoryBean.getTimeout());
    }

}
