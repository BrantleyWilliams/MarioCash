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

package dev.zhihexireng.node;

import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeEventListener;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.mapper.BlockMapper;
import dev.zhihexireng.core.mapper.TransactionMapper;
import dev.zhihexireng.core.net.NodeSyncClient;
import dev.zhihexireng.proto.BlockChainProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageSender implements DisposableBean, NodeEventListener {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private List<NodeSyncClient> activePeerList = Collections.synchronizedList(new ArrayList<>());

    @PreDestroy
    public void destroy() {
        for (NodeSyncClient client : activePeerList) {
            client.stop();
        }
    }

    public void ping() {
        for (NodeSyncClient client : activePeerList) {
            client.ping("Ping");
        }
    }

    @Override
    public void newTransaction(Transaction tx) {
        BlockChainProto.Transaction protoTx
                = TransactionMapper.transactionToProtoTransaction(tx);
        BlockChainProto.Transaction[] txns = new BlockChainProto.Transaction[] {protoTx};

        for (NodeSyncClient client : activePeerList) {
            client.broadcastTransaction(txns);
        }
    }

    @Override
    public void newBlock(Block block) {
        BlockChainProto.Block[] blocks
                = new BlockChainProto.Block[] {BlockMapper.blockToProtoBlock(block)};
        for (NodeSyncClient client : activePeerList) {
            client.broadcastBlock(blocks);
        }
    }

    @Override
    public void newActivePeer(NodeSyncClient client) {
        activePeerList.add(client);
    }

    /**
     * Sync block list.
     *
     * @param offset the offset
     * @return the block list
     */
    @Override
    public List<Block> syncBlock(long offset) throws IOException {
        if (activePeerList.isEmpty()) {
            log.warn("Active peer is empty.");
            return Collections.emptyList();
        }
        // TODO sync peer selection policy
        List<BlockChainProto.Block> blockList = activePeerList.get(0).syncBlock(offset);
        log.debug("Synchronize block received=" + blockList.size());
        List<Block> syncList = new ArrayList<>(blockList.size());
        for (BlockChainProto.Block block : blockList) {
            syncList.add(BlockMapper.protoBlockToBlock(block));
        }
        return syncList;
    }

    /**
     * Sync transaction list.
     *
     * @return the transaction list
     */
    @Override
    public List<Transaction> syncTransaction() throws IOException {
        if (activePeerList.isEmpty()) {
            log.warn("Active peer is empty.");
            return Collections.emptyList();
        }
        // TODO sync peer selection policy
        List<BlockChainProto.Transaction> txList = activePeerList.get(0).syncTransaction();
        log.debug("Synchronize transaction received=" + txList.size());
        List<Transaction> syncList = new ArrayList<>(txList.size());
        for (BlockChainProto.Transaction tx : txList) {
            syncList.add(TransactionMapper.protoTransactionToTransaction(tx));
        }
        return syncList;
    }
}
