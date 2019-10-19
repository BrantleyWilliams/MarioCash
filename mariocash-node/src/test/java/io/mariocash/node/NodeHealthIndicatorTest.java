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
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.net.PeerClientChannel;
import dev.zhihexireng.node.config.NodeProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

public class NodeHealthIndicatorTest {
    private final Status SYNC = new Status("SYNC", "Synchronizing..");
    NodeHealthIndicator nodeHealthIndicator;

    @Before
    public void setUp() {
        MessageSender<PeerClientChannel> sender = new MessageSender<>(new NodeProperties());
        this.nodeHealthIndicator = new NodeHealthIndicator(new DefaultConfig(), new BlockChain(),
                sender);
    }

    @Test
    public void health() {
        Health health = nodeHealthIndicator.health();
        assert health.getStatus() == Status.DOWN;
        assert health.getDetails().get("name").equals("mariocash");
        assert (long) health.getDetails().get("height") == 1;
        assert (int) health.getDetails().get("activePeers") == 0;
    }

    @Test
    public void up() {
        assert nodeHealthIndicator.health().getStatus() == Status.DOWN;
        nodeHealthIndicator.up();
        assert nodeHealthIndicator.health().getStatus() == Status.UP;
    }

    @Test
    public void sync() {
        assert nodeHealthIndicator.health().getStatus() == Status.DOWN;
        nodeHealthIndicator.sync();
        assert nodeHealthIndicator.health().getStatus().getCode().equals(SYNC.getCode());
    }
}
