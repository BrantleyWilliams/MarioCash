package dev.zhihexireng.contract;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionReceipt;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ContractAdaptor implements Contract {
    protected static final Logger log = LoggerFactory.getLogger(ContractAdaptor.class);
    protected Map<String, Long> state;
    protected TransactionReceiptStore txReceiptStore;
    protected String sender;

    @Override
    public void init(StateStore stateStore, TransactionReceiptStore txReceiptStore) {
        this.state = stateStore.getState();
        this.txReceiptStore = txReceiptStore;
    }

    @Override
    public boolean invoke(TransactionHusk txHusk) throws Exception {
        String data = txHusk.getBody();
        JsonParser jsonParser = new JsonParser();
        JsonObject txBody = (JsonObject) jsonParser.parse(data);
        String method = txBody.get("method").getAsString().toLowerCase();
        this.sender = txHusk.getAddress().toString();
        JsonArray params = txBody.get("params").getAsJsonArray();

        if (!method.isEmpty()) {
            TransactionReceipt txReciept = (TransactionReceipt) this.getClass()
                    .getMethod(method, JsonArray.class)
                    .invoke(this, params);
            txReciept.setTransactionHash(txHusk.getHash().toString());
            txReceiptStore.put(txHusk.getHash().toString(), txReciept);
            return true;
        }
        return false;
    }

    @Override
    public JsonObject query(JsonObject query) throws Exception {
        this.sender = query.get("address").getAsString();
        String method = query.get("method").getAsString().toLowerCase();
        JsonArray params = query.get("params").getAsJsonArray();

        JsonObject result = new JsonObject();
        if (!method.isEmpty()) {
            Object res = this.getClass().getMethod(method, JsonArray.class)
                    .invoke(this, params);
            result.addProperty("result", res.toString());
            return result;
        }
        return null;
    }
}
