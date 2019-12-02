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

package dev.zhihexireng.trie;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.Wallet;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;

public class TrieTests {

    private static final Logger log = LoggerFactory.getLogger(TrieTests.class);

    private Transaction tx1;
    private Transaction tx2;
    private Wallet wallet;

    @Before
    public void setUp() throws IOException, InvalidCipherTextException {
        wallet = new Wallet();
        // create tx_data1
        JsonObject data1 = new JsonObject();
        data1.addProperty("key", "balance");
        data1.addProperty("operator", "transfer");
        data1.addProperty("value", 30);

        // create tx_data2
        JsonObject data2 = new JsonObject();
        data2.addProperty("key", "balance");
        data2.addProperty("operator", "transfer");
        data2.addProperty("value", 10);

        // create sample tx
        this.tx1 = new Transaction(wallet, data1);
        this.tx2 = new Transaction(wallet, data2);
    }

    @Test
    public void MerkleRootTest() throws IOException {

        // 1. test merkle root with tx 7
        // create transactions
        List<Transaction> txsList;
        txsList = new ArrayList<>();
        txsList.add(this.tx1);
        txsList.add(this.tx2);
        txsList.add(this.tx1);
        txsList.add(this.tx1);
        txsList.add(this.tx1);
        txsList.add(this.tx2);
        txsList.add(this.tx2);

        byte[] merkleRoot;
        merkleRoot = Trie.getMerkleRoot(txsList);
        assertNotNull(merkleRoot);

        log.debug("MerkelRoot with tx 7=" + Hex.encodeHexString(merkleRoot));

        // 2. test with tx 1
        txsList = new ArrayList<>();
        txsList.add(this.tx1);
        merkleRoot = Trie.getMerkleRoot(txsList);
        assertNotNull(merkleRoot);

        log.debug("MerkelRoot with tx 1=" + Hex.encodeHexString(merkleRoot));

        // 3. test with tx 0
        txsList = new ArrayList<>();
        merkleRoot = Trie.getMerkleRoot(txsList);
        assertNull(merkleRoot);

        log.debug("MerkleRoot with tx 0 = null");

        // 4. test with tx null
        merkleRoot = Trie.getMerkleRoot(null);
        assertNull(merkleRoot);

        log.debug("MerkleRoot with tx null = null");
    }
}
