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
import org.springframework.context.annotation.Profile;

@Configuration
public class StoreConfiguration {

    @Bean
    BranchGroup branchGroup(Runtime runtime) {
        return new BranchGroup(runtime);
    }

    @Bean
    Runtime runTime(StateStore stateStore, TransactionReceiptStore transactionReceiptStore) {
        return new Runtime(stateStore, transactionReceiptStore);
    }

    @Bean
    StateStore stateStore() {
        return new StateStore();
    }

    @Bean
    TransactionReceiptStore transactionReceiptStore() {
        return new TransactionReceiptStore();
    }

    @Bean
    BlockStore blockStore(@Qualifier("blockDbSource") DbSource source) {
        return new BlockStore(source);
    }

    @Bean
    TransactionStore transactionStore(@Qualifier("txDbSource") DbSource source) {
        return new TransactionStore(source);
    }

    @Profile({"default", "ci"})
    @Bean(name = "blockDbSource")
    DbSource blockDbSource() {
        return new HashMapDbSource();
    }

    @Profile({"default", "ci"})
    @Bean(name = "txDbSource")
    DbSource txDbSource() {
        return new HashMapDbSource();
    }
}
