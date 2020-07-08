package dev.zhihexireng.node.api;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.Runtime;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BranchApiImplTest {
    private static final Logger log = LoggerFactory.getLogger(BranchApiImplTest.class);
    private static final Runtime runtime = new Runtime(new TransactionReceiptStore());
    private static final BranchApiImpl branchApiImpl = new BranchApiImpl(runtime);
    private static final Map<String, JsonObject> branchStoreMock = new HashMap<>();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void create() {
    }

    @Test
    public void update() {
    }

    @Test
    public void search() {
    }

    @Test
    public void view() {
    }

    @Test
    public void getCurrentVersion() {
    }

    @Test
    public void getVersionHistory() {
    }
}
