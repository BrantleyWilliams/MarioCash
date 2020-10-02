package dev.zhihexireng.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.TestUtils;
import dev.zhihexireng.contract.CoinContract;
import dev.zhihexireng.contract.StemContract;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeTest {
    private static final Logger log = LoggerFactory.getLogger(RuntimeTest.class);
    private final TransactionReceiptStore txReceiptStore = new TransactionReceiptStore();
    private final CoinContract coinContract = new CoinContract();
    private final StemContract stemContract = new StemContract();
    private Runtime runtime;
    private Wallet wallet;
    private String branchId;

    @Before
    public void setUp() throws IOException, InvalidCipherTextException {
        runtime = new Runtime(new StateStore(), txReceiptStore);
        wallet = new Wallet();
    }

    @Test
    public void invokeFromYeedTest() throws Exception {
        JsonArray params = new JsonArray();
        JsonObject param1 = new JsonObject();
        param1.addProperty("address", "0xe1980adeafbb9ac6c9be60955484ab1547ab0b76");
        JsonObject param2 = new JsonObject();
        param2.addProperty("amount", 100);
        params.add(param1);
        params.add(param2);

        JsonObject txObj = new JsonObject();
        txObj.addProperty("method", "transfer");
        txObj.add("params", params);

        TransactionHusk tx = new TransactionHusk(TestUtils.sampleTxObject(wallet, txObj));
        runtime.invoke(coinContract, tx);
    }

    @Test
    public void invokeFromStemTest() throws Exception {
        JsonObject branch = TestUtils.getSampleBranch1();
        branchId = TestUtils.getBranchId(branch);
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();

        param.addProperty("branchId", branchId);
        param.add("branch", branch);
        params.add(param);


        JsonObject txObj = new JsonObject();
        txObj.addProperty("method", "create");
        txObj.add("params", params);

        TransactionHusk tx = new TransactionHusk(TestUtils.sampleTxObject(null, txObj));
        runtime.invoke(stemContract, tx);

        String description = "hello world!";
        String updatedVersion = "0xf4312kjise099qw0nene76555484ab1547av8b9e";
        JsonObject updatedBranch = TestUtils.updateBranch(description, updatedVersion, branch, 0);

        params.remove(0);
        param.addProperty("branchId", branchId);
        param.add("branch", updatedBranch);
        params.add(param);

        txObj.addProperty("method", "update");
        txObj.add("params", params);

        tx = new TransactionHusk(TestUtils.sampleTxObject(null, txObj));
        runtime.invoke(stemContract, tx);
    }

    @Test
    public void queryToYeedTest() throws Exception {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("address", "0xe1980adeafbb9ac6c9be60955484ab1547ab0b76");
        params.add(param);

        assertThat(runtime.query(coinContract,
                TestUtils.createQuery("balanceOf", params))).isNotNull();
    }

    @Test
    public void queryToStemTest() throws Exception {
        invokeFromStemTest();

        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId",
                branchId);
        params.add(param);

        assertThat(runtime.query(stemContract,
                TestUtils.createQuery("getCurrentVersion", params))).isNotNull();
        log.debug("[getCurrentVersion] res => " + runtime.query(stemContract,
                TestUtils.createQuery("getCurrentVersion", params)));

        assertThat(runtime.query(stemContract,
                TestUtils.createQuery("getVersionHistory", params))).isNotNull();
        log.debug("[getVersionHistory] res => " + runtime.query(stemContract,
                TestUtils.createQuery("getVersionHistory", params)));

        assertThat(runtime.query(stemContract,
                TestUtils.createQuery("getAllBranchId", new JsonArray()))).isNotNull();
        log.debug("[getAllBranchId] res => " + runtime.query(stemContract,
                TestUtils.createQuery("getAllBranchId", params)));

        params.remove(0);
        param.remove("branchId");
        param.addProperty("key", "type");
        param.addProperty("value", "immunity");
        params.add(param);
        assertThat(runtime.query(stemContract, TestUtils.createQuery("search", params)))
                .isNotNull();
        log.debug("[Search | type | immunity] res => "
                + runtime.query(stemContract, TestUtils.createQuery("search", params)));

        param.addProperty("key", "name");
        param.addProperty("value", "TEST1");
        assertThat(runtime.query(stemContract, TestUtils.createQuery("search", params)))
                .isNotNull();
        log.debug("[Search | name | TEST1] res => "
                + runtime.query(stemContract, TestUtils.createQuery("search", params)));
    }
}
