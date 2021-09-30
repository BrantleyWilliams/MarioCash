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

package dev.zhihexireng.node;

import dev.zhihexireng.common.config.DefaultConfig;
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.net.NodeStatus;
import dev.zhihexireng.core.net.PeerGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class NodeHealthIndicator implements HealthIndicator, NodeStatus {
    private static final Logger log = LoggerFactory.getLogger(NodeHealthIndicator.class);
    private static final Status SYNC = new Status("SYNC", "Synchronizing..");

    private final AtomicReference<Health> health = new AtomicReference<>(Health.down().build());
    private final DefaultConfig defaultConfig;
    private final BranchGroup branchGroup;
    private final PeerGroup peerGroup;

    @Autowired
    public NodeHealthIndicator(DefaultConfig defaultConfig, BranchGroup branchGroup,
                               PeerGroup peerGroup) {
        this.defaultConfig = defaultConfig;
        this.branchGroup = branchGroup;
        this.peerGroup = peerGroup;
    }

    @Override
    public Health health() {
        updateDetail(health.get().getStatus());
        return health.get();
    }

    @Override
    public void up() {
        log.info("Changed node status={} -> {}", health.get().getStatus(), Status.UP);
        updateDetail(Status.UP);
    }

    @Override
    public void sync() {
        log.info("Changed node status={} -> {}", health.get().getStatus(), SYNC);
        updateDetail(SYNC);
    }

    @Override
    public boolean isUpStatus() {
        return health.get().getStatus().equals(Status.UP);
    }

    private void updateDetail(Status status) {
        Health.Builder builder = Health.status(status);
        builder.withDetail("name", defaultConfig.getNodeName());
        builder.withDetail("version", defaultConfig.getNodeVersion());
        builder.withDetail("p2pVersion", defaultConfig.getNetworkP2PVersion());
        builder.withDetail("network", defaultConfig.getNetwork());
        // Add Node BranchIds and block index
        Map<BranchId, Long> branches = branchGroup.getAllBranch()
                .stream()
                .collect(Collectors.toMap(BlockChain::getBranchId, BlockChain::getLastIndex));
        builder.withDetail("branches", branches);

        builder.withDetail("activePeers", peerGroup.getActivePeerList().size());
        health.set(builder.build());
    }
}
