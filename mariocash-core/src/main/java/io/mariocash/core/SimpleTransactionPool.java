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

package dev.zhihexireng.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.io.IOException;
import java.util.List;

public class SimpleTransactionPool implements TransactionPool {
    @Value("#{cacheManager.getCache('transactionPool')}")
    private ConcurrentMapCache transactionPool;

    @Override
    public Transaction getTxByHash(String id) {
        return null;
    }

    @Override
    public Transaction addTx(Transaction tx) throws IOException {
        return null;
    }

    @Override
    public List getTxList() {
        return null;
    }

    @Override
    public void removeTx(List<String> hashList) {

    }

    public Transaction get(String hashString, Class<Transaction> transactionClass) {
        return transactionPool.get(hashString, transactionClass);
    }

    public void putIfAbsent(String hashString, Transaction tx) {
        transactionPool.putIfAbsent(hashString, tx);
    }

    public void clear() {
        transactionPool.clear();
    }
}
