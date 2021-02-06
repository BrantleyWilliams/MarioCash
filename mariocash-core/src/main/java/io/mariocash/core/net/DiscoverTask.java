package dev.zhihexireng.core.net;

import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DiscoverTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger("DiscoverTask");

    private PeerGroup peerGroup;
    private NodeManager nodeManager;
    private Peer owner;
    private byte[] ownerId;

    public DiscoverTask(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
        String ynodeUri = nodeManager.getNodeUri();
        owner = Peer.valueOf(ynodeUri);
        ownerId = owner.getPeerId().getBytes();
    }

    @Override
    public void run() {
        discover(ownerId, 0, new ArrayList<>());
    }

    private synchronized void discover(byte[] peerId, int round, List<Peer> prevTried) {
        try {
            if (round == KademliaOptions.MAX_STEPS) {
                log.debug("Peer table contains [{}] peers", peerGroup.count(BranchId.stem()));
                log.debug("{}", String.format("(KademliaOptions.MAX_STEPS) Terminating discover"
                        + "after %d rounds.", round));
                log.trace("{}\n{}",
                        String.format("Peers discovered %d", peerGroup.count(BranchId.stem())),
                        peerGroup.getPeerUriList(BranchId.stem()));
                return;
            }

            List<Peer> closest = peerGroup.getPeerTable(BranchId.stem()).getClosestPeers(ownerId);
            List<Peer> tried = new ArrayList<>();

            for (Peer p : closest) {
                if (!tried.contains(p) && !prevTried.contains(p)) {
                    try {
                        //TODO FIND_NODE 메세지 전송 (NodeHandler)
                        tried.add(p);
                        Utils.sleep(50);
                    } catch (Exception e) {
                        log.error("Unexpected Exception " + e, e);
                    }
                }
                if (tried.size() == KademliaOptions.ALPHA) {
                    break;
                }
            }

            if (tried.isEmpty()) {
                log.debug("{}", String.format(
                        "(tried.isEmpty()) Terminating discover after %d rounds.", round));
                log.trace("{}\n{}",
                        String.format("Peers discovered %d", peerGroup.count(BranchId.stem())),
                        peerGroup.getPeerUriList(BranchId.stem()));
                return;
            }

            tried.addAll(prevTried);
            discover(ownerId, round + 1, tried);
        } catch (Exception e) {
            log.info("{}", e);
        }
    }
}
