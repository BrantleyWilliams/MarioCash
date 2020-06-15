package dev.zhihexireng.core.store;

import dev.zhihexireng.core.TransactionReceipt;

import java.util.HashMap;

public class TransactionReceiptStore {

    private final HashMap<String, TransactionReceipt> txReciptStore = new HashMap<>();

    public void put(String txHash, TransactionReceipt txRecipt) {
        txReciptStore.put(txHash, txRecipt);
    }

    public TransactionReceipt get(String txHash) {
        return txReciptStore.get(txHash);
    }

    public HashMap<String, TransactionReceipt> getTxReciptStore() {
        return txReciptStore;
    }
}
