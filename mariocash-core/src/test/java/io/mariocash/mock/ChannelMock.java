package dev.zhihexireng.mock;

import dev.zhihexireng.TestUtils;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerClientChannel;
import dev.zhihexireng.proto.Pong;
import dev.zhihexireng.proto.Proto;

import java.util.Collections;
import java.util.List;

public class ChannelMock implements PeerClientChannel {
    private final Peer peer;
    private final Pong pong = Pong.newBuilder().setPong("Pong").build();
    private boolean pongResponse = true;

    public ChannelMock(String ynodeUri) {
        this.peer = Peer.valueOf(ynodeUri);
    }

    @Override
    public Peer getPeer() {
        return peer;
    }

    @Override
    public void stop() {
    }

    @Override
    public Pong ping(String message) {
        if (pongResponse) {
            pongResponse = false;
            return pong;
        }
        pongResponse = true;
        return null;
    }

    @Override
    public List<Proto.Block> syncBlock(BranchId branchId, long offset) {
        return Collections.singletonList(TestUtils.sampleBlock().toProtoBlock());
    }

    @Override
    public List<Proto.Transaction> syncTransaction(BranchId branchId) {
        Proto.Transaction protoTx = Transaction.toProtoTransaction(TestUtils.sampleTransferTx());
        return Collections.singletonList(protoTx);
    }

    @Override
    public void broadcastTransaction(Proto.Transaction[] txs) {
    }

    @Override
    public void broadcastBlock(Proto.Block[] blocks) {
    }

    public static PeerClientChannel dummy() {
        return new ChannelMock("ynode://75bff16c@localhost:32918");
    }
}
