package dev.zhihexireng.node.mock;

import dev.zhihexireng.core.Block;

import java.io.IOException;

public class BlockMock {

    public String retBlockMock() throws IOException {
        BlockBuilderMock blockBuilderMock = new BlockBuilderMock();
        Block block = blockBuilderMock.build();
        return block.toString();
    }

}

