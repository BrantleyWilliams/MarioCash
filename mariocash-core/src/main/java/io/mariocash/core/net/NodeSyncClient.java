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

import com.google.common.annotations.VisibleForTesting;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import dev.zhihexireng.proto.BlockChainGrpc;
import dev.zhihexireng.proto.BlockChainProto;
import dev.zhihexireng.proto.BlockChainProto.Empty;
import dev.zhihexireng.proto.BlockChainProto.SyncLimit;
import dev.zhihexireng.proto.Ping;
import dev.zhihexireng.proto.PingPongGrpc;
import dev.zhihexireng.proto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NodeSyncClient {

    public static final Logger log = LoggerFactory.getLogger(NodeSyncClient.class);

    private final ManagedChannel channel;
    private final PingPongGrpc.PingPongBlockingStub blockingStub;
    private final BlockChainGrpc.BlockChainStub asyncStub;
    private final Peer peer;

    public NodeSyncClient(Peer peer) {
        this(ManagedChannelBuilder.forAddress(peer.getHost(), peer.getPort()).usePlaintext()
                .build(), peer);
    }

    @VisibleForTesting
    NodeSyncClient(ManagedChannel channel, Peer peer) {
        this.channel = channel;
        this.peer = peer;
        blockingStub = PingPongGrpc.newBlockingStub(channel);
        asyncStub = BlockChainGrpc.newStub(channel);
    }

    String getPeerYnodeUri() {
        return peer.getYnodeUri();
    }

    public void stop() {
        log.debug("stop for peer=" + peer.getYnodeUri());
        if (channel != null) {
            channel.shutdown();
        }
    }

    public void stop(String ynodeUri) {
        disconnectPeer(ynodeUri);
        stop();
    }

    void blockUtilShutdown() throws InterruptedException {
        if (channel != null) {
            channel.awaitTermination(5, TimeUnit.MINUTES);
        }
    }

    public Pong ping(String message) {
        Ping request = Ping.newBuilder().setPing(message).build();
        return blockingStub.play(request);
    }

    /**
     * Sync block request
     *
     * @param offset the start block index to sync
     * @return the block list
     */
    public List<BlockChainProto.Block> syncBlock(long offset) {
        SyncLimit syncLimit = SyncLimit.newBuilder().setOffset(offset).build();
        return BlockChainGrpc.newBlockingStub(channel).syncBlock(syncLimit).getBlocksList();
    }

    /**
     * Sync transaction request
     *
     * @return the transaction list
     */
    public List<BlockChainProto.Transaction> syncTransaction() {
        Empty empty = Empty.newBuilder().build();
        return BlockChainGrpc.newBlockingStub(channel).syncTransaction(empty).getTransactionsList();
    }

    public void broadcastTransaction(BlockChainProto.Transaction[] txs) {
        log.info("*** Broadcasting tx...");
        StreamObserver<BlockChainProto.Transaction> requestObserver =
                asyncStub.broadcastTransaction(new StreamObserver<BlockChainProto.Transaction>() {
                    @Override
                    public void onNext(BlockChainProto.Transaction tx) {
                        log.trace("Got transaction: {}", tx);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.warn("Broadcast transaction failed: {}",
                                Status.fromThrowable(t).getCode());
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Finished broadcasting");
                    }
                });

        for (BlockChainProto.Transaction tx : txs) {
            log.trace("Sending transaction: {}", tx);
            requestObserver.onNext(tx);
        }

        requestObserver.onCompleted();
    }

    public void broadcastBlock(BlockChainProto.Block[] blocks) {
        log.info("*** Broadcasting blocks...");
        StreamObserver<BlockChainProto.Block> requestObserver =
                asyncStub.broadcastBlock(new StreamObserver<BlockChainProto.Block>() {
                    @Override
                    public void onNext(BlockChainProto.Block block) {
                        log.trace("Got block: {}", block);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.warn("Broadcast block failed: {}", Status.fromThrowable(t).getCode());
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Finished broadcasting");
                    }
                });

        for (BlockChainProto.Block block : blocks) {
            log.trace("Sending block: {}", block);
            requestObserver.onNext(block);
        }

        requestObserver.onCompleted();
    }

    public List<String> requestPeerList(String ynodeUri, int limit) {
        if (ynodeUri.equals(peer.getYnodeUri())) {
            log.debug("ignore from me");
            return Collections.emptyList();
        }
        BlockChainProto.PeerRequest request = BlockChainProto.PeerRequest.newBuilder()
                .setFrom(ynodeUri).setLimit(limit).build();
        return BlockChainGrpc.newBlockingStub(channel).requestPeerList(request).getPeersList();
    }

    public void disconnectPeer(String ynodeUri) {
        if (ynodeUri.equals(peer.getYnodeUri())) {
            log.debug("ignore from me");
            return;
        }
        log.info("Disconnect request peer=" + ynodeUri);
        BlockChainProto.PeerRequest request = BlockChainProto.PeerRequest.newBuilder()
                .setFrom(ynodeUri).build();
        BlockChainGrpc.newBlockingStub(channel).disconnectPeer(request);
    }
}
