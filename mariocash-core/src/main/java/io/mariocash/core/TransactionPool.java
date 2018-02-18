package dev.zhihexireng.core;

import java.io.IOException;

public interface TransactionPool {
    Transaction getTxByHash(String id);

    Transaction addTx(Transaction tx) throws IOException;
}
