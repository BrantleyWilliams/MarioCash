package dev.zhihexireng.core;

import com.google.gson.JsonArray;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.trie.Trie;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class BlockBody implements Serializable {

    private List<Transaction> transactionList;

    public BlockBody() {
    }

    /**
     * Instantiates a new Block body.
     *
     * @param transactionList the transaction list
     */
    public BlockBody(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public byte[] getMerkleRoot() {
        try {
            return Trie.getMerkleRoot(this.transactionList);
        } catch (IOException e) {
            throw new NotValidateException(e);
        }
    }

    public long getSize() {
        return this.transactionList.size(); // check byte
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("transactionList=>");
        for (Transaction tx : this.transactionList) {
            buffer.append(tx.toString());
        }
        return buffer.toString();
    }

    /**
     * Covert BlockBody.class to JsonArray
     * @return blockbody as JsonArray
     */
    public JsonArray toJsonArray() {
        JsonArray jsonArray = new JsonArray();

        for (Transaction tx : this.transactionList) {
            jsonArray.add(tx.toJsonObject());
        }

        return jsonArray;
    }

}




