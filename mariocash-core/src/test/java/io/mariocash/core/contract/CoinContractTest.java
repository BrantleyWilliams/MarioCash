package dev.zhihexireng.core.contract;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.TestUtils;
import dev.zhihexireng.contract.CoinContract;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoinContractTest {

    private CoinContract coinContract;

    @Before
    public void setUp() {
        StateStore<Long> stateStore = new StateStore<>();
        TransactionReceiptStore txReceiptStore = new TransactionReceiptStore();
        coinContract = new CoinContract();
        coinContract.init(stateStore, txReceiptStore);
    }

    @Test
    public void balanceTest() throws Exception {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("address", "0xe1980adeafbb9ac6c9be60955484ab1547ab0b76");
        params.add(param);

        JsonObject query = new JsonObject();
        query.addProperty("address", "0xe1980adeafbb9ac6c9be60955484ab1547ab0b76");
        query.addProperty("method", "balanceOf");
        query.add("params", params);

        JsonObject result = coinContract.query(query);
        assertThat(result).isNotNull();
    }

    @Test
    public void transferTest() throws Exception {
        Wallet wallet = new Wallet();

        TransactionHusk tx = new TransactionHusk(TestUtils.sampleTxObject(wallet));
        boolean result = coinContract.invoke(tx);
        assertThat(result).isTrue();
    }

    private JsonObject query(JsonObject query) throws Exception {
        return coinContract.query(query);
    }

    private Boolean invoke(TransactionHusk tx) throws Exception {
        return coinContract.invoke(tx);
    }

}
