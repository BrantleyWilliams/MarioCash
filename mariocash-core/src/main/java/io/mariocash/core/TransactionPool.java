package dev.zhihexireng.core;

import dev.zhihexireng.core.format.TransactionFormat;

import java.io.IOException;
import java.util.List;

public interface TransactionPool {
    TransactionFormat getTxByHash(String id);

    TransactionFormat addTx(Transaction tx) throws IOException;

    List getTxList();

    void removeTx(List<String> hashList);
}
