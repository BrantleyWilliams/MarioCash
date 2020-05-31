package dev.zhihexireng.contract;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.store.TransactionReceiptStore;

public class NoneContract implements Contract {
    @Override
    public void init(StateStore stateStore, TransactionReceiptStore txReciptStore) {

    }

    @Override
    public boolean invoke(TransactionHusk tx) throws Exception {
        return true;
    }

    @Override
    public JsonObject query(JsonObject qurey) throws Exception {
        return new JsonObject();
    }
}
