package dev.zhihexireng.node.api;

import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.node.mock.BlockBuilderMock;
import dev.zhihexireng.node.mock.BlockMock;
import dev.zhihexireng.node.mock.NodeManagerMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@Import(JsonRpcConfig.class)
public class BlockApiImplTest {
    private static final Logger log = LoggerFactory.getLogger(TransactionApi.class);

    private final NodeManager nodeManager = new NodeManagerMock();

    @Test
    public void createBlockMock() throws IOException {
        BlockMock blockMock = new BlockMock(nodeManager);
        log.debug("blockMock" + blockMock.retBlockMock());
    }

    @Test
    public void blockBuildMockTest() throws IOException {
        BlockBuilderMock blockBuilderMock = new BlockBuilderMock(nodeManager);
        Block block = blockBuilderMock.build(nodeManager.getWallet());
        log.debug("blockBuilderMock : " + block.toString());
    }
}
