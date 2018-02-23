package dev.zhihexireng.core;

public interface TransactionPool {
    Transaction getTxByHash(String id);

    Transaction addTx(Transaction tx);
}
