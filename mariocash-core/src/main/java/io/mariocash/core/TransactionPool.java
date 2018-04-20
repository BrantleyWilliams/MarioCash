package dev.zhihexireng.core;

import dev.zhihexireng.core.format.TransactionFormat;

import java.io.IOException;

public interface TransactionPool {
    TransactionFormat getTxByHash(String id);

    TransactionFormat addTx(TransactionFormat tx) throws IOException;
}
