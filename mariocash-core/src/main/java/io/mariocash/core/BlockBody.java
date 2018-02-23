package dev.zhihexireng.core;

import dev.zhihexireng.trie.Trie;

import java.io.Serializable;
import java.util.List;

public class BlockBody implements Serializable {

    private List<Transaction> transactionList;

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

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public byte[] getMerkleRoot() {
        return Trie.getMerkleRoot(this.transactionList);
    }

    public long getSize() {
        return this.transactionList.size(); // check byte
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("transactionList=>");
        for (Transaction tx : this.transactionList) {
            buffer.append(tx.toString());
        }
        return buffer.toString();
    }

}




