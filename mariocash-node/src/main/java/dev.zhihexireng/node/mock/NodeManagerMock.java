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

package dev.zhihexireng.node.mock;

import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.NodeEventListener;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.TransactionPool;
import dev.zhihexireng.core.exception.NotValidteException;
import dev.zhihexireng.node.BlockBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeManagerMock implements NodeManager {

    private final BlockBuilder blockBuilder = new BlockBuilderMock();

    private final BlockChain blockChain = new BlockChain();

    private final TransactionPool transactionPool = new TransactionPoolMock();

    private NodeEventListener listener;

    @Override
    public void setListener(NodeEventListener listener) {
        this.listener = listener;
    }

    @Override
    public Transaction getTxByHash(String id) {
        return (Transaction) transactionPool.getTxByHash(id);
    }

    @Override
    public Transaction addTransaction(Transaction tx) throws IOException {
        Transaction newTx = (Transaction) transactionPool.addTx(tx);
        if (listener != null) {
            listener.newTransaction(tx);
        }
        return newTx;
    }

    @Override
    public Set<Block> getBlocks() {
        Set<Block> blockSet = new HashSet<>();
        for (Block block : blockChain.getBlocks().values()) {
            blockSet.add(block);
        }
        return blockSet;
    }

    @Override
    public Block generateBlock() throws IOException, NotValidteException {
        Block block =
                blockBuilder.build(transactionPool.getTxList(), blockChain.getPrevBlock());

        blockChain.addBlock(block);

        if (listener != null) {
            listener.newBlock(block);
        }
        removeTxByBlock(block);
        return block;
    }

    @Override
    public Block addBlock(Block block) throws IOException, NotValidteException {
        Block newBlock = null;
        if (blockChain.isGenesisBlockChain() && block.getIndex() == 0) {
            blockChain.addBlock(block);
            newBlock = block;
        }
        else if (blockChain.getPrevBlock().nextIndex() == block.getIndex()) {
            blockChain.addBlock(block);
            newBlock = block;
        }
        if (listener != null) {
            listener.newBlock(block);
        }
        removeTxByBlock(block);
        return newBlock;
    }

    @Override
    public Block getBlockByIndexOrHash(String indexOrHash) {

        if (isNumeric(indexOrHash)) {
            int index = Integer.parseInt(indexOrHash);
            return blockChain.getBlockByIndex(index);
        } else {
            return blockChain.getBlockByHash(indexOrHash);
        }
    }

    private void removeTxByBlock(Block block) throws IOException {
        if (block == null || block.getData().getTransactionList() == null) {
            return;
        }
        List<String> idList = new ArrayList<>();

        for (Transaction tx : block.getData().getTransactionList()) {
            idList.add(tx.getHashString());
        }
        this.transactionPool.removeTx(idList);
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
