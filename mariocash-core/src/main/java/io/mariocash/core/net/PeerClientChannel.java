package dev.zhihexireng.core.net;

import dev.zhihexireng.proto.BlockChainProto;
import dev.zhihexireng.proto.Pong;

import java.util.List;

public interface PeerClientChannel {

    Peer getPeer();

    void stop();

    void stop(String ynodeUri);

    Pong ping(String message);

    List<BlockChainProto.Block> syncBlock(long offset);

    List<BlockChainProto.Transaction> syncTransaction();

    void broadcastTransaction(BlockChainProto.Transaction[] txs);

    void broadcastBlock(BlockChainProto.Block[] blocks);

    List<String> requestPeerList(String ynodeUri, int limit);

    void disconnectPeer(String ynodeUri);
}
