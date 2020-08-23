package dev.zhihexireng.core.contract;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.TestUtils;
import dev.zhihexireng.contract.StemContract;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StemContractTest {

    private static final Logger log = LoggerFactory.getLogger(StemContractTest.class);

    private StemContract stemContract;
    private JsonObject referenceBranch;
    private String referenceBranchAddress;

    @Before
    public void setUp() {
        StateStore<JsonObject> stateStore = new StateStore<JsonObject>();
        TransactionReceiptStore txReceiptStore = new TransactionReceiptStore();

        stemContract = new StemContract();
        stemContract.init(stateStore, txReceiptStore);

        referenceBranch = TestUtils.getSampleBranch1();
        String referenceBranchId = TestUtils.getBranchId(referenceBranch);
        referenceBranchAddress = stemContract.create(referenceBranchId, referenceBranch);

        JsonObject referenceBranch2 = TestUtils.getSampleBranch2();
        String referenceBranch2Id = TestUtils.getBranchId(referenceBranch2);
        stemContract.create(referenceBranch2Id, referenceBranch2);
    }

    @Test
    public void createTest() {
        JsonObject newBranch = TestUtils.getSampleBranch3(referenceBranchAddress);
        String newBranchId = TestUtils.getBranchId(newBranch);
        assertThat(stemContract.create(newBranchId, newBranch)).isNotNull();
    }

    @Test
    public void updateTest() {
        String branchId = referenceBranchAddress;
        String description = "Hello World!";
        String updatedVersion = "0xf4312kjise099qw0nene76555484ab1547av8b9e";
        JsonObject updatedBranch = updateBranch(description, updatedVersion, referenceBranch, 0);
        String res = stemContract.update(branchId, updatedBranch);
        assertEquals(res, branchId);
        updatedBranch = updateBranch(description, updatedVersion, referenceBranch, 1);
        res = stemContract.update(branchId, updatedBranch);
        assertNotEquals(res, branchId);
    }

    @Test
    public void searchTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("key", "type");
        param.addProperty("value", "immunity");
        params.add(param);

        assertThat(stemContract.search(params).size()).isNotZero();

        param.addProperty("key", "name");
        param.addProperty("value", "TEST2");

        assertThat(stemContract.search(params).size()).isNotZero();

        param.addProperty("key", "property");
        param.addProperty("value", "dex");

        assertThat(stemContract.search(params).size()).isNotZero();

        param.addProperty("key", "owner");
        param.addProperty("value", "9e187f5264037ab77c87fcffcecd943702cd72c3");

        assertThat(stemContract.search(params).size()).isNotZero();

        param.addProperty("key", "symbol");
        param.addProperty("value", "TEST2");

        assertThat(stemContract.search(params).size()).isNotZero();

        param.addProperty("key", "tag");
        param.addProperty("value", "0.1");

        assertThat(stemContract.search(params).size()).isNotZero();
    }

    @Test
    public void viewTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", referenceBranchAddress);
        params.add(param);

        log.debug(stemContract.getcurrentversion(params));
        assertThat(stemContract.view(params)).isNotEmpty();
    }

    @Test
    public void getCurrentVersionTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", referenceBranchAddress);
        params.add(param);

        log.debug(stemContract.getcurrentversion(params));
        assertThat(stemContract.getcurrentversion(params)).isNotEmpty();
    }

    @Test
    public void getVersionHistoryTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", referenceBranchAddress);
        params.add(param);

        log.debug(stemContract.getversionhistory(params).getAsString());
        assertThat(stemContract.getversionhistory(params).size()).isNotZero();
    }

    private JsonObject updateBranch(String description, String version, JsonObject branch, Integer checkSum) {
        JsonObject updatedBranch = new JsonObject();
        updatedBranch.addProperty("name", checkSum == 0 ? branch.get("name").getAsString() : "HELLO");
        updatedBranch.addProperty("owner", branch.get("owner").getAsString());
        updatedBranch.addProperty("symbol", branch.get("symbol").getAsString());
        updatedBranch.addProperty("property", branch.get("property").getAsString());
        updatedBranch.addProperty("type", branch.get("type").getAsString());
        updatedBranch.addProperty("timestamp", branch.get("timestamp").getAsString());
        updatedBranch.addProperty("description", description);
        updatedBranch.addProperty("tag", branch.get("tag").getAsFloat());
        updatedBranch.addProperty("version", version);
        updatedBranch.add("versionHistory", branch.get("versionHistory").getAsJsonArray());
        updatedBranch.addProperty("reference_address", branch.get("reference_address").getAsString());
        updatedBranch.addProperty("reserve_address", branch.get("reserve_address").getAsString());

        return updatedBranch;
    }
}
