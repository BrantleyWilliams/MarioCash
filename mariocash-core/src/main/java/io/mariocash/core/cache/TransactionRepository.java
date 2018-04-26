package dev.zhihexireng.core.cache;

import dev.zhihexireng.core.Transaction;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 * The type Transaction repository.
 */
@Repository("mariocash.transaction")
public class TransactionRepository {

    private static final Logger log = LoggerFactory.getLogger(TransactionRepository.class);
    DB db = null;
    @Value("#{cacheManager.getCache('transactionPool')}")
    private ConcurrentMapCache transactionPool;

    public TransactionRepository() {
        // make database
        Options options = new Options();
        options.createIfMissing(true);
        try {
            // TODO resource path set by profile or setting file
            this.db = factory.open(new File("resources/db/transaction"), options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets transaction.
     *
     * @param hash the hash
     * @return the transaction
     */
    public Transaction getTransaction(String hashString) throws DecoderException {

        // check Cache
        Transaction tx = transactionPool.get(hashString, Transaction.class);
        log.debug("get transaction hash : " + hashString);

        if (tx == null) {
            tx = loadTransactionIfExist(hashString);
            if (tx != null) {
                transactionPool.putIfAbsent(hashString, tx);
            }
        }
        return tx;
    }

    public void flushPool() {
        transactionPool.clear();
    }

    /**
     * Add transaction int.
     *
     * @param transaction the transaction
     */
    public void addTransaction(Transaction transaction, boolean store) throws IOException {
        this.transactionPool.putIfAbsent(transaction.getHashString(), transaction);
        //todo: check from byte[] to object
        log.debug("add transaction hash : " + transaction.getHashString());

        if (store) {
            saveTransaction(transaction);
        }
    }

    /**
     * Save Transaction levelDB
     *
     * @param transaction
     */
    private void saveTransaction(Transaction transaction) {
        ObjectOutput out = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(transaction);
            out.flush();
            byte[] transactionBytes = bos.toByteArray();
            this.db.put(transaction.getHash(), transactionBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
                out.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    /**
     * Load Transaction levelDb
     *
     * @param hashString transaction Hash
     * @return Transaction or null
     */
    private Transaction loadTransactionIfExist(String hashString) throws DecoderException {
        Transaction transaction = null;
        if (hashString == null) {
            return null;
        }
        try {
            byte[] transactionBytes = this.db.get(Hex.decodeHex(hashString.toCharArray()));
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(transactionBytes));
            transaction = (Transaction) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // pass
        }
        return transaction;
    }


}
