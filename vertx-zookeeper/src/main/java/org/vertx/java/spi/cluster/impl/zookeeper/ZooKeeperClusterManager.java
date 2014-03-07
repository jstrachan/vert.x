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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMap;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ClusterManager;
import org.vertx.java.core.spi.cluster.NodeListener;
import org.vertx.java.spi.cluster.impl.zookeeper.util.ConfigInjection;
import org.vertx.java.spi.cluster.impl.zookeeper.util.Maps;
import org.vertx.java.spi.cluster.impl.zookeeper.util.PropertiesHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A <a href="http://zookeeper.apache.org/">Apache ZooKeeper</a>
 * implementation of the {@link org.vertx.java.core.spi.cluster.ClusterManager}
 *
 * @author <a href="https://twitter.com/jstrachan">James Strachan</a>
 */
public class ZooKeeperClusterManager implements ClusterManager {
    private static final Logger log = LoggerFactory.getLogger(ZooKeeperClusterManager.class);

    public static final String CONFIG_FILE = "zookeeper.properties";
    public static final String NODES_PATH = "/vertx/nodes";

    private static final String NODES_PATH_PREFIX = NODES_PATH + "/";

    private final VertxSPI vertx;
    private NodeListener nodeListener;
    private CuratorFramework curator;
    private ZKServerFactory serverFactory;
    private String nodeId;
    private PathChildrenCache childrenCache;

    public ZooKeeperClusterManager(VertxSPI vertx) {
        this.vertx = vertx;
    }

    @Override
    public void join() {
        try {
            Properties properties = PropertiesHelper.getResourceProperties(CONFIG_FILE);
            if (serverFactory == null) {
                Map<String, Object> serverProperties = Maps.getSubMap(properties, "server.");
                if (serverProperties.size() > 0) {
                    serverFactory = new ZKServerFactory();
                    ConfigInjection.applyConfiguration(serverProperties, serverFactory);
                    serverFactory.init();
                }
            }
            if (curator == null) {
                Map<String, Object> clientProperties = Maps.getSubMap(properties, "client.");
                ZKClientFactory clientFactoryBean = new ZKClientFactory();
                ConfigInjection.applyConfiguration(clientProperties, clientFactoryBean);
                curator = clientFactoryBean.createCurator();
            }
            String path = NODES_PATH;

            String nodePath = curator.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(path + "/0");

            nodeId = toNodeId(nodePath);

            childrenCache = new PathChildrenCache(curator, path, true);
            childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                    ChildData data = event.getData();
                    String id = toNodeId(data.getPath());
                    PathChildrenCacheEvent.Type eventType = event.getType();
                    if (eventType.equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                        nodeAdded(id);
                    } else if (eventType.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                        nodeRemoved(id);
                    }
                }
            });
            childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to join: " + e, e);
        }
    }


    @Override
    public void leave() {
        if (childrenCache != null) {
            try {
                childrenCache.close();
            } catch (IOException e) {
                handleException(e);
            }
        }
        if (curator != null) {
            curator.close();
            curator = null;
        }
        if (serverFactory != null) {
            try {
                serverFactory.destroy();
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    @Override
    public <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(String s) {
        // TODO
        return null;
    }

    @Override
    public <K, V> AsyncMap<K, V> getAsyncMap(String s) {
        // TODO
        return null;
    }

    @Override
    public <K, V> Map<K, V> getSyncMap(String s) {
        // TODO
        return null;
    }

    @Override
    public String getNodeID() {
        return nodeId;
    }

    @Override
    public List<String> getNodes() {
        List<String> answer = new ArrayList<String>();
        List<ChildData> currentData = childrenCache.getCurrentData();
        for (ChildData childData : currentData) {
            String id = toNodeId(childData.getPath());
            answer.add(id);
        }
        return answer;
    }

    @Override
    public void nodeListener(NodeListener nodeListener) {
        this.nodeListener = nodeListener;
    }

    protected void nodeAdded(String id) {
        if (log.isDebugEnabled()) {
            log.info("nodeAdded: " + id);
        }
        if (nodeListener != null) {
            nodeListener.nodeLeft(id);
        }
    }

    protected void nodeRemoved(String id) {
        if (log.isDebugEnabled()) {
            log.info("nodeRemoved: " + id);
        }
        if (nodeListener != null) {
            nodeListener.nodeAdded(id);
        }
    }

    protected String toNodeId(String path) {
        if (path.startsWith(NODES_PATH_PREFIX)) {
            return path.substring(NODES_PATH_PREFIX.length());
        } else {
            return path;
        }
    }

    protected void handleException(Throwable e) {
        log.error("" + e, e);
    }
}
