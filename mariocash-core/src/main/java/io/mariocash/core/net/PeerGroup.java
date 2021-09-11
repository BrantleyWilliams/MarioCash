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

import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.event.BranchEventListener;
import dev.zhihexireng.proto.Pong;
import dev.zhihexireng.proto.Proto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PeerGroup implements BranchEventListener {

    private static final Logger log = LoggerFactory.getLogger(PeerGroup.class);

    private final Map<BranchId, Map<String, Peer>> peerTables = new ConcurrentHashMap<>();
    // <branchId, peers>


    private final Map<BranchId, Map<String, PeerClientChannel>> peerTableChannels
            = new ConcurrentHashMap<>();
    // <branchId, peerChannels>

    private final int maxPeers;

    private List<String> seedPeerList;

    public PeerGroup(int maxPeers) {
        this.maxPeers = maxPeers;
    }

    void addPeerByYnodeUri(BranchId branchId, List<String> peerList) {
        for (String ynodeUri : peerList) {
            addPeerByYnodeUri(branchId, ynodeUri);
        }
    }

    void addPeerByYnodeUri(BranchId branchId, String ynodeUri) {
        addPeer(branchId, Peer.valueOf(ynodeUri));
    }

    public void addPeer(BranchId branchId, Peer peer) {
        String ynodeUri = peer.getYnodeUri();

        if (peerTables.containsKey(branchId)) {
            if (peerTables.get(branchId).containsKey(ynodeUri)) {
                log.debug("Duplicated node in <" + branchId + ">, uri={}", ynodeUri);
                return;
            } else if (peerTables.get(branchId).size() >= maxPeers) {
                log.warn("Maximum number of peers exceeded in <" + branchId + ">."
                        + "count={}, peer={}", peerTables.get(branchId).size(), ynodeUri);
                return;
            }

            peerTables.get(branchId).put(ynodeUri, peer);
            log.debug("peerTables has " + branchId + "\npeerTables => " + peerTables.toString());
        } else {
            Map<String, Peer> peerList = new ConcurrentHashMap<>();
            peerList.put(ynodeUri, peer);
            peerTables.put(branchId, peerList);
            log.debug("peerTables has no " + branchId + "\npeerTables => " + peerTables.toString());
        }
    }

    int count(BranchId branchId) {
        log.debug(branchId + "'s count => " + peerTables.get(branchId).size());
        return peerTables.get(branchId).size();
    }

    public Collection<Peer> getPeers(BranchId branchId) {
        if (peerTables.containsKey(branchId)) {
            log.debug(branchId + "'s peers size => " + peerTables.get(branchId).values().size());
            return peerTables.get(branchId).values();
        } else {
            return new ArrayList<>();
        }
    }

    public boolean contains(BranchId branchId, String ynodeUri) {
        return peerTables.get(branchId).containsKey(ynodeUri);
    }

    public boolean isEmpty(BranchId branchId) {
        if (peerTables.containsKey(branchId)) {
            return peerTables.get(branchId).isEmpty();
        } else {
            return true;
        }
    }

    public List<String> getSeedPeerList() {
        return seedPeerList;
    }

    public void setSeedPeerList(List<String> seedPeerList) {
        this.seedPeerList = seedPeerList;
    }

    public List<String> getPeerUriList(BranchId branchId) {
        if (peerTables.containsKey(branchId)) {
            return peerTables.get(branchId).values().stream()
                    .map(Peer::getYnodeUri).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public void destroy() {
        for (Map<String, PeerClientChannel> peerChannel : peerTableChannels.values()) {
            peerChannel.values().forEach(PeerClientChannel::stop);
        }
    }

    public void healthCheck() {
        if (peerTableChannels.isEmpty()) {
            log.trace("Active peer is empty to health check peer");
            return;
        }
        log.debug("peerTableChannel" + peerTableChannels);

        for (Map.Entry<BranchId, Map<String, PeerClientChannel>> entry
                : peerTableChannels.entrySet()) {
            BranchId branchId = entry.getKey();
            List<PeerClientChannel> peerChannelList
                    = new ArrayList<>(peerTableChannels.get(branchId).values());

            for (PeerClientChannel client : peerChannelList) {
                try {
                    Pong pong = client.ping("Ping");
                    if (pong.getPong().equals("Pong")) {
                        continue;
                    }
                } catch (Exception e) {
                    log.warn("Health check fail. peer=" + client.getPeer().getYnodeUri());
                }
                String ynodeUri = client.getPeer().getYnodeUri();
                peerTableChannels.get(branchId).remove(ynodeUri);
                client.stop();
            }
        }
    }

    @Override
    public void receivedTransaction(TransactionHusk tx) {
        if (peerTableChannels.isEmpty()) {
            log.trace("Active peer is empty to broadcast transaction");
        }
        Proto.Transaction[] txns = new Proto.Transaction[] {tx.getInstance()};

        if (peerTableChannels.containsKey(tx.getBranchId())) {
            for (PeerClientChannel client : peerTableChannels.get(tx.getBranchId()).values()) {
                client.broadcastTransaction(txns);
            }
        }
    }

    @Override
    public void chainedBlock(BlockHusk block) {
        if (peerTableChannels.isEmpty()) {
            log.trace("Active peer is empty to broadcast block");
        }
        Proto.Block[] blocks = new Proto.Block[] {block.getInstance()};
        if (peerTableChannels.containsKey(block.getBranchId())) {
            for (PeerClientChannel client : peerTableChannels.get(block.getBranchId()).values()) {
                client.broadcastBlock(blocks);
            }
        }
    }

    public void newPeerChannel(BranchId branchId, PeerClientChannel client) {
        Peer peer = client.getPeer();
        /*
        if (peerChannels.containsKey(peer.getYnodeUri())) {
            return;
        } else if (peerChannels.size() >= maxPeers) {
            log.info("Ignore to add active peer channel. count={}, peer={}", peerChannels.size(),
                    peer.getYnodeUri());
            return;
        }
        */
        if (peerTableChannels.containsKey(branchId)) {
            if (peerTableChannels.get(branchId).containsKey(peer.getYnodeUri())) {
                return;
            } else if (peerTableChannels.get(branchId).size() >= maxPeers) {
                log.info("Maximum number of peer channel exceeded. count={}, peer={}",
                        peerTableChannels.size(), peer.getYnodeUri());
                return;
            }
        }

        try {
            log.info("Connecting... peer {}:{}", peer.getHost(), peer.getPort());
            Pong pong = client.ping("Ping");
            // TODO validation peer
            if (pong.getPong().equals("Pong")) {
                log.info("Added channel={}", peer);
                //peerChannels.put(peer.getYnodeUri(), client);
                if (peerTableChannels.containsKey(branchId)) {
                    peerTableChannels.get(branchId).put(peer.getYnodeUri(), client);
                } else {
                    Map<String, PeerClientChannel> peerChannelList = new ConcurrentHashMap<>();
                    peerChannelList.put(peer.getYnodeUri(), client);
                    peerTableChannels.put(branchId, peerChannelList);
                }
            }
        } catch (Exception e) {
            log.warn("Fail to add to the peer channel err=" + e.getMessage());
        }
    }

    public List<String> getActivePeerList() {
        List<String> activePeerList = new ArrayList<>();
        for (Map<String, PeerClientChannel> peerTable : peerTableChannels.values()) {
            activePeerList.addAll(peerTable.keySet());
        }
        return activePeerList;
    }

    /**
     * Sync block list.
     *
     * @param offset the offset
     * @return the block list
     */
    public List<BlockHusk> syncBlock(BranchId branchId, long offset) {
        if (!peerTableChannels.containsKey(branchId)) {
            log.trace("Active peer is empty to sync block");
            return Collections.emptyList();
        }
        // TODO sync peer selection policy
        Map<String, PeerClientChannel> peerClientChannelMap = peerTableChannels.get(branchId);
        String key = (String) peerClientChannelMap.keySet().toArray()[0];
        PeerClientChannel client = peerClientChannelMap.get(key);
        List<Proto.Block> blockList = client.syncBlock(branchId, offset);
        log.debug("Synchronize block offset={} receivedSize={}, from={}", offset, blockList.size(),
                client.getPeer());
        List<BlockHusk> syncList = new ArrayList<>(blockList.size());
        for (Proto.Block block : blockList) {
            syncList.add(new BlockHusk(block));
        }
        return syncList;
    }

    /**
     * Sync transaction list.
     *
     * @return the transaction list
     */
    public List<TransactionHusk> syncTransaction(BranchId branchId) {
        if (!peerTableChannels.containsKey(branchId)) {
            log.trace("Active peer is empty to sync transaction");
            return Collections.emptyList();
        }
        // TODO sync peer selection policy
        Map<String, PeerClientChannel> peerClientChannelMap = peerTableChannels.get(branchId);
        String key = (String) peerClientChannelMap.keySet().toArray()[0];
        PeerClientChannel client = peerClientChannelMap.get(key);
        List<Proto.Transaction> txList = client.syncTransaction(branchId);
        log.info("Synchronize transaction receivedSize={}, from={}", txList.size(),
                client.getPeer());
        List<TransactionHusk> syncList = new ArrayList<>(txList.size());
        for (Proto.Transaction tx : txList) {
            syncList.add(new TransactionHusk(tx));
        }
        return syncList;
    }
}
