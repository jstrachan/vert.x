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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper methods for working with maps
 */
public class Maps {

    /**
     * For a map, find all the keys which start with the given keyPrefix and create a new map will all the keys and values
     * where the key has the keyPrefix removed.
     *
     * e.g. for a map <code>{ "foo.bar" : "cheese" }</code> then the result of <code>Maps.getSubMap(map, "foo.")</code>
     * would result in the map <code>{ "bar" : "cheese" }</code>
     *
     * @param map a map of configuration
     * @param keyPrefix the prefix of the keys to match and then remove the prefix in the resulting map
     * @return a map of all matching keys and values; where the key starts with keyPrefix and its then stripped.
     */
    public static Map<String,Object> getSubMap(Map map, String keyPrefix) {
        int length = keyPrefix.length();
        Map<String, Object> answer = new HashMap<String, Object>();
        Set<Map.Entry> set = map.entrySet();
        for (Map.Entry entry : set) {
            Object keyValue = entry.getKey();
            if (keyValue != null) {
                String key = keyValue.toString();
                if (key.startsWith(keyPrefix)) {
                    String childKey = key.substring(length);
                    answer.put(childKey, entry.getValue());
                }
            }
        }
        return answer;
    }
}
