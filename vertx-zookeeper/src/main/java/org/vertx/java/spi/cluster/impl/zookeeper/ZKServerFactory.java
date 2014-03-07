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

import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.io.File;
import java.net.BindException;
import java.net.InetSocketAddress;

/**
 * A simple factory bean for creating ZooKeeper servers; particularly useful for testing or embedding a ZK server.
 */
public class ZKServerFactory {
    private static final Logger log = LoggerFactory.getLogger(ZKServerFactory.class);

    private ZooKeeperServer zooKeeperServer = new ZooKeeperServer();
    private NIOServerCnxnFactory connectionFactory;
    private File dataLogDir;
    private File dataDir;
    private boolean purge;
    protected int tickTime = ZooKeeperServer.DEFAULT_TICK_TIME;
    /**
     * defaults to -1 if not set explicitly
     */
    private int minSessionTimeout = -1;
    /**
     * defaults to -1 if not set explicitly
     */
    private int maxSessionTimeout = -1;
    private InetSocketAddress clientPortAddress;
    private int maxClientConnections;
    private int port = 2181;
    private boolean ignoreBindExceptions;

    public void init() throws Exception {
        if (purge) {
            deleteFilesInDir(getDataLogDir());
            deleteFilesInDir(getDataDir());
        }
        FileTxnSnapLog ftxn = new FileTxnSnapLog(getDataLogDir(), getDataDir());
        zooKeeperServer.setTxnLogFactory(ftxn);
        zooKeeperServer.setTickTime(getTickTime());
        zooKeeperServer.setMinSessionTimeout(getMinSessionTimeout());
        zooKeeperServer.setMaxSessionTimeout(getMaxSessionTimeout());
        try {
            connectionFactory = new NIOServerCnxnFactory();
            connectionFactory.configure(getClientPortAddress(), getMaxClientConnections());
            connectionFactory.startup(zooKeeperServer);
        } catch (BindException e) {
            if (isIgnoreBindExceptions()) {
                log.warn("Bind exception trying to create a ZooKeeper server on port " + port + ". Ignoring as there is probably already one running on this port. Exception message: " + e.getMessage());

            } else {
                throw e;
            }
        }
    }

    public void destroy() throws Exception {
        shutdown();
    }

    private void deleteFilesInDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFilesInDir(file);
                } else {
                    file.delete();
                }
            }
        }
    }


    protected void shutdown() {
        if (connectionFactory != null) {
            connectionFactory.shutdown();
            try {
                connectionFactory.join();
            } catch (InterruptedException e) {
                // Ignore
            }
            connectionFactory = null;
        }
        if (zooKeeperServer != null) {
            zooKeeperServer.shutdown();
            zooKeeperServer = null;
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    public ZooKeeperServer getZooKeeperServer() {
        return zooKeeperServer;
    }

    public NIOServerCnxnFactory getConnectionFactory() {
        return connectionFactory;
    }

    public File getDataLogDir() {
        if (dataLogDir == null) {
            dataLogDir = new File(getZKOutputDir(), "log");
            dataLogDir.mkdirs();
        }
        return dataLogDir;
    }

    public File getDataDir() {
        if (dataDir == null) {
            dataDir = new File(getZKOutputDir(), "data");
            dataDir.mkdirs();
        }
        return dataDir;
    }

    public int getTickTime() {
        return tickTime;
    }

    public int getMinSessionTimeout() {
        return minSessionTimeout;
    }

    public int getMaxSessionTimeout() {
        return maxSessionTimeout;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getClientPortAddress() {
        if (clientPortAddress == null) {
            clientPortAddress = new InetSocketAddress(port);
        }
        return clientPortAddress;
    }

    public int getMaxClientConnections() {
        return maxClientConnections;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setClientPortAddress(InetSocketAddress clientPortAddress) {
        this.clientPortAddress = clientPortAddress;
    }

    public void setConnectionFactory(NIOServerCnxnFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public void setDataLogDir(File dataLogDir) {
        this.dataLogDir = dataLogDir;
    }

    public void setMaxClientConnections(int maxClientConnections) {
        this.maxClientConnections = maxClientConnections;
    }

    public void setMaxSessionTimeout(int maxSessionTimeout) {
        this.maxSessionTimeout = maxSessionTimeout;
    }

    public void setMinSessionTimeout(int minSessionTimeout) {
        this.minSessionTimeout = minSessionTimeout;
    }

    public void setTickTime(int tickTime) {
        this.tickTime = tickTime;
    }

    public void setZooKeeperServer(ZooKeeperServer zooKeeperServer) {
        this.zooKeeperServer = zooKeeperServer;
    }

    public boolean isPurge() {
        return purge;
    }

    public void setPurge(boolean purge) {
        this.purge = purge;
    }

    public boolean isIgnoreBindExceptions() {
        return ignoreBindExceptions;
    }

    /**
     * Enabling ignoreBindExceptions allows you to run many JVMs on a machine and the first one creates the ZK server and other JVMs will silently avoid creating one
     * on startup.
     *
     * Usually you will create ZK servers up front for an environment; but this makes it easy to lazily create ZK servers with the first one winning - which is handy for tests
     */
    public void setIgnoreBindExceptions(boolean ignoreBindExceptions) {
        this.ignoreBindExceptions = ignoreBindExceptions;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected File getZKOutputDir() {
        String baseDir = System.getProperty("basedir", ".");
        File dir = new File(baseDir + "/build/zk");
        dir.mkdirs();
        return dir;
    }


}
