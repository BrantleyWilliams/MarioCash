package dev.zhihexireng.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.util.SerializeUtils;

import java.io.Serializable;

@Deprecated
public class Transaction implements Serializable {

    // Header
    private TransactionHeader header;

    // Data
    // TODO Data Object re modelling
    private String data;

    public Transaction() {
    }

    /**
     * Transaction Constructor
     *
     * @param header transaction header
     * @param data   transaction data
     */
    public Transaction(TransactionHeader header, String data) {
        this.data = data;
        this.header = header;
    }

    /**
     * Transaction Constructor
     *
     * @param data   transaction data(Json)
     */
    public Transaction(Wallet wallet, JsonObject data) {

        // 1. make data
        this.data = data.toString();

        // 2. make header
        byte[] bin = SerializeUtils.serialize(data);
        this.header = new TransactionHeader(wallet, HashUtil.sha3(bin), bin.length);
    }

    /**
     * get transaction hash
     *
     * @return transaction hash
     */
    @JsonIgnore
    public String getHashString() {
        return this.header.getHashString();
    }

    /**
     * get transaction hash
     *
     * @return transaction hash
     */
    @JsonIgnore
    public byte[] getHash() {
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
     * @return tx header
     */
    public TransactionHeader getHeader() {
        return header;
    }

    /**
     * print transaction
     */
    public String toString() {
        return header.toString() + "transactionData=" + data;
    }

    /**
     * Convert from Transaction.class to JSON string.
     * @return transaction as JsonObject
     */
    public JsonObject toJsonObject() {
        //todo: change to serialize method

        JsonObject jsonObject = this.getHeader().toJsonObject();
        jsonObject.add("data", new Gson().fromJson(this.data, JsonObject.class));

        return jsonObject;
    }

}
