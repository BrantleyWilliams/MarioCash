package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import dev.zhihexireng.contract.Contract;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;

public class Runtime {

    private final StateStore stateStore = new StateStore();
    private final TransactionReceiptStore txReceiptStore;

    public Runtime(TransactionReceiptStore txReceiptStore) {
        this.txReceiptStore = txReceiptStore;
    }

    public boolean invoke(Contract contract, TransactionHusk tx) throws Exception {
        contract.init(stateStore, txReceiptStore);
        return contract.invoke(tx);
    }

    public JsonObject query(Contract contract, JsonObject query) throws Exception {
        contract.init(stateStore, txReceiptStore);
        return contract.query(query);
    }

    public StateStore getStateStore() {
        return this.stateStore;
    }

}
