/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.core.store;

import com.google.common.collect.EvictingQueue;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.store.datasource.DbSource;
import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionStore implements Store<Sha3Hash, TransactionHusk> {
    private static final Logger log = LoggerFactory.getLogger(TransactionStore.class);
    private static final Lock LOCK = new ReentrantLock();

    private static final int CACHE_SIZE = 500;
    private long countOfTxs = 0;

    private Queue<TransactionHusk> readCache;
    private final Cache<Sha3Hash, TransactionHusk> pendingPool;
    private final Set<Sha3Hash> pendingKeys = new HashSet<>();
    private final DbSource<byte[], byte[]> db;

    TransactionStore(DbSource<byte[], byte[]> db) {
        this.db = db.init();
        this.pendingPool = CacheManagerBuilder
                .newCacheManagerBuilder().build(true)
                .createCache("txPool", CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(Sha3Hash.class, TransactionHusk.class,
                                ResourcePoolsBuilder.heap(Long.MAX_VALUE)));
        this.readCache = EvictingQueue.create(CACHE_SIZE);

    }

    TransactionStore(DbSource<byte[], byte[]> db, int cacheSize) {
        this(db);
        this.readCache = EvictingQueue.create(cacheSize);
    }

    public Collection<TransactionHusk> getRecentTxs() {
        return new ArrayList<>(readCache);
    }

    @Override
    public boolean contains(Sha3Hash key) {
        return pendingPool.containsKey(key) || db.get(key.getBytes()) != null;
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void put(Sha3Hash key, TransactionHusk tx) {
        LOCK.lock();
        pendingPool.put(key, tx);
        if (pendingPool.containsKey(key)) {
            pendingKeys.add(key);
        } else {
            log.warn("unconfirmedTxs size={}, ignore key={}", pendingKeys.size(), key);
        }
        LOCK.unlock();
    }

    @Override
    public TransactionHusk get(Sha3Hash key) {
        TransactionHusk item = pendingPool.get(key);
        try {
            return item != null ? item : new TransactionHusk(db.get(key.getBytes()));
        } catch (Exception e) {
            throw new FailedOperationException(e);
        }
    }

    public void batch(Set<Sha3Hash> keys) {
        LOCK.lock();
        if (keys.size() > 0) {
            Map<Sha3Hash, TransactionHusk> map = pendingPool.getAll(keys);
            int countOfBatchedTxs = map.size();
            for (Sha3Hash key : map.keySet()) {
                TransactionHusk foundTx = map.get(key);
                if (foundTx != null) {
                    db.put(key.getBytes(), foundTx.getData());
                    addReadCache(foundTx);
                } else {
                    countOfBatchedTxs -= 1;
                }
            }
            this.countOfTxs += countOfBatchedTxs;
            this.flush(keys);
        }
        LOCK.unlock();
    }

    private void addReadCache(TransactionHusk tx) {
        readCache.add(tx);
    }

    public long countOfTxs() {
        return this.countOfTxs;
    }

    public Collection<TransactionHusk> getUnconfirmedTxs() {
        return pendingPool.getAll(pendingKeys).values();
    }

    private void flush(Set<Sha3Hash> keys) {
        log.debug("pendingSize={}, readCacheSize={}", pendingKeys.size(), readCache.size());
        pendingPool.removeAll(keys);
        pendingKeys.removeAll(keys);
    }

    public void updateCache(List<TransactionHusk> body) {
        this.countOfTxs += body.size();
        this.readCache.addAll(body);
    }
}
