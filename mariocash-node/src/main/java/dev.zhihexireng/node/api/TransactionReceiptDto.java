package dev.zhihexireng.node.api;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.Account;
import dev.zhihexireng.core.Transaction;

import java.io.IOException;

public class TransactionReceiptDto {

    private int blockNumber;
    private int transactionIndex;
    private int status;
    private String blockHash;
    private String transactionHash;

    // private String contractAddress;
    // private String branchAddress;
    // private obj logs;
}
