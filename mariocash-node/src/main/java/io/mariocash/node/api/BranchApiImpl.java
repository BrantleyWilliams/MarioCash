package dev.zhihexireng.node.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.contract.StemContract;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.Runtime;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.node.controller.TransactionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AutoJsonRpcServiceImpl
public class BranchApiImpl implements BranchApi {

    private static final Logger log = LoggerFactory.getLogger(BranchApiImpl.class);
    private final BranchGroup branchGroup;
    private final Runtime runtime;

    @Autowired
    public BranchApiImpl(BranchGroup branchGroup, Runtime runtime) {
        this.branchGroup = branchGroup;
        this.runtime = runtime;
    }

    @Override
    public String createBranch(TransactionDto tx) {
        TransactionHusk addedTx = branchGroup.addTransaction(TransactionDto.of(tx));
        return addedTx.getHash().toString();
    }

    @Override
    public String updateBranch(TransactionDto tx) {
        TransactionHusk addedTx = branchGroup.addTransaction(TransactionDto.of(tx));
        return addedTx.getHash().toString();
    }

    @Override
    public String searchBranch(String data) throws Exception {
        return queryOf(data);
    }

    @Override
    public String viewBranch(String data) throws Exception {
        return queryOf(data);
    }

    @Override
    public String getCurrentVersionOfBranch(String data) throws Exception {
        return queryOf(data);
    }

    @Override
    public String getVersionHistoryOfBranch(String data) throws Exception {
        return queryOf(data);
    }

    @Override
    public String getAllBranchId(String data) throws Exception {
        return queryOf(data);
    }

    private String queryOf(String data) throws Exception {
        log.debug("[BranchAPI | queryOf( " + data + " )]");
        JsonParser jsonParser = new JsonParser();
        JsonObject query = (JsonObject) jsonParser.parse(data);
        return runtime.query(new StemContract(), query).toString();
    }
}
