/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.node.config.annotaion;

import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BlockChainBuilder;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.Branch;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.net.PeerGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class DefaultBranchAutoConfig {
    private final boolean isProduction;

    @Value("classpath:/genesis-stem.json")
    Resource stemResource;

    @Value("classpath:/genesis-yeed.json")
    Resource yeedResource;

    DefaultBranchAutoConfig(Environment env) {
        this.isProduction = Arrays.asList(env.getActiveProfiles()).contains("prod");
    }

    @Bean(Branch.STEM)
    BlockChain stem(PeerGroup peerGroup, BranchGroup branchGroup) throws IOException,
            IllegalAccessException, InstantiationException {
        return addBranch(stemResource.getInputStream(),
                "4fc0d50cba2f2538d6cda789aa4955e88c810ef5", peerGroup, branchGroup);
    }

    @Bean(Branch.YEED)
    BlockChain yeed(PeerGroup peerGroup, BranchGroup branchGroup) throws IOException,
            IllegalAccessException, InstantiationException {
        return addBranch(yeedResource.getInputStream(),
                "da2778112c033cdbaa3ca75616472c784a4d4410", peerGroup, branchGroup);
    }

    private BlockChain addBranch(InputStream json, String contractId, PeerGroup peerGroup,
                                 BranchGroup branchGroup)
            throws IllegalAccessException, InstantiationException {
        BlockHusk genesis = Block.loadGenesis(json);

        BlockChainBuilder builder = BlockChainBuilder.Builder()
                .addGenesis(genesis)
                .addContractId(contractId);
        BlockChain branch = isProduction ? builder.buildForProduction() : builder.build();

        branchGroup.addBranch(branch.getBranchId(), branch, peerGroup);
        return branch;
    }
}
