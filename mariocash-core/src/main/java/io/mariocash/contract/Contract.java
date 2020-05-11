package dev.zhihexireng.contract;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.store.TransactionReceiptStore;

public interface Contract {
    void init(StateStore stateStore, TransactionReceiptStore txReciptStore);

    boolean invoke(TransactionHusk tx) throws Exception;

    JsonObject query(JsonObject qurey) throws Exception;
}
