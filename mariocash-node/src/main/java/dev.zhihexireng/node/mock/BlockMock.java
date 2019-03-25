package dev.zhihexireng.node.mock;

import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeManager;

import java.io.IOException;

public class BlockMock {

    private final NodeManager nodeManager;

    public BlockMock(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public Block retBlockMock() throws IOException {
        BlockBuilderMock blockBuilderMock = new BlockBuilderMock(nodeManager);
        return blockBuilderMock.build(nodeManager.getWallet());
    }

}

