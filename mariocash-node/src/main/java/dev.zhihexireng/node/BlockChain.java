package dev.zhihexireng.node;

import dev.zhihexireng.core.Block;

import java.util.LinkedHashMap;

public interface BlockChain {
    Block addBlock(Block generatedBlock);

    Block getBlockByIndex(int index);

    Block getBlockByHash(String id);

    LinkedHashMap<byte[], Block> getBlocks();
}
