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

import com.google.protobuf.InvalidProtocolBufferException;
import dev.zhihexireng.TestUtils;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.store.TransactionStore;
import dev.zhihexireng.core.store.datasource.DbSource;
import dev.zhihexireng.core.store.datasource.HashMapDbSource;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionStoreTest {

    TransactionStore ts;

    @Before
    public void setUp() {
        DbSource db = new HashMapDbSource();
        ts = new TransactionStore(db);
        ts.flush();
    }

    @Test
    public void shouldGetFromDb() throws InvalidProtocolBufferException {
        TransactionHusk tx = TestUtils.createTxHusk();
        Sha3Hash key = tx.getHash();
        ts.put(tx.getHash(), tx);

        ts.batchAll();
        TransactionHusk transactionHusk = ts.get(key);
        assertThat(transactionHusk).isEqualTo(tx);
    }

    @Test
    public void shouldBeBatched() {
        TransactionHusk tx = TestUtils.createTxHusk();
        ts.put(tx.getHash(), tx);

        ts.batchAll();
        assertThat(ts.countFromCache()).isZero();
        assertThat(ts.countFromDb()).isEqualTo(1L);
    }

    @Test
    public void shouldBeGotTxFromCache() throws InvalidProtocolBufferException {
        TransactionHusk tx = TestUtils.createTxHusk();

        Sha3Hash key = tx.getHash();
        ts.put(tx.getHash(), tx);

        TransactionHusk foundTx = ts.get(key);
        assertThat(foundTx).isNotNull();
        assertThat(foundTx.getBody()).contains("transfer");
    }

    @Test
    public void shouldBePutTx() {
        TransactionHusk tx = TestUtils.createTxHusk();
        ts.put(tx.getHash(), tx);
    }

    @Test
    public void shouldLoadTestObject() {
        assertThat(ts).isNotNull();
    }
}
