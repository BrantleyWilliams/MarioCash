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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.contract.ContractEvent;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BlockChainBuilder;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.Branch;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionReceipt;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.event.ContractEventListener;
import dev.zhihexireng.core.net.PeerGroup;
import dev.zhihexireng.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BranchConfiguration implements ContractEventListener {
    private static final Logger log = LoggerFactory.getLogger(BranchConfiguration.class);

    private final BlockChainBuilder builder;
    private final Wallet wallet;
    private final PeerGroup peerGroup;

    private BranchGroup branchGroup;

    @Value("classpath:/genesis.json")
    private Resource resource;

    void setResource(Resource resource) {
        this.resource = resource;
    }

    BranchConfiguration(Environment env, Wallet wallet, PeerGroup peerGroup) {
        boolean isProduction = Arrays.asList(env.getActiveProfiles()).contains("prod");
        this.builder = new BlockChainBuilder(isProduction);
        this.wallet = wallet;
        this.peerGroup = peerGroup;
    }

    @Bean
    BranchGroup branchGroup() throws IOException, IllegalAccessException, InstantiationException {
        this.branchGroup = new BranchGroup();
        BlockHusk genesis = Block.loadGenesis(resource.getInputStream());
        BlockChain stem = builder.build(genesis, Branch.STEM);
        branchGroup.addBranch(stem.getBranchId(), stem, peerGroup, this);
        return branchGroup;
    }

    @Override
    public void onContractEvent(ContractEvent event) {
        TransactionReceipt txReceipt = event.getTransactionReceipt();
        TransactionHusk txHusk = event.getTransactionHusk();
        if (!txReceipt.isSuccess() || !txHusk.getBranchId().equals(BranchId.stem())) {
            return;
        }

        JsonObject txBody = Utils.parseJsonArray(txHusk.getBody()).get(0).getAsJsonObject();
        JsonArray params = txBody.get("params").getAsJsonArray();

        for (int i = 0; i < params.size(); i++) {
            String branchId = params.get(i).getAsJsonObject().get("branchId").getAsString();
            if (containsBranch(branchId)) {
                continue;
            }
            try {
                Map branchMap = (HashMap)txReceipt.getLog(branchId);
                String reserveAddress = String.valueOf(branchMap.get("reserve_address"));
                if (!Arrays.equals(Hex.decode(reserveAddress), wallet.getAddress())) {
                    log.warn("Ignore branch creation. branch={}, reserveAddress={}", branchId,
                            reserveAddress);
                    continue;
                }
                String branchName = String.valueOf(branchMap.get("name"));
                String owner = String.valueOf(branchMap.get("owner"));
                Branch branch = Branch.of(branchId, branchName, owner);
                BlockChain blockChain = builder.build(wallet, branch);
                branchGroup.addBranch(blockChain.getBranchId(), blockChain, peerGroup, this);
                log.info("New branch created. id={}, name={}, genesis={}", blockChain.getBranchId(),
                        blockChain.getBranchName(), blockChain.getPrevBlock().getHash());
            } catch (Exception e) {
                log.warn("Add branch fail. id={}, err={}", branchId, e.getMessage());
            }
        }
    }

    private boolean containsBranch(String branchId) {
        return branchGroup.getBranch(BranchId.of(branchId)) != null;
    }
}
