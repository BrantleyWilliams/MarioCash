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

import com.google.gson.JsonObject;
import io.grpc.internal.testing.StreamRecorder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcServerRule;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.husk.BlockHusk;
import dev.zhihexireng.core.husk.TransactionHusk;
import dev.zhihexireng.core.net.NodeSyncServer.BlockChainImpl;
import dev.zhihexireng.core.net.NodeSyncServer.PingPongImpl;
import dev.zhihexireng.proto.BlockChainGrpc;
import dev.zhihexireng.proto.NetProto;
import dev.zhihexireng.proto.Ping;
import dev.zhihexireng.proto.PingPongGrpc;
import dev.zhihexireng.proto.Pong;
import dev.zhihexireng.proto.Proto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeSyncServerTest {

    @Rule
    public final GrpcServerRule grpcServerRule = new GrpcServerRule().directExecutor();
    @Mock
    private NodeManager nodeManagerMock;
    private TransactionHusk tx;
    private BlockHusk block;

    @Before
    public void setUp() throws Exception {
        grpcServerRule.getServiceRegistry().addService(new PingPongImpl());
        grpcServerRule.getServiceRegistry().addService(new BlockChainImpl(nodeManagerMock));

        Wallet wallet = new Wallet();
        JsonObject json = new JsonObject();
        json.addProperty("data", "TEST");
        this.tx = new TransactionHusk(json).sign(wallet);
        when(nodeManagerMock.addTransaction(any())).thenReturn(tx);
        this.block = BlockHusk.genesis(wallet, json);
        when(nodeManagerMock.addBlock(any())).thenReturn(block);
    }

    @Test
    public void play() {
        PingPongGrpc.PingPongBlockingStub blockingStub = PingPongGrpc.newBlockingStub(
                grpcServerRule.getChannel());

        Pong pong = blockingStub.play(Ping.newBuilder().setPing("Ping").build());
        assertEquals("Pong", pong.getPong());
    }

    @Test
    public void requestPeerList() {
        when(nodeManagerMock.getPeerUriList()).thenReturn(Arrays.asList("a", "b", "c"));

        BlockChainGrpc.BlockChainBlockingStub blockingStub
                = BlockChainGrpc.newBlockingStub(grpcServerRule.getChannel());
        String ynodeUri = "ynode://75bff16c@localhost:9090";
        NetProto.PeerRequest.Builder builder
                = NetProto.PeerRequest.newBuilder().setFrom(ynodeUri);
        NetProto.PeerList response = blockingStub.requestPeerList(builder.build());
        assertEquals(3, response.getPeersCount());
        // limit test
        response = blockingStub.requestPeerList(builder.setLimit(2).build());
        assertEquals(2, response.getPeersCount());
    }

    @Test
    public void syncBlock() {
        Set<BlockHusk> blocks = new HashSet<>();
        blocks.add(block);
        when(nodeManagerMock.getBlocks()).thenReturn(blocks);

        BlockChainGrpc.BlockChainBlockingStub blockingStub
                = BlockChainGrpc.newBlockingStub(grpcServerRule.getChannel());
        NetProto.SyncLimit syncLimit
                = NetProto.SyncLimit.newBuilder().setOffset(0).build();
        Proto.BlockList list = blockingStub.syncBlock(syncLimit);
        assertEquals(1, list.getBlocksCount());
    }

    @Test
    public void syncTransaction() {
        when(nodeManagerMock.getTransactionList()).thenReturn(Collections.singletonList(tx));

        BlockChainGrpc.BlockChainBlockingStub blockingStub
                = BlockChainGrpc.newBlockingStub(grpcServerRule.getChannel());
        NetProto.Empty empty = NetProto.Empty.getDefaultInstance();
        Proto.TransactionList list = blockingStub.syncTransaction(empty);
        assertEquals(1, list.getTransactionsCount());
    }

    @Test
    public void broadcastTransaction() throws Exception {
        BlockChainGrpc.BlockChainStub stub = BlockChainGrpc.newStub(grpcServerRule.getChannel());
        StreamRecorder<NetProto.Empty> responseObserver = StreamRecorder.create();
        StreamObserver<Proto.Transaction> requestObserver
                = stub.broadcastTransaction(responseObserver);

        requestObserver.onNext(tx.getInstance());
        requestObserver.onCompleted();
        assertNotNull(responseObserver.firstValue().get());
    }

    @Test
    public void broadcastBlock() throws Exception {
        BlockChainGrpc.BlockChainStub stub = BlockChainGrpc.newStub(grpcServerRule.getChannel());
        StreamRecorder<NetProto.Empty> responseObserver = StreamRecorder.create();
        StreamObserver<Proto.Block> requestObserver
                = stub.broadcastBlock(responseObserver);

        requestObserver.onNext(block.getInstance());
        requestObserver.onCompleted();

        NetProto.Empty firstResponse = responseObserver.firstValue().get();
        assertNotNull(firstResponse);
    }
}
