package dev.zhihexireng.core;

import dev.zhihexireng.trie.Trie;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class BlockBody implements Serializable {

    private final List<Transaction> transactionList;

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

    public byte[] getMerkleRoot() throws IOException {
        return Trie.getMerkleRoot(this.transactionList);
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

}




