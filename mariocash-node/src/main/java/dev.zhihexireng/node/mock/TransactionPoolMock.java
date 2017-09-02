package dev.zhihexireng.node.mock;

import dev.zhihexireng.core.TransactionPool;

import java.util.HashMap;
import java.util.Map;

public class TransactionPoolMock implements TransactionPool {
    private Map<String, TransactionPool> txs = new HashMap<>();
}
