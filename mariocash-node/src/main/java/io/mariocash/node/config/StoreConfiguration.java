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

package dev.zhihexireng.node.config;

import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.Runtime;
import dev.zhihexireng.core.store.BlockStore;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.core.store.TransactionStore;
import dev.zhihexireng.core.store.datasource.DbSource;
import dev.zhihexireng.core.store.datasource.HashMapDbSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreConfiguration {

    @Bean
    BranchGroup branchGroup(Runtime runtime, @Qualifier("yeedBranch")BlockChain blockChain) {
        BranchGroup branchGroup = new BranchGroup(runtime);
        branchGroup.addBranch(blockChain.getBranchId(), blockChain);
        return branchGroup;
    }

    @Bean
    TransactionReceiptStore transactionReceiptStore() {
        return new TransactionReceiptStore();
    }

    @Bean
    Runtime runTime(TransactionReceiptStore transactionReceiptStore) {
        return new Runtime(transactionReceiptStore);
    }

    @Bean
    BlockStore blockStore(@Qualifier("blockDbSource") DbSource source) {
        return new BlockStore(source);
    }

    @Bean
    TransactionStore transactionStore(@Qualifier("txDbSource") DbSource source) {
        return new TransactionStore(source);
    }

    @Bean(name = "blockDbSource")
    DbSource blockDbSource() {
        return new HashMapDbSource();
    }

    @Bean(name = "txDbSource")
    DbSource txDbSource() {
        return new HashMapDbSource();
    }

    @Bean
    StateStore stateStore() {
        return new StateStore();
    }
}
