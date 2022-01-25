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

import dev.zhihexireng.common.config.DefaultConfig;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.account.Wallet;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerGroup;
import dev.zhihexireng.node.config.annotaion.EnableDefaultBranch;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableDefaultBranch
public class NodeConfiguration {

    private final NodeProperties nodeProperties;

    @Autowired
    NodeConfiguration(NodeProperties nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    @Bean
    PeerGroup peerGroup(Wallet wallet) {
        Peer owner = Peer.valueOf(wallet.getNodeId(), nodeProperties.getGrpc().getHost(),
                nodeProperties.getGrpc().getPort());
        PeerGroup peerGroup = new PeerGroup(owner, nodeProperties.getMaxPeers());
        peerGroup.setSeedPeerList(nodeProperties.getSeedPeerList());
        return peerGroup;
    }

    @Bean
    BranchGroup branchGroup() {
        return new BranchGroup();
    }

    @Bean
    DefaultConfig defaultConfig() {
        return new DefaultConfig();
    }

    @Bean
    Wallet wallet(DefaultConfig defaultConfig) throws IOException, InvalidCipherTextException {
        return new Wallet(defaultConfig);
    }
}
