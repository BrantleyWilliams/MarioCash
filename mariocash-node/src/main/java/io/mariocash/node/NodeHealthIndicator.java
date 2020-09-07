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

import dev.zhihexireng.config.DefaultConfig;
import dev.zhihexireng.core.net.PeerChannelGroup;
import dev.zhihexireng.core.store.BlockStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class NodeHealthIndicator implements HealthIndicator {
    private final AtomicReference<Health> health = new AtomicReference<>(Health.down().build());

    private final DefaultConfig defaultConfig;

    private final BlockStore blockStore;

    private final PeerChannelGroup peerChannelGroup;

    @Autowired
    public NodeHealthIndicator(DefaultConfig defaultConfig, BlockStore blockStore,
                               PeerChannelGroup peerChannelGroup) {
        this.defaultConfig = defaultConfig;
        this.blockStore = blockStore;
        this.peerChannelGroup = peerChannelGroup;
    }

    @Override
    public Health health() {
        updateDetail(health.get().getStatus());
        return health.get();
    }

    public void up() {
        updateDetail(Status.UP);
    }

    public void sync() {
        updateDetail(new Status("SYNC", "Synchronizing.."));
    }

    private void updateDetail(Status status) {
        Health.Builder builder = Health.status(status);
        builder.withDetail("name", defaultConfig.getNodeName());
        builder.withDetail("version", defaultConfig.getNodeVersion());
        builder.withDetail("p2pVersion", defaultConfig.getNetworkP2PVersion());
        builder.withDetail("network", defaultConfig.getNetwork());
        builder.withDetail("height", blockStore.size());
        builder.withDetail("activePeers", peerChannelGroup.getActivePeerList().size());
        health.set(builder.build());
    }
}
