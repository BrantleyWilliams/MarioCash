package dev.zhihexireng.core.net;

import dev.zhihexireng.common.util.Utils;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.proto.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DiscoverTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DiscoverTask.class);

    private PeerGroup peerGroup;
    private final Peer owner;
    private final byte[] ownerId;

    public DiscoverTask(PeerGroup peerGroup) {
        this.peerGroup = peerGroup;
        this.owner = peerGroup.getOwner();
        this.ownerId = owner.getPeerId().getBytes();
    }

    @Override
    public void run() {
        discover(0, new ArrayList<>());
    }

    public abstract PeerClientChannel getClient(Peer peer);

    private synchronized void discover(int round, List<Peer> prevTried) {
        log.info("Start discover!");
        log.info("Size of STEM PeerTable => {}",
                peerGroup.getPeerTable(BranchId.stem()).getPeersCount() - 1);
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

            Optional<PeerTable> peerTable
                    = Optional.ofNullable(peerGroup.getPeerTable(BranchId.stem()));
            List<Peer> closest = peerTable
                    .map(pt -> pt.getClosestPeers(ownerId)).orElse(new ArrayList<>());
            List<Peer> tried = new ArrayList<>();

            for (Peer p : closest) {
                if (!tried.contains(p) && !prevTried.contains(p)) {
                    try {
                        Optional<List<NodeInfo>> list = Optional.ofNullable(
                                getClient(p).findPeers(BranchId.stem(), owner));
                        list.ifPresent(nodeInfo -> nodeInfo.forEach(
                                n -> peerGroup.addPeerByYnodeUri(BranchId.stem(), n.getUrl())));

                        tried.add(p);
                        Utils.sleep(50);
                    } catch (Exception e) {
                        //log.error("Unexpected Exception " + e, e);
                    }
                }
                if (tried.size() == KademliaOptions.ALPHA) {
                    break;
                }
            }

            if (tried.isEmpty()) {
                log.debug("Terminating discover after {} rounds.", round);
                log.trace("{}\n{}",
                        String.format("Peers discovered %d", peerGroup.count(BranchId.stem())),
                        peerGroup.getPeerUriList(BranchId.stem()));
                return;
            }

            tried.addAll(prevTried);
            discover(round + 1, tried);
        } catch (Exception e) {
            log.info("{}", e);
        }
    }
}
