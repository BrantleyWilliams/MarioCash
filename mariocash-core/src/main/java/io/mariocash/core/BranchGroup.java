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

import dev.zhihexireng.common.Sha3Hash;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BranchGroup {

    private Map<BranchId, BlockChain> branches = new ConcurrentHashMap<>();
    private static BlockChain chain;

    private Runtime runtime;

    public BranchGroup(Runtime runtime) {
        this.runtime = runtime;
    }

    public void addBranch(BranchId branchId, BlockChain blockChain) {
        if (branches.containsKey(branchId)) {
            return;
        }
        chain = blockChain; // TODO remove
        branches.put(branchId, blockChain);
        blockChain.init(runtime);
    }

    public TransactionHusk addTransaction(TransactionHusk tx) {
        return chain.addTransaction(tx);
    }

    public long getLastIndex() {
        return chain.getLastIndex();
    }

    public List<TransactionHusk> getTransactionList() {
        return chain.getTransactionList();
    }

    public TransactionHusk getTxByHash(Sha3Hash hash) {
        return chain.getTxByHash(hash);
    }

    public BlockHusk generateBlock(Wallet wallet) {
        return chain.generateBlock(wallet, runtime);
    }

    public BlockHusk addBlock(BlockHusk block) {
        return chain.addBlock(block, runtime);
    }

    public Set<BlockHusk> getBlocks() {
        return chain.getBlocks();
    }

    public BlockHusk getBlockByIndexOrHash(String indexOrHash) {
        if (isNumeric(indexOrHash)) {
            int index = Integer.parseInt(indexOrHash);
            return chain.getBlockByIndex(index);
        } else {
            return chain.getBlockByHash(indexOrHash);
        }
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
