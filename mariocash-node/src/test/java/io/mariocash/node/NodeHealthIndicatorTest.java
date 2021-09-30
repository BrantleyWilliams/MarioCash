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

import dev.zhihexireng.TestUtils;
import dev.zhihexireng.common.config.DefaultConfig;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NodeHealthIndicatorTest {
    private static final Status SYNC = new Status("SYNC", "Synchronizing..");

    @Mock
    private BranchGroup branchGroupMock;

    private NodeHealthIndicator nodeHealthIndicator;

    private Peer owner = Peer.valueOf("ynode://75bff16c@127.0.0.1:32918");

    @Before
    public void setUp() {
        PeerGroup peerGroup = new PeerGroup(owner, 1);
        this.nodeHealthIndicator = new NodeHealthIndicator(new DefaultConfig(), branchGroupMock,
                peerGroup);
    }

    @After
    public void tearDown() {
        TestUtils.clearTestDb();
    }

    @Test
    public void health() {
        Health health = nodeHealthIndicator.health();
        assert health.getStatus() == Status.DOWN;
        assert health.getDetails().get("name").equals("mariocash");
        assertNotNull(health.getDetails().get("branches"));
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
