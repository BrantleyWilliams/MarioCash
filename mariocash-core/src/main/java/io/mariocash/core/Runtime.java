package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import dev.zhihexireng.contract.Contract;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;

class Runtime<T> {

    private final StateStore<T> stateStore;
    private final TransactionReceiptStore txReceiptStore;

    Runtime(StateStore<T> stateStore, TransactionReceiptStore txReceiptStore) {
        this.stateStore = stateStore;
        this.txReceiptStore = txReceiptStore;
    }

    public boolean invoke(Contract contract, TransactionHusk tx) {
        contract.init(stateStore, txReceiptStore);
        return contract.invoke(tx);
    }

    public JsonObject query(Contract contract, JsonObject query) throws Exception {
        contract.init(stateStore, txReceiptStore);
        return contract.query(query);
    }

    StateStore<T> getStateStore() {
        return this.stateStore;
    }

    TransactionReceiptStore getTransactionReceiptStore() {
        return this.txReceiptStore;
    }
}
