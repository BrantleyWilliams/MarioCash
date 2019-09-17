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

import dev.zhihexireng.core.BlockBuilder;
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.TransactionManager;
import dev.zhihexireng.core.TransactionValidator;
import dev.zhihexireng.core.store.TransactionPool;
import dev.zhihexireng.core.store.datasource.DbSource;
import dev.zhihexireng.node.BlockBuilderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockChainConfiguration {

    @Bean
    BlockChain blockChain() {
        return new BlockChain();
    }

    @Bean
    BlockBuilder blockBuilder() {
        return new BlockBuilderImpl();
    }

    @Bean
    TransactionValidator transactionValidator() {
        return new TransactionValidator();
    }

    @Bean
    TransactionManager transactionManager(DbSource db, TransactionPool txPool) {
        return new TransactionManager(db, txPool);
    }
}