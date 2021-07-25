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

package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.contract.Contract;
import dev.zhihexireng.core.event.BranchEventListener;
import dev.zhihexireng.core.event.BranchGroupEventListener;
import dev.zhihexireng.core.event.ContractEventListener;
import dev.zhihexireng.core.exception.DuplicatedException;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BranchGroup {
    private static final Logger log = LoggerFactory.getLogger(BranchGroup.class);

    private final Map<BranchId, BlockChain> branches = new ConcurrentHashMap<>();

    private BranchGroupEventListener listener;

    public void setListener(BranchGroupEventListener listener) {
        this.listener = listener;
    }

    public void addBranch(BranchId branchId, BlockChain blockChain,
                          BranchEventListener branchEventListener,
                          ContractEventListener contractEventListener) {
        if (branches.containsKey(branchId)) {
            throw new DuplicatedException(branchId.toString() + " duplicated");
        }
        blockChain.addListener(branchEventListener);
        blockChain.init(contractEventListener);
        branches.put(branchId, blockChain);
        if (listener != null) {
            listener.newBranch(blockChain);
        }
    }

    public BlockChain getBranch(BranchId branchId) {
        return branches.get(branchId);
    }

    public Collection<BlockChain> getAllBranch() {
        return branches.values();
    }

    public TransactionHusk addTransaction(TransactionHusk tx) {
        if (branches.containsKey(tx.getBranchId())) {
            return branches.get(tx.getBranchId()).addTransaction(tx);
        }
        return tx;
    }

    public long getLastIndex(BranchId id) {
        return branches.get(id).getLastIndex();
    }

    public List<TransactionHusk> getTransactionList(BranchId branchId) {
        return branches.get(branchId).getTransactionList();
    }

    public TransactionHusk getTxByHash(BranchId branchId, String id) {
        return getTxByHash(branchId, new Sha3Hash(id));
    }

    TransactionHusk getTxByHash(BranchId branchId, Sha3Hash hash) {
        return branches.get(branchId).getTxByHash(hash);
    }

    public void generateBlock(Wallet wallet) {
        for (BlockChain blockChain : branches.values()) {
            if (blockChain.getBranchId().equals(BranchId.stem())) {
                blockChain.generateBlock(wallet);
            } else {
                try {
                    int randomSleep = ThreadLocalRandom.current().nextInt(1, 9 + 1);
                    TimeUnit.SECONDS.sleep(randomSleep);
                    blockChain.generateBlock(wallet);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage());
                }
            }
        }
    }

    public BlockHusk addBlock(BlockHusk block) {
        if (branches.containsKey(block.getBranchId())) {
            return branches.get(block.getBranchId()).addBlock(block, true);
        }
        return block;
    }

    public BlockHusk getBlockByIndex(BranchId branchId, long index) {
        return branches.get(branchId).getBlockByIndex(index);
    }

    public BlockHusk getBlockByHash(BranchId branchId, String hash) {
        return branches.get(branchId).getBlockByHash(hash);
    }

    public int getBranchSize() {
        return branches.size();
    }

    public StateStore<?> getStateStore(BranchId branchId) {
        return branches.get(branchId).getRuntime().getStateStore();
    }

    public TransactionReceiptStore getTransactionReceiptStore(BranchId branchId) {
        return branches.get(branchId).getRuntime().getTransactionReceiptStore();
    }

    public List<TransactionHusk> getUnconfirmedTxs(BranchId branchId) {
        return branches.get(branchId).getUnconfirmedTxs();
    }

    Contract getContract(BranchId branchId) {
        return branches.get(branchId).getContract();
    }

    public JsonObject query(JsonObject query) {
        try {
            BranchId branchId = BranchId.of(query.get("address").getAsString());
            BlockChain chain = branches.get(branchId);
            return chain.getRuntime().query(chain.getContract(), query);
        } catch (Exception e) {
            throw new FailedOperationException(e);
        }
    }
}
