package dev.zhihexireng.core.net;

import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.proto.NodeInfo;
import dev.zhihexireng.proto.Pong;
import dev.zhihexireng.proto.Proto;

import java.util.List;

public interface PeerClientChannel {
    List<NodeInfo> findPeers(BranchId branchId, Peer peer);

    Peer getPeer();

    void stop();

    Pong ping(String message);

    List<Proto.Block> syncBlock(BranchId branchId, long offset);

    List<Proto.Transaction> syncTransaction(BranchId branchId);

    void broadcastTransaction(Proto.Transaction[] txs);

    void broadcastBlock(Proto.Block[] blocks);
}
