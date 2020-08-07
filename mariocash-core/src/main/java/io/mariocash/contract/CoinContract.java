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

public class CoinContract implements Contract {
    private static final Logger log = LoggerFactory.getLogger(CoinContract.class);
    private Map<String, Long> state;
    private TransactionReceiptStore txReceiptStore;
    private String sender;

    @Override
    public void init(StateStore stateStore, TransactionReceiptStore txReceiptStore) {
        this.state = stateStore.getState();
        this.txReceiptStore = txReceiptStore;
    }

    @Override
    public boolean invoke(TransactionHusk txHusk) throws Exception {

        String data = txHusk.getBody();
        JsonParser jsonParser = new JsonParser();
        JsonArray txBodyArray = (JsonArray) jsonParser.parse(data);
        JsonObject txBody = null;

        for (int i = 0; i < txBodyArray.size(); i++) {
            //todo: modify for 1 more tx_body
            txBody = txBodyArray.get(i).getAsJsonObject();
        }

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

    /**
     * Returns the balance of the account (query)
     *
     * @param params   account address
     */
    public Long balanceof(JsonArray params) {
        String address = params.get(0).getAsJsonObject().get("address").getAsString().toLowerCase();
        if (state.get(address) != null) {
            return state.get(address);
        }
        return 0L;
    }

    /**
     * Returns TransactionRecipt (invoke)
     */
    public TransactionReceipt transfer(JsonArray params) {
        log.info("\n transfer :: params => " + params);
        String to = params.get(0).getAsJsonObject().get("address").getAsString().toLowerCase();
        long amount = params.get(1).getAsJsonObject().get("amount").getAsInt();

        TransactionReceipt txRecipt = new TransactionReceipt();
        txRecipt.txLog.put("from", sender);
        txRecipt.txLog.put("to", to);
        txRecipt.txLog.put("amount", String.valueOf(amount));

        if (state.get(sender) != null) {
            long balanceOfFrom = state.get(sender);

            if (balanceOfFrom - amount < 0) {
                txRecipt.setStatus(0);
                log.info("\n[ERR] " + sender + " has no enough balance!");
            } else {
                balanceOfFrom -= amount;
                state.replace(sender, balanceOfFrom);
                if (state.get(to) != null) {
                    long balanceOfTo = state.get(to);
                    balanceOfTo += amount;
                    state.replace(to, balanceOfTo);
                } else {
                    state.put(to, amount);
                }
                log.info(
                        "\nBalance of From : " + state.get(sender)
                                + "\nBalance of To   : " + state.get(to));
            }
        } else {
            txRecipt.setStatus(0);
            log.info("\n[ERR] " + sender + " has no balance!");
        }
        return txRecipt;
    }
}
