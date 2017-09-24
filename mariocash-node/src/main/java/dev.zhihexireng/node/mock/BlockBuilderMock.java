package dev.zhihexireng.node.mock;

import dev.zhihexireng.core.Account;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockBody;
import dev.zhihexireng.core.BlockHeader;
import dev.zhihexireng.node.BlockBuilder;

import java.util.Arrays;

public class BlockBuilderMock implements BlockBuilder {
    @Override
    public Block build(String data) {
        Account account = new Account();
        BlockBody blockBody = new BlockBody(Arrays.asList());
        BlockHeader blockHeader = new BlockHeader.Builder()
                .account(account)
                .prevBlock(null)
                .blockBody(blockBody).build();
        return new Block(blockHeader, blockBody);
    }
}
