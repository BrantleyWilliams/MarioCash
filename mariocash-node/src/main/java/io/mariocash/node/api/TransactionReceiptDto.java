package dev.zhihexireng.node.api;

import dev.zhihexireng.core.store.CachePool;

public class TransactionReceiptDto {

    private final CachePool txPool;

    public TransactionReceiptDto(CachePool txPool) {
        this.txPool = txPool;
    }

}
