package dev.zhihexireng.node;

import dev.zhihexireng.core.Block;

public interface BlockBuilder {
    Block build(String data);
}
