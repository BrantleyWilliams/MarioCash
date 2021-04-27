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

import dev.zhihexireng.contract.Contract;
import dev.zhihexireng.contract.ContractClassLoader;
import dev.zhihexireng.contract.ContractMeta;
import dev.zhihexireng.core.store.BlockStore;
import dev.zhihexireng.core.store.MetaStore;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.StoreBuilder;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.core.store.TransactionStore;

public class BlockChainBuilder {

    private BlockHusk genesis;
    private String contractId;

    public BlockChainBuilder addGenesis(BlockHusk genesis) {
        this.genesis = genesis;
        return this;
    }

    // TODO get contractId from genesis
    public BlockChainBuilder addContractId(String contractId) {
        this.contractId = contractId;
        return this;
    }

    public BlockChain build() throws InstantiationException, IllegalAccessException {
        return buildIntenal(false);
    }

    public BlockChain buildForProduction() throws InstantiationException, IllegalAccessException {
        return buildIntenal(true);
    }

    private BlockChain buildIntenal(boolean isProduction) throws InstantiationException,
            IllegalAccessException {
        StoreBuilder storeBuilder = new StoreBuilder(isProduction);
        BlockStore blockStore = storeBuilder.buildBlockStore(genesis.getBranchId());
        TransactionStore txStore = storeBuilder.buildTxStore(genesis.getBranchId());
        MetaStore metaStore = storeBuilder.buildMetaStore(genesis.getBranchId());

        Contract contract = getContract();
        Runtime<?> runtime = getRunTime(contract.getClass().getGenericSuperclass().getClass());

        return new BlockChain(
                genesis, blockStore, txStore, metaStore, contract, runtime);
    }

    private Contract getContract()
            throws IllegalAccessException, InstantiationException {
        ContractMeta contractMeta = ContractClassLoader.loadContractById(contractId);
        return contractMeta.getContract().newInstance();
    }

    private <T> Runtime<T> getRunTime(Class<T> clazz) {
        return new Runtime<>(new StateStore<>(), new TransactionReceiptStore());
    }

    public static BlockChainBuilder Builder() {
        return new BlockChainBuilder();
    }
}
