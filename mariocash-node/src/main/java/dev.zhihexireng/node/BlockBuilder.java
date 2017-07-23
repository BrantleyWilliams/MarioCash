package dev.zhihexireng.node;

import dev.zhihexireng.node.mock.Block;

public interface BlockBuilder {
    Block build(String data);
}
