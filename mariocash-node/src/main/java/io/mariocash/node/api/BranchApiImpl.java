package dev.zhihexireng.node.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.node.controller.TransactionDto;

import java.util.List;
import java.util.Map;

public class BranchApiImpl implements BranchApi {
    @Override
    public String createBranch(TransactionDto tx) {
        return null;
    }

    @Override
    public String updateBranch(TransactionDto tx) {
        return null;
    }

    @Override
    public List<JsonObject> searchBranch(String key, String value) {
        return null;
    }

    @Override
    public String viewBranch(String branchId) {
        return null;
    }

    @Override
    public Map<String, String> getAllBranchName() {
        return null;
    }

    @Override
    public String getCurrentVersionOfBranch(String branchId) {
        return null;
    }

    @Override
    public JsonArray getVersionHistoryOfBranch(String branchId) {
        return null;
    }
}
