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
import dev.zhihexireng.core.exception.NonExistObjectException;
import dev.zhihexireng.core.store.PeerStore;
import dev.zhihexireng.core.store.StoreBuilder;
import dev.zhihexireng.proto.Pong;
import dev.zhihexireng.proto.Proto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PeerGroup implements BranchEventListener {

    private static final Logger log = LoggerFactory.getLogger(PeerGroup.class);

    private final Map<BranchId, PeerTable> peerTables = new ConcurrentHashMap<>();

    private final Map<BranchId, Map<PeerId, PeerClientChannel>> peerTableChannels
            = new ConcurrentHashMap<>();

    private final int maxPeers;

    private final Peer owner;

    private List<String> seedPeerList;

    public PeerGroup(Peer owner, int maxPeers) {
        this.owner = owner;
        this.maxPeers = maxPeers;
    }

    public Peer getOwner() {
        return owner;
    }

    public void bootstrapping(DiscoveryClient discoveryClient) {
        List<String> seedPeerList = new ArrayList<>();
        try {
            if (peerTables.containsKey(BranchId.stem())
                    && getPeerTable(BranchId.stem()).isPeerStoreEmpty()) {
                seedPeerList = getPeerTable(BranchId.stem()).getAllFromPeerStore();
            } else {
                seedPeerList = getSeedPeerList();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (seedPeerList == null || seedPeerList.isEmpty()) {
            return;
        }
        for (String ynodeUri : seedPeerList) {
            if (ynodeUri.equals(owner.getYnodeUri())) {
                continue;
            }
            Peer peer = Peer.valueOf(ynodeUri);
            log.info("Try connecting to SEED peer = {}", peer);

            try {
                List<String> foundedPeerList =
                        discoveryClient.findPeers(peer.getHost(), peer.getPort(), owner);
                foundedPeerList.forEach(u -> addPeerByYnodeUri(BranchId.stem(), u));
            } catch (Exception e) {
                //log.debug(e.getMessage(), e);
                log.error("Failed connecting to SEED peer = {}", peer);
                continue;
            }
            DiscoverTask discoverTask = new DiscoverTask(this, discoveryClient);
            discoverTask.run();
        }
    }

    void addPeerByYnodeUri(BranchId branchId, List<String> peerList) {
        for (String ynodeUri : peerList) {
            addPeerByYnodeUri(branchId, ynodeUri);
        }
    }

    void addPeerByYnodeUri(BranchId branchId, String ynodeUri) {
        addPeer(branchId, Peer.valueOf(ynodeUri));
    }

    void addPeer(BranchId branchId, Peer peer) {
        log.info("Add peer => {}, PeerTable of {} : {}", peer, branchId, peerTables.get(branchId));
        PeerTable peerTable = peerTables.get(branchId);
        if (peerTable == null) {
            peerTable = new PeerTable(buildPeerStore(branchId), owner);
            peerTables.put(branchId, peerTable);
        }
        peerTable.addPeer(peer);
        log.info("PeerTable => {}", peerTable);
    }

    int count(BranchId branchId) {
        Optional<PeerTable> peerTable = Optional.ofNullable(peerTables.get(branchId));
        return peerTable.map(PeerTable::getPeersCount).orElse(0);
        //log.debug(branchId + "'s count => " + peerTables.get(branchId).getPeersCount());
        //return peerTables.get(branchId).getPeersCount();
    }

    public List<String> getPeers(BranchId branchId, Peer peer) {
        ArrayList<String> peerList = new ArrayList<>();
        PeerTable peerTable;

        if (peerTables.containsKey(branchId)) {
            log.debug(branchId + "'s peers size => " + peerTables.get(branchId).getPeersCount());
            peerTable = peerTables.get(branchId);
        } else {
            peerTable = new PeerTable(buildPeerStore(branchId), owner);
            peerTables.put(branchId, peerTable);
        }

        for (Peer storedPeer : peerTable.getAllPeers()) {
            peerList.add(storedPeer.toString());
        }
        peerTable.addPeer(peer);

        return peerList;
    }

    public List<Peer> getClosestPeers() {
        return Optional.ofNullable(getPeerTable(BranchId.stem()))
                .map(o -> o.getClosestPeers(owner.getPeerId().getBytes()))
                .orElse(new ArrayList<>());
        //return peerTables.get(BranchId.stem()).getClosestPeers(owner.getPeerId().getBytes());
    }

    PeerStore buildPeerStore(BranchId branchId) {
        StoreBuilder storeBuilder = new StoreBuilder(false);
        return storeBuilder.buildPeerStore(branchId);
    }

    public PeerTable getPeerTable(BranchId branchId) {
        return peerTables.getOrDefault(branchId, null);
    }

    boolean containsPeer(BranchId branchId, Peer peer) {
        if (peerTables.containsKey(branchId)) {
            return peerTables.get(branchId).contains(peer);
        } else {
            return false;
        }
    }

    boolean isPeerEmpty(BranchId branchId) {
        if (peerTables.containsKey(branchId)) {
            return peerTables.get(branchId).getPeersCount() == 0;
        } else {
            return true;
        }
    }

    public boolean isChannelEmpty(BranchId branchId) {
        if (peerTableChannels.containsKey(branchId)) {
            return peerTableChannels.get(branchId).isEmpty();
        } else {
            return true;
        }
    }

    List<String> getSeedPeerList() {
        return seedPeerList;
    }

    public void setSeedPeerList(List<String> seedPeerList) {
        this.seedPeerList = seedPeerList;
    }

    public List<String> getPeerUriList(BranchId branchId) {
        if (peerTables.containsKey(branchId)) {
            return peerTables.get(branchId).getAllPeers().stream()
                    .map(Peer::getYnodeUri).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public void destroy() {
        for (Map<PeerId, PeerClientChannel> peerChannel : peerTableChannels.values()) {
            peerChannel.values().forEach(PeerClientChannel::stop);
        }
    }

    public void healthCheck() {
        if (peerTableChannels.isEmpty()) {
            log.trace("Active peer is empty to health check peer");
            throw new NonExistObjectException("Active peer is empty to health check peer");
            //return;
        }
        log.debug("peerTableChannel" + peerTableChannels);

        for (Map.Entry<BranchId, Map<PeerId, PeerClientChannel>> entry
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
                Peer peer = client.getPeer();
                peerTables.get(branchId).dropPeer(peer);
                peerTableChannels.get(branchId).remove(peer.getPeerId());
                try {
                    //peerStore 에서 삭제
                    getPeerTable(branchId).removePeerFromPeerStore(peer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

        if (peerTableChannels.containsKey(branchId)) {
            if (peerTableChannels.get(branchId).containsKey(peer.getPeerId())) {
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
                // 접속 성공 시
                log.info("Added channel={}", peer);
                if (peerTableChannels.containsKey(branchId)) {
                    peerTableChannels.get(branchId).put(peer.getPeerId(), client);
                } else {
                    Map<PeerId, PeerClientChannel> peerChannelList = new ConcurrentHashMap<>();
                    peerChannelList.put(peer.getPeerId(), client);
                    peerTableChannels.put(branchId, peerChannelList);
                }
            } else {
                // 접속 실패 시 목록 및 버킷에서 제거
                peerTables.get(branchId).dropPeer(peer);
            }
        } catch (Exception e) {
            log.warn("Fail to add to the peer channel err=" + e.getMessage());
        }
    }

    public List<String> getActivePeerList() {
        List<String> activePeerList = new ArrayList<>();
        for (Map<PeerId, PeerClientChannel> peerTableChannel : peerTableChannels.values()) {
            List<String> branchChannelList = peerTableChannel.values().stream()
                    .map(channel -> channel.getPeer().toString())
                    .collect(Collectors.toList());
            activePeerList.addAll(branchChannelList);
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
        Map<PeerId, PeerClientChannel> peerClientChannelMap = peerTableChannels.get(branchId);
        PeerId key = (PeerId) peerClientChannelMap.keySet().toArray()[0];
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
        Map<PeerId, PeerClientChannel> peerClientChannelMap = peerTableChannels.get(branchId);
        PeerId key = (PeerId) peerClientChannelMap.keySet().toArray()[0];
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
