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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.contract.Contract;
import dev.zhihexireng.contract.ContractClassLoader;
import dev.zhihexireng.contract.ContractMeta;
import dev.zhihexireng.contract.NoneContract;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.core.store.BlockStore;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.StoreBuilder;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.core.store.TransactionStore;
import dev.zhihexireng.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class BlockChainBuilder {

    private final StoreBuilder storeBuilder;
    private BlockHusk genesis;
    private Wallet wallet;
    private Branch branch;

    private BlockChainBuilder(boolean isProduction) {
        this.storeBuilder = new StoreBuilder(isProduction);
    }

    public BlockChainBuilder addGenesis(BlockHusk genesis) {
        this.genesis = genesis;
        return this;
    }

    public BlockChainBuilder addWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public BlockChainBuilder addBranch(Branch branch) {
        this.branch = branch;
        return this;
    }

    public BlockChain build(Wallet wallet, Branch branch) throws IllegalAccessException,
            InstantiationException {
        // TODO fix change builder patten ref : https://jdm.kr/blog/217

        BlockStore blockStore = storeBuilder.buildBlockStore(branch.getBranchId());

        BlockHusk genesis;
        if (blockStore.size() > 0) {
            genesis = blockStore.get(0);
        } else {
            genesis = getGenesis(wallet, branch);
        }
        blockStore.close();
        return build(genesis, branch.getName());
    }

    public BlockChain build(BlockHusk genesis, String branchName)
            throws InstantiationException, IllegalAccessException {
        // TODO fix blockchain by branch information (contract and other information)
        BlockStore blockStore = storeBuilder.buildBlockStore(genesis.getBranchId());
        TransactionStore txStore = storeBuilder.buildTxStore(genesis.getBranchId());

        Contract contract = getContract(branchName);
        Runtime<?> runtime = getRunTime(contract.getClass().getGenericSuperclass().getClass());

        BlockChain blockChain = new BlockChain(genesis, blockStore, txStore, contract, runtime);
        blockChain.setBranchName(branchName);
        return blockChain;
    }

    public BlockChain build() throws InstantiationException, IllegalAccessException {
        // TODO initialization wallet and branch
        this.genesis = getGenesis(this.wallet, this.branch);

        BlockStore blockStore = storeBuilder.buildBlockStore(genesis.getBranchId());
        TransactionStore txStore = storeBuilder.buildTxStore(genesis.getBranchId());
        // TODO branch Name get branch
        Contract contract = BlockChainBuilder.getContract(this.branch.getName());
        Runtime<?> runtime = getRunTime(contract.getClass().getGenericSuperclass().getClass());
        BlockChain bc = new BlockChain(genesis, blockStore, txStore, contract, runtime);

        return bc;
    }

    private BlockHusk getGenesis(Wallet wallet, Branch branch) {

        if (!branch.isYeed()) {
            throw new FailedOperationException("Not supported name=" + branch.getName());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("branchId", branch.getBranchId().toString());
        jsonObject.addProperty("method", "genesis");

        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("frontier", branch.getOwner());
        param.addProperty("balance", "1000000000");
        params.add(param);
        jsonObject.add("params", params);

        JsonArray jsonArrayTxBody = new JsonArray();
        jsonArrayTxBody.add(jsonObject);

        return genesis(wallet, branch.getBranchId(), jsonArrayTxBody);
    }

    private BlockHusk genesis(Wallet wallet, BranchId branchId, JsonArray jsonArrayTxBody) {
        try {
            TransactionBody txBody = new TransactionBody(jsonArrayTxBody);

            TransactionHeader txHeader = new TransactionHeader(
                    branchId.getBytes(),
                    new byte[8],
                    new byte[8],
                    TimeUtils.time(),
                    txBody);

            Transaction tx = new Transaction(txHeader, wallet, txBody);
            List<Transaction> txList = new ArrayList<>();
            txList.add(tx);

            BlockBody blockBody = new BlockBody(txList);
            BlockHeader blockHeader = new BlockHeader(
                    branchId.getBytes(),
                    new byte[8],
                    new byte[8],
                    new byte[32],
                    0L,
                    0L,
                    blockBody.getMerkleRoot(),
                    blockBody.length());

            Block coreBlock = new Block(blockHeader, wallet, blockBody);

            return new BlockHusk(coreBlock.toProtoBlock());
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    private static Contract getContract(String branchName)
            throws IllegalAccessException, InstantiationException {
        if (Branch.STEM.equalsIgnoreCase(branchName)) {
            // replace StemContract
            ContractMeta stem = ContractClassLoader
                    .loadContractById("4fc0d50cba2f2538d6cda789aa4955e88c810ef5");
            assert stem != null;
            return stem.getContract().newInstance();
        } else if (Branch.YEED.equalsIgnoreCase(branchName)) {
            ContractMeta yeed = ContractClassLoader
                    .loadContractById("da2778112c033cdbaa3ca75616472c784a4d4410");
            assert yeed != null;
            return yeed.getContract().newInstance();
        } else {
            return new NoneContract();
        }
    }

    private static <T> Runtime<T> getRunTime(Class<T> clazz) {
        return new Runtime<>(new StateStore<>(), new TransactionReceiptStore());
    }

    public static BlockChainBuilder of(boolean isProduction) {
        return new BlockChainBuilder(isProduction);
    }

}
