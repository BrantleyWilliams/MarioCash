package dev.zhihexireng.core;

import java.io.IOException;
import java.util.List;

public interface TransactionPool {
    Transaction getTxByHash(String id);

    Transaction addTx(Transaction tx) throws IOException;

    List getTxList();

    void removeTx(List<String> hashList);
}
