package dev.zhihexireng.contract;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.Transaction;

public interface Contract {
    public boolean invoke(Transaction tx) throws Exception;
    public JsonObject query(JsonObject qurey) throws Exception;
}
