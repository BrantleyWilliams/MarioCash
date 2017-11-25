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

package dev.zhihexireng.core.net;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.proto.BlockChainGrpc;
import dev.zhihexireng.proto.BlockChainOuterClass;
import dev.zhihexireng.proto.Ping;
import dev.zhihexireng.proto.PingPongGrpc;
import dev.zhihexireng.proto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeSyncClient {
    public static final Logger log = LoggerFactory.getLogger(NodeSyncClient.class);

    private final ManagedChannel channel;
    private final PingPongGrpc.PingPongBlockingStub blockingStub;
    private final BlockChainGrpc.BlockChainStub asyncStub;

    public NodeSyncClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }

    NodeSyncClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = PingPongGrpc.newBlockingStub(channel);
        asyncStub = BlockChainGrpc.newStub(channel);
    }

    public void ping(String message) {
        Ping request = Ping.newBuilder().setPing(message).build();
        try {
            Pong pong = blockingStub.play(request);
            log.debug(pong.getPong());
        } catch (Exception e) {
            System.out.println("retrying...");
        }
    }

    public void broadcast(Transaction[] txs) {
        log.info("*** Broadcasting...");
        StreamObserver<BlockChainOuterClass.Transaction> requestObserver =
                asyncStub.broadcast(new StreamObserver<BlockChainOuterClass.Transaction>() {
                    @Override
                    public void onNext(BlockChainOuterClass.Transaction tx) {
                        log.info("Got transaction");
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.warn("broadcast Failed: {}", Status.fromThrowable(t));
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Finished Broadcasting");
                    }
                });

        BlockChainOuterClass.Transaction[] requests = {
                BlockChainOuterClass.Transaction.newBuilder().setData("Tx1").build(),
                BlockChainOuterClass.Transaction.newBuilder().setData("Tx2").build(),
                BlockChainOuterClass.Transaction.newBuilder().setData("Tx3").build(),
                BlockChainOuterClass.Transaction.newBuilder().setData("Tx4").build()
        };

        for (BlockChainOuterClass.Transaction request : requests) {
            requestObserver.onNext(request);
        }

        requestObserver.onCompleted();
    }
}
