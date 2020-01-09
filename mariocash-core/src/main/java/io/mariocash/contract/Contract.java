package dev.zhihexireng.contract;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.store.TransactionReceiptStore;

public interface Contract {
    public void init(StateStore stateStore, TransactionReceiptStore txReciptStore);

    public boolean invoke(TransactionHusk tx) throws Exception;

    public JsonObject query(JsonObject qurey) throws Exception;
}
