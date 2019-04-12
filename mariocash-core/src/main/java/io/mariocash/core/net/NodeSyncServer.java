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

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NodeSyncServer {
    private static final Logger log = LoggerFactory.getLogger(NodeSyncServer.class);
    private final NodeManager nodeManager;
    private Server server;

    public NodeSyncServer(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void start(int port) throws IOException {
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
        nodeManager.init();
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
            log.debug("request=" + request.getPing());
            Pong pong = Pong.newBuilder().setPong("Pong").build();
            responseObserver.onNext(pong);
            responseObserver.onCompleted();
        }
    }

    /**
     * The block chain rpc server implementation.
     */
    static class BlockChainImpl extends BlockChainGrpc.BlockChainImplBase {
        private static final Set<StreamObserver<BlockChainProto.Transaction>> txObservers =
                ConcurrentHashMap.newKeySet();
        private static final Set<StreamObserver<BlockChainProto.Block>> blockObservers =
                ConcurrentHashMap.newKeySet();
        private final NodeManager nodeManager;

        BlockChainImpl(NodeManager nodeManager) {
            this.nodeManager = nodeManager;
        }

        /**
         * Sync block response
         *
         * @param syncLimit        the start block index and limit to sync
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
         * Sync transaction response
         *
         * @param empty            the empty message
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

        /**
         * Peer list response
         *
         * @param peerRequest      the request with limit of peer and peer uri
         * @param responseObserver the observer response to the peer list
         */
        @Override
        public void requestPeerList(BlockChainProto.PeerRequest peerRequest,
                                    StreamObserver<BlockChainProto.PeerList> responseObserver) {
            log.debug("Synchronize peer request from=" + peerRequest.getFrom());
            BlockChainProto.PeerList.Builder builder = BlockChainProto.PeerList.newBuilder();

            List<String> peerUriList = nodeManager.getPeerUriList();

            if (peerRequest.getLimit() > 0) {
                int limit = peerRequest.getLimit();
                builder.addAllPeers(peerUriList.stream().limit(limit).collect(Collectors.toList()));
            } else {
                builder.addAllPeers(peerUriList);
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            nodeManager.addPeer(peerRequest.getFrom());
        }

        /**
         * Broadcast a disconnected peer
         *
         * @param peerRequest      the request with disconnected peer uri
         * @param responseObserver the empty response
         */
        @Override
        public void disconnectPeer(BlockChainProto.PeerRequest peerRequest,
                                   StreamObserver<BlockChainProto.Empty> responseObserver) {
            log.debug("Received disconnect for=" + peerRequest.getFrom());
            responseObserver.onNext(BlockChainProto.Empty.newBuilder().build());
            responseObserver.onCompleted();
            nodeManager.removePeer(peerRequest.getFrom());
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
                    try {
                        Transaction transaction
                                = TransactionMapper.protoTransactionToTransaction(tx);
                        newTransaction = nodeManager.addTransaction(transaction);
                    } catch (SignatureException | IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    // ignore broadcast by other node's broadcast
                    if (newTransaction == null) {
                        return;
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
                    long id = protoBlock.getHeader().getIndex();
                    Block newBlock = null;
                    try {
                        Block block = BlockMapper.protoBlockToBlock(protoBlock);
                        log.debug("Received block id=[{}], hash={}", id, block.getBlockHash());
                        newBlock = nodeManager.addBlock(block);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    // ignore broadcast by other node's broadcast
                    if (newBlock == null) {
                        return;
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
