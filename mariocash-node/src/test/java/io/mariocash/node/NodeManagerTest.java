/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.node;

import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.Runtime;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.exception.InvalidSignatureException;
import dev.zhihexireng.core.net.PeerClientChannel;
import dev.zhihexireng.core.net.PeerGroup;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.core.store.TransactionStore;
import dev.zhihexireng.core.store.datasource.HashMapDbSource;
import dev.zhihexireng.node.config.NodeProperties;
import dev.zhihexireng.util.ByteUtil;
import dev.zhihexireng.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class NodeManagerTest {

    private static final Logger log = LoggerFactory.getLogger(NodeManagerTest.class);

    private NodeManagerImpl nodeManager;
    private NodeProperties nodeProperties;
    private TransactionHusk tx;
    private BlockHusk firstBlock;
    private BlockHusk secondBlock;

    @Before
    public void setUp() throws Exception {
        this.nodeProperties = new NodeProperties();
        nodeProperties.getGrpc().setHost("localhost");
        nodeProperties.getGrpc().setPort(9090);
        this.nodeManager = new NodeManagerImpl();
        nodeManager.setPeerGroup(new PeerGroup());
        nodeManager.setNodeProperties(nodeProperties);
        MessageSender<PeerClientChannel> messageSender = new MessageSender<>(nodeProperties);
        messageSender.setListener(nodeManager);
        nodeManager.setMessageSender(messageSender);
        nodeManager.setWallet(new Wallet());

        TransactionStore transactionStore = new TransactionStore(new HashMapDbSource());
        nodeManager.setTransactionStore(transactionStore);
        Runtime runtime = new Runtime(new TransactionReceiptStore());
        nodeManager.setRuntime(runtime);
        nodeManager.setBlockChain(new BlockChain(
                new File(getClass().getClassLoader()
                        .getResource("branch-sample.json").getFile())));
        nodeManager.setNodeHealthIndicator(mock(NodeHealthIndicator.class));
        nodeManager.init();
        assert nodeManager.getNodeUri() != null;
        this.tx = TestUtils.createTxHusk(nodeManager.getWallet());
        this.firstBlock = BlockHusk.build(nodeManager.getWallet(), Collections.singletonList(tx),
                nodeManager.getBlockByIndexOrHash("0"));
        this.secondBlock = BlockHusk.build(nodeManager.getWallet(), Collections.singletonList(tx),
                firstBlock);
    }

    @After
    public void tearDown() throws Exception {
        //TODO 테스트 설정 파일에서 DB 부분 제거
        FileUtil.recursiveDelete(Paths.get(".mariocash/db"));
    }

    @Test
    public void addTransactionTest() {
        nodeManager.addTransaction(tx);
        TransactionHusk pooledTx = nodeManager.getTxByHash(tx.getHash());
        assert pooledTx.getHash().equals(tx.getHash());
    }

    @Test(expected = InvalidSignatureException.class)
    public void unsignedTxTest() {
        nodeManager.addTransaction(TestUtils.createUnsignedTxHusk());
    }

    @Test(expected = FailedOperationException.class)
    public void addTransactionExceptionTest() {
        nodeManager.setMessageSender(null);
        nodeManager.addTransaction(tx);
    }

    @Test(expected = InvalidSignatureException.class)
    public void failedOperationExceptionTest() {
        nodeManager.addTransaction(TestUtils.createInvalidTxHusk());
    }

    @Test
    public void addBlockTest() {
        nodeManager.addTransaction(tx);
        nodeManager.addBlock(firstBlock);
        nodeManager.addBlock(secondBlock);
        assert nodeManager.getBlocks().size() == 3;
        assert nodeManager.getBlockByIndexOrHash("2").getHash()
                .equals(secondBlock.getHash());
        TransactionHusk foundTx = nodeManager.getTxByHash(tx.getHash());
        assert foundTx.getHash().equals(tx.getHash());
    }

    @Test
    public void generateBlockTest() {
        nodeManager.addTransaction(tx);
        BlockHusk newBlock = nodeManager.generateBlock();
        assert nodeManager.getBlocks().size() == 2;
        BlockHusk chainedBlock = nodeManager.getBlockByIndexOrHash(newBlock.getHash().toString());
        assert chainedBlock.getHash().equals(newBlock.getHash());
        log.debug(Hex.toHexString(ByteUtil.longToBytes(chainedBlock.getBody().size())));
        assert chainedBlock.getBody().size() != 0;
        assertThat(nodeManager.getTxByHash(tx.getHash()).getHash(), equalTo(tx.getHash()));
    }

    @Test
    public void addPeerTest() {
        int testCount = nodeProperties.getMaxPeers() + 5;
        for (int i = 0; i < testCount; i++) {
            int port = i + 9000;
            nodeManager.addPeer("ynode://75bff16c@localhost:" + port);
        }
        assert nodeProperties.getMaxPeers() == nodeManager.getPeerUriList().size();
    }
}
