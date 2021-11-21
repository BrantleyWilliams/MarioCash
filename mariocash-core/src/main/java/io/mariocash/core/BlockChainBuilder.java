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

import dev.zhihexireng.core.contract.Contract;
import dev.zhihexireng.core.contract.ContractClassLoader;
import dev.zhihexireng.core.contract.ContractMeta;
import dev.zhihexireng.core.contract.Runtime;
import dev.zhihexireng.core.exception.InternalErrorException;
import dev.zhihexireng.core.genesis.GenesisBlock;
import dev.zhihexireng.core.store.BlockStore;
import dev.zhihexireng.core.store.MetaStore;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.StoreBuilder;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.core.store.TransactionStore;

public class BlockChainBuilder {

    private GenesisBlock genesis;
    private boolean productMode = false;

    public BlockChainBuilder addGenesis(GenesisBlock genesis) {
        this.genesis = genesis;
        return this;
    }

    public BlockChainBuilder setProductMode(boolean productMode) {
        this.productMode = productMode;
        return this;
    }

    public BlockChain build() {
        StoreBuilder storeBuilder = new StoreBuilder(this.productMode);

        BlockHusk genesisBlock = genesis.getBlock();
        BlockStore blockStore = storeBuilder.buildBlockStore(genesisBlock.getBranchId());
        TransactionStore txStore = storeBuilder.buildTxStore(genesisBlock.getBranchId());
        MetaStore metaStore = storeBuilder.buildMetaStore(genesisBlock.getBranchId());

        Contract contract = getContract();
        Runtime<?> runtime = getRunTime(contract.getClass().getGenericSuperclass().getClass());

        return new BlockChain(genesisBlock, blockStore, txStore, metaStore, contract, runtime);
    }

    private Contract getContract() {
        ContractMeta contractMeta = ContractClassLoader.loadContractById(genesis.getContractId());
        try {
            return contractMeta.getContract().newInstance();
        } catch (Exception e) {
            throw new InternalErrorException("Can't load contract id="
                    + genesis.getContractId(), e);
        }
    }

    private <T> Runtime<T> getRunTime(Class<T> clazz) {
        return new Runtime<>(new StateStore<>(), new TransactionReceiptStore());
    }

    public static BlockChainBuilder Builder() {
        return new BlockChainBuilder();
    }
}
