package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.proto.BlockChainProto;
import dev.zhihexireng.util.SerializeUtils;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.Serializable;

public class Transaction implements Serializable {

    // Header
    private TransactionHeader header;

    // Data
    // TODO Data Object re modelling
    private String data;

    private Transaction(String data) {
        this.data = data;
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
        buffer.append("transactionData=").append(this.data.toString());

        return buffer.toString();
    }

    public static Transaction valueOf(BlockChainProto.Transaction tx) {
        Transaction transaction = new Transaction(tx.getData());
        transaction.header = TransactionHeader.valueOf(tx.getHeader());
        return transaction;
    }

    public static BlockChainProto.Transaction of(Transaction tx) {
        TransactionHeader header = tx.getHeader();
        return BlockChainProto.Transaction.newBuilder().setData(tx.getData())
                .setHeader(TransactionHeader.of(header)).build();
    }
}
