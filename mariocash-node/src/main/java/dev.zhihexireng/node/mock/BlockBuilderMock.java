package dev.zhihexireng.node.mock;

import dev.zhihexireng.node.BlockBuilder;

public class BlockBuilderMock implements BlockBuilder {

    @Override
    public Block build(String data) {
        return new Block();
    }
}
