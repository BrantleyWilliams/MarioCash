/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.node.config.annotaion;

import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerGroup;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;

public class DefaultBranchAutoConfigTest {

    private final Peer owner = Peer.valueOf("ynode://75bff16c@127.0.0.1:32918");
    private final PeerGroup peerGroup = new PeerGroup(owner, 1);
    private final ResourceLoader loader = new DefaultResourceLoader();
    private MockEnvironment mockEnv;
    private BranchGroup branchGroup;

    @Before
    public void setUp() {
        mockEnv = new MockEnvironment();
        branchGroup = new BranchGroup();
    }

    @Test
    public void addStemBranchTest() throws IllegalAccessException, IOException,
            InstantiationException {
        DefaultBranchAutoConfig config = new DefaultBranchAutoConfig(mockEnv);
        config.stemResource = loader.getResource("classpath:/genesis-stem.json");
        BlockChain blockChain = config.stem(peerGroup, branchGroup);
        assert blockChain.getBranchId().equals(BranchId.stem());
        assert branchGroup.getBranchSize() == 1;
    }

    @Test
    public void addProductionStemBranchTest() throws IllegalAccessException, IOException,
            InstantiationException {
        mockEnv.addActiveProfile("prod");
        addStemBranchTest();
    }

    @Test
    public void addYeedBranchTest() throws IllegalAccessException, IOException,
            InstantiationException {
        DefaultBranchAutoConfig config = new DefaultBranchAutoConfig(mockEnv);
        config.yeedResource = loader.getResource("classpath:/genesis-yeed.json");
        BlockChain blockChain = config.yeed(peerGroup, branchGroup);
        assert blockChain.getBranchId().equals(BranchId.yeed());
        assert branchGroup.getBranchSize() == 1;
    }
}
