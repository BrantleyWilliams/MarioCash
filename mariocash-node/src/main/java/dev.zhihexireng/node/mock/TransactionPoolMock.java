package dev.zhihexireng.node.mock;

import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.TransactionPool;

import java.util.HashMap;
import java.util.Map;

public class TransactionPoolMock implements TransactionPool {
    private Map<String, Transaction> txs = new HashMap<>();

    @Override
    public Transaction getTxByHash(String id) {
        return txs.get(id);
    }

    @Override
    public Transaction addTx(Transaction tx) {
        txs.put(tx.getHashString(), tx);
        return tx;
    }
}
