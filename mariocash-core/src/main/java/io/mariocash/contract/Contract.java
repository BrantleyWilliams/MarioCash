package dev.zhihexireng.contract;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;

public interface Contract<V> {
    void init(StateStore<V> store, TransactionReceiptStore txReceiptStore);

    boolean invoke(TransactionHusk tx) throws Exception;

    JsonObject query(JsonObject query) throws Exception;
}
