package dev.zhihexireng.core.net;

import dev.zhihexireng.TestUtils;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.store.BlockStore;
import dev.zhihexireng.mock.ChannelMock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class PeerGroupTest {
    private static final Logger log = LoggerFactory.getLogger(BlockStore.class);

    private static final int MAX_PEERS = 25;

    private PeerGroup peerGroup;
    private TransactionHusk tx;
    private BlockHusk block;

    @Before
    public void setUp() {
        this.peerGroup = new PeerGroup(MAX_PEERS);
        this.tx = TestUtils.createTxHusk();
        this.block = TestUtils.createGenesisBlockHusk();
        peerGroup.setListener(peer -> log.debug(peer.getYnodeUri() + " disconnected"));
        ChannelMock channel = new ChannelMock("ynode://75bff16c@localhost:9999");
        peerGroup.newPeerChannel(channel);
    }

    @Test
    public void addPeerTest() {
        assert peerGroup.isEmpty();
        peerGroup.addPeer(Peer.valueOf("ynode://75bff16c@127.0.0.1:9090"));
        assert peerGroup.count() == 1;
        assert !peerGroup.getPeers().isEmpty();
        assert !peerGroup.isEmpty();
        peerGroup.clear();
        assert peerGroup.isEmpty();
    }

    @Test
    public void removePeerTest() {
        peerGroup.addPeer(Peer.valueOf("ynode://75bff16c@127.0.0.1:9090"));
        assert peerGroup.contains("ynode://75bff16c@127.0.0.1:9090");
        assert !peerGroup.contains("wrong");
    }

    @Test
    public void getSeedPeerList() {
        assert peerGroup.getSeedPeerList() == null;
        peerGroup.setSeedPeerList(Collections.singletonList("ynode://75bff16c@127.0.0.1:9090"));
        assert !peerGroup.getSeedPeerList().isEmpty();
    }

    @Test
    public void getPeerUriListTest() {
        assert peerGroup.getPeerUriList().isEmpty();
        peerGroup.addPeer(Peer.valueOf("ynode://75bff16c@127.0.0.1:9090"));
        assert peerGroup.getPeerUriList().contains("ynode://75bff16c@127.0.0.1:9090");
    }

    @Test
    public void healthCheck() {
        peerGroup.healthCheck();
        assert !peerGroup.getActivePeerList().isEmpty();
    }

    @Test
    public void syncBlock() {
        peerGroup.chainedBlock(block);
        assert !peerGroup.syncBlock(0).isEmpty();
    }

    @Test
    public void syncTransaction() {
        peerGroup.newTransaction(tx);
        assert !peerGroup.syncTransaction().isEmpty();
    }

    @Test
    public void addActivePeer() {
        int testCount = MAX_PEERS + 5;
        for (int i = 0; i < testCount; i++) {
            int port = i + 9000;
            ChannelMock channel = new ChannelMock("ynode://75bff16c@localhost:" + port);
            peerGroup.newPeerChannel(channel);
        }
        assert MAX_PEERS == peerGroup.getActivePeerList().size();
    }

    @Test
    public void broadcastPeerConnect() {
        assert !peerGroup.broadcastPeerConnect("ynode://75bff16c@localhost:9999").isEmpty();
    }

    @Test
    public void broadcastPeerDisconnect() {
        assert !peerGroup.getActivePeerList().isEmpty();
        peerGroup.broadcastPeerDisconnect("ynode://75bff16c@localhost:9999");
        assert peerGroup.getActivePeerList().isEmpty();
    }
}
