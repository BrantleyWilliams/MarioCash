package dev.zhihexireng.node.config;

import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerGroup;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;

public class BranchConfigurationTest {

    private final Peer owner = Peer.valueOf("ynode://75bff16c@127.0.0.1:32918");
    private final PeerGroup peerGroup = new PeerGroup(owner, 1);
    private final ResourceLoader loader = new DefaultResourceLoader();
    private MockEnvironment mockEnv;

    @Before
    public void setUp() {
        mockEnv = new MockEnvironment();
    }

    @Test
    public void defaultBranchGroupTest() throws IOException, InvalidCipherTextException,
            IllegalAccessException, InstantiationException {
        assert getBranchGroup().getBranchSize() == 1;
    }

    @Test
    public void productionBranchGroupTest() throws IOException, InvalidCipherTextException,
            IllegalAccessException, InstantiationException {
        mockEnv.addActiveProfile("prod");
        assert getBranchGroup().getBranchSize() == 1;
    }

    private BranchGroup getBranchGroup() throws IOException, InvalidCipherTextException,
            InstantiationException, IllegalAccessException {
        BranchConfiguration config = new BranchConfiguration(mockEnv, new Wallet(), peerGroup);
        config.setResource(loader.getResource("classpath:/genesis.json"));
        return config.branchGroup();
    }
}
