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
package org.vertx.java.spi.cluster.impl.zookeeper;

import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.ClusterManager;
import org.vertx.java.core.spi.cluster.ClusterManagerFactory;

/**
 * A <a href="http://zookeeper.apache.org/">Apache ZooKeeper</a>
 * implementation of the {@link org.vertx.java.core.spi.cluster.ClusterManagerFactory}
 *
 * @author <a href="https://twitter.com/jstrachan">James Strachan</a>
 */
public class ZooKeeperClusterManagerFactory implements ClusterManagerFactory {
    @Override
    public ClusterManager createClusterManager(VertxSPI vertx) {
        return new ZooKeeperClusterManager(vertx);
    }
}
