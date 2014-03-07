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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.ClusterManager;

import java.util.List;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertFalse;
import static org.vertx.testtools.VertxAssert.assertTrue;

/**
 * @author <a href="https://twitter.com/jstrachan">James Strachan</a>
 */
public class ZooKeeperClusterManagerTest {
    private static final Logger log = LoggerFactory.getLogger(ZooKeeperClusterManagerTest.class);

    private ClusterManager clusterManager;
    private ClusterManager clusterManager2;

    @Before
    public void init() throws Exception {
    }

    @After
    public void destroy() throws Exception {
        leave(clusterManager2);
        leave(clusterManager);
    }


    @Test
    public void testNodesAppearInList() throws Exception {
        VertxSPI verttx = null;
        clusterManager = new ZooKeeperClusterManagerFactory().createClusterManager(verttx);
        clusterManager.join();

        String nodeID = clusterManager.getNodeID();
        log.info("Local node is: " + nodeID);

        // lets create another node
        clusterManager2 = new ZooKeeperClusterManagerFactory().createClusterManager(verttx);
        clusterManager2.join();

        String nodeID2 = clusterManager2.getNodeID();
        log.info("node2 is: " + nodeID2);

        Thread.sleep(3000);

        assertFalse("Node ids should not be the same", nodeID.equals(nodeID2));
        List<String> nodes = clusterManager.getNodes();
        assertEquals("Node size " + nodes, 2, nodes.size());

        assertNodesContains(nodes, nodeID);
        assertNodesContains(nodes, nodeID2);

        clusterManager2.leave();
        clusterManager2 = null;

        Thread.sleep(3000);
        nodes = clusterManager.getNodes();
        assertEquals("Node size " + nodes, 1, nodes.size());

    }

    protected void leave(ClusterManager clusterManager) {
        if (clusterManager != null) {
            clusterManager.leave();
        }
    }

    protected void assertNodesContains(List<String> nodes, String id) {
        assertTrue("Nodes should contain: " + id + " but was: " + nodes, nodes.contains(id));
    }
}
