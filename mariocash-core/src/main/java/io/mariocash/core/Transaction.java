package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.format.TransactionFormat;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.util.SerializeUtils;

import java.io.IOException;
import java.io.Serializable;

public class Transaction implements Serializable, TransactionFormat {

    // Header
    private TransactionHeader header;

    // Data
    // TODO Data Object re modelling
    private String data;

    /**
     * Transaction Constructor
     *
     * @param header transaction header
     * @param data transaction data
     */
    public Transaction(TransactionHeader header, String data) {
        this.data = data;
        this.header = header;
    }

    /**
     * Transaction Constructor
     *
     * @param from account for creating transaction
     * @param data transaction data(Json)
     */
    public Transaction(Account from, JsonObject data) throws IOException {
        // 1. make data
        this.data = data.toString();

        // 2. make header
        byte[] bin = SerializeUtils.serialize(data);
        this.header = new TransactionHeader(from, HashUtil.sha256(bin), bin.length);
    }

    /**
     * get transaction hash
     *
     * @return transaction hash
     */
    public String getHashString() throws IOException {
        return this.header.getHashString();
    }

    /**
     * get transaction hash
     *
     * @return transaction hash
     */
    public byte[] getHash() throws IOException {
        return this.header.getHash();
    }

    /**
     * get transaction data
     *
     * @return tx data
     */
    public String getData() {
        return this.data;
    }

    /**
     * get Transaction Header
     *
     * @return
     */
    public TransactionHeader getHeader() {
        return header;
    }

    /**
     * print transaction
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.header.toString());
        buffer.append("transactionData=").append(this.data);

        return buffer.toString();
    }
}
