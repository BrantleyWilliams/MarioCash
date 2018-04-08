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

import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeEventListener;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.net.NodeSyncClient;
import dev.zhihexireng.proto.BlockChainProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MessageSender implements DisposableBean, NodeEventListener {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    @Value("${grpc.port}")
    private int grpcPort;

    private NodeSyncClient nodeSyncClient;

    @PostConstruct
    public void init() {
        int port = grpcPort == 9090 ? 9091 : 9090;
        log.info("Connecting gRPC Server at [{}]", port);
        nodeSyncClient = new NodeSyncClient("localhost", port);
    }

    public void ping() {
        nodeSyncClient.ping("Ping");
    }

    @Override
    public void destroy() {
        nodeSyncClient.stop();
    }

    @Override
    public void newTransaction(Transaction tx) {
        log.debug("New transaction={}", tx);
        nodeSyncClient.broadcastTransaction(new BlockChainProto.Transaction[] {Transaction.of(tx)});
    }

    @Override
    public void newBlock(Block block) {
        log.debug("New block={}", block);
        //nodeSyncClient.broadcastBlock(new BlockChainProto.Block[] {Block.of(block)});
    }
}
