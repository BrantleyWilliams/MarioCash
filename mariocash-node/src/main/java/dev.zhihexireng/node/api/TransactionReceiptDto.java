package dev.zhihexireng.node.api;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.Account;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.TransactionPool;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class TransactionReceiptDto {

    private final TransactionPool txPool;

    public TransactionReceiptDto(TransactionPool txPool) {
        this.txPool = txPool;
    }

//    private int blockNumber;
//    private int transactionIndex;
//    private int status;
//    private String blockHash;
//    private String transactionHash;
//    private String contractAddress;
//    private String branchAddress;
//    private JsonObject logs;

    public String get(String hash) {
        Transaction tx = txPool.getTxByHash(hash);
        if (tx != null) {
            return tx.toString();
        }
        return "no transaction";
    }
}
