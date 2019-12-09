package dev.zhihexireng.core.net;

import dev.zhihexireng.proto.Pong;
import dev.zhihexireng.proto.Proto;

import java.util.List;

public interface PeerClientChannel {

    Peer getPeer();

    void stop();

    void stop(String ynodeUri);

    Pong ping(String message);

    List<Proto.Block> syncBlock(long offset);

    List<Proto.Transaction> syncTransaction();

    void broadcastTransaction(Proto.Transaction[] txs);

    void broadcastBlock(Proto.Block[] blocks);

    List<String> requestPeerList(String ynodeUri, int limit);

    void disconnectPeer(String ynodeUri);
}
