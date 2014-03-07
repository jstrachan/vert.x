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
package org.vertx.java.spi.cluster.impl.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * A factory to create a ZK client connection
 */
public class ZKClientFactory {
    private static final Logger log = LoggerFactory.getLogger(ZKClientFactory.class);

    private String connectString = "localhost:2181";
    private String password;
    private int timeout = 30000;

    public CuratorFramework createCurator() throws UnsupportedEncodingException {
         CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                 .connectString(connectString)
                 .retryPolicy(new ExponentialBackoffRetry(5, 10))
                 .connectionTimeoutMs(timeout);
         if (password != null && !password.isEmpty()) {
             builder.authorization("digest", ("zookeeper:" + password).getBytes("UTF-8"));
         }
         CuratorFramework curator = builder.build();
         log.debug("Starting curator " + curator);
         curator.start();
         return curator;
     }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
