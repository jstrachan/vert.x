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
import org.vertx.java.spi.cluster.impl.zookeeper.util.Maps;

import java.util.HashMap;
import java.util.Map;

import static org.vertx.testtools.VertxAssert.assertEquals;

/**
 * @author <a href="https://twitter.com/jstrachan">James Strachan</a>
 */
public class SubMapTest {
    @Test
    public void testSubMap() throws Exception {
        Map<String,String> map = new HashMap<String, String>();
        map.put("client.foo", "abc");
        map.put("client.bar", "def");
        map.put("server.bar", "xyz");

        Map<String, Object> subMap = Maps.getSubMap(map, "client.");
        assertEquals("Size of submap " + subMap, 2, subMap.size());
        assertMapValue(subMap, "foo", "abc");
        assertMapValue(subMap, "bar", "def");
    }

    public static void assertMapValue(Map<String, ?> map, String key, Object expected) {
        assertEquals("key of " + key, expected, map.get(key));
    }

}
