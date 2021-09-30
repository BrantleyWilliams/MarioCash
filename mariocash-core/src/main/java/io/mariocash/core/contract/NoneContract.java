package dev.zhihexireng.core.contract;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;

public class NoneContract implements Contract {
    @Override
    public void init(StateStore stateStore, TransactionReceiptStore txReceiptStore) {
    }

    @Override
    public boolean invoke(TransactionHusk tx) {
        return true;
    }

    @Override
    public JsonObject query(JsonObject query) {
        return new JsonObject();
    }
}
