package dev.zhihexireng.node.api;

import dev.zhihexireng.core.store.TransactionPool;

public class TransactionReceiptDto {

    private final TransactionPool txPool;

    public TransactionReceiptDto(TransactionPool txPool) {
        this.txPool = txPool;
    }

}
