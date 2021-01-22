/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.contract;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.TestUtils;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.store.StateStore;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class StemContractTest {

    private static final Logger log = LoggerFactory.getLogger(StemContractTest.class);

    private StemContract stemContract;
    private JsonObject referenceBranch;
    private String referenceBranchAddress;

    @Before
    public void setUp() {
        StateStore<JsonObject> stateStore = new StateStore<>();
        TransactionReceiptStore txReceiptStore = new TransactionReceiptStore();

        stemContract = new StemContract();
        stemContract.init(stateStore, txReceiptStore);

        referenceBranch = TestUtils.getSampleBranch1();
        BranchId referenceBranchId = BranchId.of(referenceBranch);
        referenceBranchAddress = referenceBranchId.toString();
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", referenceBranchAddress);
        param.add("branch", referenceBranch);
        params.add(param);
        stemContract.create(params);

        JsonObject referenceBranch2 = TestUtils.getSampleBranch2();
        BranchId referenceBranch2Id = BranchId.of(referenceBranch2);

        param.addProperty("branchId", referenceBranch2Id.toString());
        param.add("branch", referenceBranch2);
        stemContract.create(params);
    }

    @Test
    public void createTest() {
        JsonObject newBranch = TestUtils.getSampleBranch3(referenceBranchAddress);
        BranchId newBranchId = BranchId.of(newBranch);

        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", newBranchId.toString());
        param.add("branch", newBranch);
        params.add(param);
        assertThat(stemContract.create(params)).isNotNull();
    }

    @Test
    public void updateTest() {
        String branchId = referenceBranchAddress;
        String description = "Hello World!";
        String updatedVersion = "0xf4312kjise099qw0nene76555484ab1547av8b9e";
        JsonObject updatedBranch = TestUtils.updateBranch(description, updatedVersion,
                referenceBranch, 0);

        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", branchId);
        param.add("branch", updatedBranch);
        params.add(param);

        assertThat(stemContract.update(params)).isNotNull();

        updatedBranch = TestUtils.updateBranch(description, updatedVersion,
                referenceBranch, 1);
        params.remove(0);
        param.add("branch", updatedBranch);
        params.add(param);

        assertThat(stemContract.update(params)).isNotNull();
    }

    @Test
    public void searchTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("key", "type");
        param.addProperty("value", "immunity");
        params.add(param);

        assertThat(stemContract.search(params)).isNotNull();
        log.debug("Search [type | immunity] res => " + stemContract.search(params));

        param.addProperty("key", "name");
        param.addProperty("value", "TEST1");
        params.remove(0);
        params.add(param);

        assertThat(stemContract.search(params)).isNotNull();
        log.debug("Search [name | TEST1] res => " + stemContract.search(params));

        param.addProperty("key", "property");
        param.addProperty("value", "dex");
        params.remove(0);
        params.add(param);

        assertThat(stemContract.search(params)).isNotNull();
        log.debug("Search [property | dex] res => " + stemContract.search(params));

        param.addProperty("key", "owner");
        param.addProperty("value", "9e187f5264037ab77c87fcffcecd943702cd72c3");
        params.remove(0);
        params.add(param);

        assertThat(stemContract.search(params)).isNotNull();
        log.debug("Search [owner | 9e187f5264037ab77c87fcffcecd943702cd72c3] res => "
                + stemContract.search(params));

        param.addProperty("key", "symbol");
        param.addProperty("value", "TEST1");
        params.remove(0);
        params.add(param);

        assertThat(stemContract.search(params)).isNotNull();
        log.debug("Search [symbol | TEST1] res => " + stemContract.search(params));

        param.addProperty("key", "tag");
        param.addProperty("value", "0.1");
        params.remove(0);
        params.add(param);

        assertThat(stemContract.search(params)).isNotNull();
        log.debug("Search [tag | 0.1] res => " + stemContract.search(params));
    }

    @Test
    public void viewTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", referenceBranchAddress);
        params.add(param);

        //assertThat(stemContract.view(params)).isEmpty();
        assertThat(stemContract.view(params)).isNotEmpty(); // No owner validation
        log.debug(stemContract.view(params));
    }

    @Test
    public void getCurrentVersionTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", referenceBranchAddress);
        params.add(param);

        //assertThat(stemContract.getcurrentversion(params)).isEmpty();
        assertThat(stemContract.getcurrentversion(params)).isNotEmpty(); // No owner validation
        log.debug(stemContract.getcurrentversion(params));
    }

    @Test
    public void getVersionHistoryTest() {
        JsonArray params = new JsonArray();
        JsonObject param = new JsonObject();
        param.addProperty("branchId", referenceBranchAddress);
        params.add(param);

        assertThat(stemContract.getversionhistory(params).size()).isNotNull();
    }

    @Test
    public void getAllBranchIdTest() {
        assertThat(stemContract.getallbranchid(new JsonArray())).isNotNull();
    }
}
