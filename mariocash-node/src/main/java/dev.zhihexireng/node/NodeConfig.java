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

package dev.zhihexireng.node;

import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.net.NodeSyncServer;
import dev.zhihexireng.core.net.PeerGroup;
import dev.zhihexireng.node.config.NodeProperties;
import dev.zhihexireng.node.mock.NodeManagerMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

@Configuration
public class NodeConfig {

    @Bean
    NodeProperties nodeProperties() {
        return new NodeProperties();
    }

    @Bean
    PeerGroup peerGroup(NodeProperties nodeProperties) {
        PeerGroup peerGroup = new PeerGroup();
        peerGroup.setSeedPeerList(nodeProperties.getSeedPeerList());
        return peerGroup;
    }

    @Bean
    MessageSender messageSender() {
        return new MessageSender();
    }

    @Bean
    NodeManager nodeManager(MessageSender messageSender, PeerGroup peerGroup,
                            NodeProperties nodeProperties) {
        return new NodeManagerMock(messageSender, peerGroup, nodeProperties.getGrpc());
    }

    @Bean
    NodeSyncServer nodeSyncServer(NodeManager nodeManager) {
        return new NodeSyncServer(nodeManager);
    }

    @Bean
    BeanNameUrlHandlerMapping beanNameUrlHandlerMapping() {
        return new BeanNameUrlHandlerMapping();
    }
}
