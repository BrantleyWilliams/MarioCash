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
import dev.zhihexireng.core.Account;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.mapper.BlockMapper;
import dev.zhihexireng.core.mapper.TransactionMapper;
import dev.zhihexireng.proto.BlockChainGrpc;
import dev.zhihexireng.proto.BlockChainProto;
import dev.zhihexireng.proto.Ping;
import dev.zhihexireng.proto.PingPongGrpc;
import dev.zhihexireng.proto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NodeSyncServer {
    private static final Logger log = LoggerFactory.getLogger(NodeSyncServer.class);
    private static final String PEER_FORMAT = "%s://%s@%s:%d";
    private NodeManager nodeManager;
    private Server server;
    private String host;
    private int port;

    public NodeSyncServer() {
    }

    public NodeSyncServer(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new PingPongImpl())
                .addService(new BlockChainImpl(nodeManager))
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
     * Init peer.
     */
    public void initPeer() {
        // my peer
        String pubKey = Hex.toHexString(new Account().getKey().getPubKey());
        Peer peer = new Peer(String.format(PEER_FORMAT, Peer.YEED_PEER_SCHEMA, pubKey, host, port));
        nodeManager.addPeer(peer);
        // TODO add a static temporary peer
        String otherKey = Hex.toHexString(new Account().getKey().getPubKey());
        Peer otherPeer = new Peer(String.format(PEER_FORMAT, Peer.YEED_PEER_SCHEMA, otherKey, host,
                port));
        nodeManager.addPeer(otherPeer);
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
            log.debug(request.getPing());
            Pong pong = Pong.newBuilder().setPong("Pong").build();
            responseObserver.onNext(pong);
            responseObserver.onCompleted();
        }
    }

    /**
     * The block chain rpc server implementation.
     */
    static class BlockChainImpl extends BlockChainGrpc.BlockChainImplBase {
        private NodeManager nodeManager;

        BlockChainImpl(NodeManager nodeManager) {
            this.nodeManager = nodeManager;
        }

        private static Set<StreamObserver<BlockChainProto.Transaction>> txObservers =
                ConcurrentHashMap.newKeySet();

        private static Set<StreamObserver<BlockChainProto.Block>> blockObservers =
                ConcurrentHashMap.newKeySet();

        /**
         * Sync block response
         *
         * @param syncLimit the start block index and limit to sync
         * @param responseObserver the observer response to the block list
         */
        @Override
        public void syncBlock(BlockChainProto.SyncLimit syncLimit,
                              StreamObserver<BlockChainProto.BlockList> responseObserver) {
            long offset = syncLimit.getOffset();
            long limit = syncLimit.getLimit();
            log.debug("Synchronize block request offset={}, limit={}", offset, limit);

            BlockChainProto.BlockList.Builder builder = BlockChainProto.BlockList.newBuilder();
            for (Block block : nodeManager.getBlocks()) {
                if (block.getIndex() >= offset) {
                    builder.addBlocks(BlockMapper.blockToProtoBlock(block));
                }
                if (limit > 0 && builder.getBlocksCount() > limit) {
                    break;
                }
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }

        /**
         * Sync block response
         *
         * @param empty the empty message
         * @param responseObserver the observer response to the transaction list
         */
        @Override
        public void syncTransaction(BlockChainProto.Empty empty,
                              StreamObserver<BlockChainProto.TransactionList> responseObserver) {
            log.debug("Synchronize tx request");
            BlockChainProto.TransactionList.Builder builder
                    = BlockChainProto.TransactionList.newBuilder();
            for (Transaction tx : nodeManager.getTransactionList()) {
                builder.addTransactions(TransactionMapper.transactionToProtoTransaction(tx));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<BlockChainProto.Transaction> broadcastTransaction(
                StreamObserver<BlockChainProto.Transaction> responseObserver) {

            txObservers.add(responseObserver);

            return new StreamObserver<BlockChainProto.Transaction>() {
                @Override
                public void onNext(BlockChainProto.Transaction tx) {
                    log.debug("Received transaction: {}", tx);
                    Transaction newTransaction = null;
                    if (nodeManager != null) {
                        try {
                            Transaction transaction
                                    = TransactionMapper.protoTransactionToTransaction(tx);
                            newTransaction = nodeManager.addTransaction(transaction);
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                        // ignore broadcast by other node's broadcast
                        if (newTransaction == null) {
                            return;
                        }
                    }

                    for (StreamObserver<BlockChainProto.Transaction> observer : txObservers) {
                        observer.onNext(tx);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    log.warn("Broadcasting transaction failed: {}", t.getMessage());
                    txObservers.remove(responseObserver);
                    responseObserver.onError(t);
                }

                @Override
                public void onCompleted() {
                    txObservers.remove(responseObserver);
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<BlockChainProto.Block> broadcastBlock(
                StreamObserver<BlockChainProto.Block> responseObserver) {

            blockObservers.add(responseObserver);

            return new StreamObserver<BlockChainProto.Block>() {
                @Override
                public void onNext(BlockChainProto.Block protoBlock) {
                    log.debug("Received block id=[{}]", protoBlock.getHeader().getIndex());
                    Block newBlock = null;
                    if (nodeManager != null) {
                        try {
                            Block block = BlockMapper.protoBlockToBlock(protoBlock);
                            log.debug("Received block hash=" + block.getBlockHash());
                            newBlock = nodeManager.addBlock(block);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                        // ignore broadcast by other node's broadcast
                        if (newBlock == null) {
                            return;
                        }
                    }

                    for (StreamObserver<BlockChainProto.Block> observer : blockObservers) {
                        observer.onNext(protoBlock);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    log.warn("Broadcasting block failed: {}", t.getMessage());
                    blockObservers.remove(responseObserver);
                    responseObserver.onError(t);
                }

                @Override
                public void onCompleted() {
                    blockObservers.remove(responseObserver);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
