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

import dev.zhihexireng.core.husk.TransactionHusk;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Deprecated
public class SimpleTransactionPool implements CachePool<String, TransactionHusk> {
    private static final Logger log = LoggerFactory.getLogger(SimpleTransactionPool.class);

    private final Cache<String, TransactionHusk> cache;

    public SimpleTransactionPool(Cache<String, TransactionHusk> cache) {
        this.cache = cache;
    }

    @Override
    public TransactionHusk get(String key) {
        return cache.get(key);
    }

    @Override
    public TransactionHusk put(TransactionHusk tx) {
        cache.put(tx.getHash().toString(), tx);
        return tx;
    }

    @Override
    public Map<String, TransactionHusk> getAll(Set<String> keys) {
        return cache.getAll(keys);
    }

    @Override
    public void remove(Set<String> keys) {
        cache.removeAll(keys);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
