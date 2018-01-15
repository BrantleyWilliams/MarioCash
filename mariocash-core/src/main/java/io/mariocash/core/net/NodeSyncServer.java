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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import dev.zhihexireng.proto.BlockChainGrpc;
import dev.zhihexireng.proto.BlockChainOuterClass;
import dev.zhihexireng.proto.Ping;
import dev.zhihexireng.proto.PingPongGrpc;
import dev.zhihexireng.proto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NodeSyncServer {
    private static final Logger log = LoggerFactory.getLogger(NodeSyncServer.class);

    private Server server;
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new PingPongImpl())
                .addService(new BlockChainImpl())
                .build()
                .start();
        log.info("GRPC Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may has been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            NodeSyncServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class PingPongImpl extends PingPongGrpc.PingPongImplBase {
        @Override
        public void play(Ping request, StreamObserver<Pong> responseObserver) {
            System.out.println(request.getPing());
            Pong pong = Pong.newBuilder().setPong("Pong").build();
            responseObserver.onNext(pong);
            responseObserver.onCompleted();
        }
    }

    static class BlockChainImpl extends BlockChainGrpc.BlockChainImplBase {
        @Override
        public StreamObserver<BlockChainOuterClass.Transaction> broadcast(
                StreamObserver<BlockChainOuterClass.Transaction> responseObserver) {
            return new StreamObserver<BlockChainOuterClass.Transaction>() {
                @Override
                public void onNext(BlockChainOuterClass.Transaction tx) {
                    System.out.println(tx);
                }

                @Override
                public void onError(Throwable t) {
                    log.warn("Broadcasting Failed: {}", t);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(BlockChainOuterClass.Transaction.newBuilder()
                            .setData("return").build());
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
