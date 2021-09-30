package dev.zhihexireng.core.net;

import dev.zhihexireng.TestUtils;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.store.BlockStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PeerGroupTest {
    private static final Logger log = LoggerFactory.getLogger(BlockStore.class);

    private static final int MAX_PEERS = 25;
    private static final BranchId BRANCH = BranchId.stem();
    private static final BranchId OTHER_BRANCH = BranchId.yeed();
    private static final Peer OWNER = Peer.valueOf("ynode://75bff16c@127.0.0.1:32920");

    private PeerGroup peerGroup;
    private TransactionHusk tx;

    @Mock
    private DiscoveryClient discoveryClient;

    @Before
    public void setUp() {
        this.peerGroup = new PeerGroup(OWNER, MAX_PEERS);
        this.tx = TestUtils.createTransferTxHusk();
        peerGroup.addPeerTable(BRANCH, false);
        peerGroup.addPeerTable(OTHER_BRANCH, false);
    }

    @Test
    public void bootstrappingTest() {
        // SeedPeerList 가 아닌 peerTables 세팅 후 bootstrapping
        Peer p1 = Peer.valueOf("ynode://75bff16c@127.0.0.1:32918");
        Peer p2 = Peer.valueOf("ynode://75bff16c@127.0.0.1:32919");
        peerGroup.addPeer(BRANCH, p1);
        peerGroup.addPeer(BRANCH, p2);
        peerGroup.bootstrapping(discoveryClient);

        assert peerGroup.getPeerTable(BRANCH).contains(p1);
        assert peerGroup.getPeerTable(BRANCH).contains(p2);
        assert peerGroup.getPeerTable(BRANCH).getPeersCount() == 3;
    }

    @Test
    public void addPeerTest() {
        assert !peerGroup.isPeerEmpty(BRANCH);
        assert peerGroup.getPeerTable(BRANCH).getPeersCount() == 1;
        peerGroup.addPeer(BRANCH, Peer.valueOf("ynode://75bff16c@127.0.0.1:32918"));
        peerGroup.addPeer(BRANCH, Peer.valueOf("ynode://75bff16c@127.0.0.1:32919"));
        peerGroup.addPeer(OTHER_BRANCH, Peer.valueOf("ynode://75bff16c@127.0.0.1:32918"));
        assert peerGroup.count(BRANCH) == 3; // addPeer 시 owner 추가됨
        assert peerGroup.count(OTHER_BRANCH) == 2;
        assert !peerGroup.getPeers(BRANCH, OWNER).isEmpty();
        assert !peerGroup.isPeerEmpty(BRANCH);
    }

    @Test
    public void getPeerTest() {
        Peer requester = Peer.valueOf("ynode://75bff16c@127.0.0.1:32918");
        Collection<String> peerListWithoutRequester = peerGroup.getPeers(BRANCH, requester);
        assert peerListWithoutRequester.isEmpty();
        // requester 가 peer 목록 조회 후에는 peerTable 에 등록되어 있다
        assert peerGroup.containsPeer(BRANCH, requester);
    }

    @Test
    public void addPeerByYnodeUriTest() {
        assert !peerGroup.isPeerEmpty(BRANCH);
        assert peerGroup.getPeerTable(BRANCH).getPeersCount() == 1;
        peerGroup.addPeerByYnodeUri(BRANCH, "ynode://75bff16c@127.0.0.1:32918");
        peerGroup.addPeerByYnodeUri(OTHER_BRANCH, "ynode://75bff16c@127.0.0.1:32918");
        assert peerGroup.count(BRANCH) == 2;
        assert peerGroup.count(OTHER_BRANCH) == 2;
        peerGroup.addPeerByYnodeUri(BRANCH,
                Collections.singletonList("ynode://75bff16c@127.0.0.1:32919"));
        peerGroup.addPeerByYnodeUri(OTHER_BRANCH,
                Collections.singletonList("ynode://75bff16c@127.0.0.1:32919"));
        assert peerGroup.count(BRANCH) == 3;
        assert peerGroup.count(OTHER_BRANCH) == 3;
    }

    @Test
    public void getSeedPeerList() {
        assert peerGroup.getSeedPeerList() == null;
        peerGroup.setSeedPeerList(Collections.singletonList("ynode://75bff16c@127.0.0.1:8080"));
        assert !peerGroup.getSeedPeerList().isEmpty();
    }

    @Test
    public void getPeerUriListTest() {
        assert peerGroup.getPeerUriList(BRANCH).isEmpty();
        assert peerGroup.getPeerUriList(OTHER_BRANCH).isEmpty();
        peerGroup.addPeer(BRANCH, Peer.valueOf("ynode://75bff16c@127.0.0.1:32918"));
        peerGroup.addPeer(OTHER_BRANCH, Peer.valueOf("ynode://75bff16c@127.0.0.1:32918"));
        assert peerGroup.getPeerUriList(BRANCH).contains("ynode://75bff16c@127.0.0.1:32918");
        assert peerGroup.getPeerUriList(OTHER_BRANCH).contains("ynode://75bff16c@127.0.0.1:32918");
    }

    /**
     * ChannelMock 은 Pong 응답이 토클 됩니다.
     * 처음에는 정상적으로 Pong이 응답되서 안정적으로 채널에 추가시키기 위함
     * 이후 healthCheck 에서 null이 응답되어 피어 테이블과 채널에서 제거될 수 있게됨
     */
    @Test
    public void healthCheck() {
        PeerClientChannel peerClientChannel = ChannelMock.dummy();

        peerGroup.newPeerChannel(BRANCH, peerClientChannel); // Pong 정상응답
        assert !peerGroup.isChannelEmpty(BRANCH);

        peerGroup.addPeer(BRANCH, peerClientChannel.getPeer());
        assert peerGroup.containsPeer(BRANCH, peerClientChannel.getPeer());

        peerGroup.healthCheck(); // Pong null 응답

        assert peerGroup.isChannelEmpty(BRANCH);
        assert !peerGroup.containsPeer(BRANCH, peerClientChannel.getPeer());
    }

    @Test
    public void syncBlock() {
        addPeerChannel();
        List<BlockHusk> blockHuskList = peerGroup.syncBlock(BRANCH, 0);
        assert !blockHuskList.isEmpty();
    }

    @Test
    public void syncTransaction() {
        addPeerChannel();
        peerGroup.receivedTransaction(tx);
        assert !peerGroup.syncTransaction(BRANCH).isEmpty();
    }

    @Test
    public void addActivePeer() {
        int testCount = MAX_PEERS + 5;
        for (int i = 0; i < testCount; i++) {
            int port = i + 32918;
            ChannelMock channel = new ChannelMock("ynode://75bff16c@localhost:" + port);
            peerGroup.newPeerChannel(BRANCH, channel);
        }
        assert MAX_PEERS == peerGroup.getActivePeerList().size();
    }

    private void addPeerChannel() {
        assert peerGroup.isChannelEmpty(BRANCH);
        peerGroup.newPeerChannel(BRANCH, ChannelMock.dummy());
        assert !peerGroup.isChannelEmpty(BRANCH);
    }
}
