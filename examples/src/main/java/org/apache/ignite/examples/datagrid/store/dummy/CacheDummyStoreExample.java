/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.examples.datagrid.store.dummy;

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.examples.*;
import org.apache.ignite.examples.datagrid.store.*;
import org.apache.ignite.transactions.*;

import javax.cache.configuration.*;
import java.util.*;

import static org.apache.ignite.cache.CacheAtomicityMode.*;

/**
 * Demonstrates usage of cache with underlying persistent store configured.
 * <p>
 * This example uses {@link CacheDummyPersonStore} as a persistent store.
 * <p>
 * Remote nodes should always be started with special configuration file which
 * enables P2P class loading: {@code 'ignite.{sh|bat} examples/config/example-ignite.xml'}.
 * <p>
 * Alternatively you can run {@link ExampleNodeStartup} in another JVM which will
 * start node with {@code examples/config/example-ignite.xml} configuration.
 */
public class CacheDummyStoreExample {
    /** Heap size required to run this example. */
    public static final int MIN_MEMORY = 1024 * 1024 * 1024;

    /** Number of entries to load. */
    private static final int ENTRY_COUNT = 100_000;

    /** Global person ID to use across entire example. */
    private static final Long id = Math.abs(UUID.randomUUID().getLeastSignificantBits());

    /**
     * Executes example.
     *
     * @param args Command line arguments, none required.
     * @throws IgniteException If example execution failed.
     */
    public static void main(String[] args) throws IgniteException {
        ExamplesUtils.checkMinMemory(MIN_MEMORY);

        // To start ignite with desired configuration uncomment the appropriate line.
        try (Ignite ignite = Ignition.start("examples/config/example-ignite.xml")) {
            System.out.println();
            System.out.println(">>> Cache store example started.");

            CacheConfiguration<Long, Person> cacheCfg = new CacheConfiguration<>();

            // Set atomicity as transaction, since we are showing transactions in example.
            cacheCfg.setAtomicityMode(TRANSACTIONAL);

            // Configure Dummy store.
            cacheCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(CacheDummyPersonStore.class));

            cacheCfg.setReadThrough(true);
            cacheCfg.setWriteThrough(true);

            try (IgniteCache<Long, Person> cache = ignite.getOrCreateCache(cacheCfg)) {
                long start = System.currentTimeMillis();

                // Start loading cache from persistent store on all caching nodes.
                cache.loadCache(null, ENTRY_COUNT);

                long end = System.currentTimeMillis();

                System.out.println(">>> Loaded " + cache.size() + " keys with backups in " + (end - start) + "ms.");

                // Start transaction and make several operations with write/read-through.
                try (Transaction tx = ignite.transactions().txStart()) {
                    Person val = cache.get(id);

                    System.out.println("Read value: " + val);

                    val = cache.getAndPut(id, new Person(id, "Isaac", "Newton"));

                    System.out.println("Overwrote old value: " + val);

                    val = cache.get(id);

                    System.out.println("Read value: " + val);

                    tx.commit();
                }

                System.out.println("Read value after commit: " + cache.get(id));
            }
        }
    }
}
