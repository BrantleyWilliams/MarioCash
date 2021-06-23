package dev.zhihexireng.core;

import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.contract.Contract;
import dev.zhihexireng.core.event.BranchEventListener;
import dev.zhihexireng.core.event.ContractEventListener;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.exception.InvalidSignatureException;
import dev.zhihexireng.core.exception.NonExistObjectException;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.core.store.BlockStore;
import dev.zhihexireng.core.store.TransactionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class BlockChain {

    private static final Logger log = LoggerFactory.getLogger(BlockChain.class);

    // <Variable>
    private final BlockHusk genesisBlock;
    private final List<BranchEventListener> listenerList = new ArrayList<>();

    private final BlockStore blockStore;
    private final TransactionStore transactionStore;
    private final Contract contract;
    private final Runtime<?> runtime;

    private BlockHusk prevBlock;
    private String branchName;

    public BlockChain(BlockHusk genesisBlock, BlockStore blockStore,
                      TransactionStore transactionStore, Contract contract, Runtime runtime) {
        this.genesisBlock = genesisBlock;
        this.blockStore = blockStore;
        this.transactionStore = transactionStore;
        this.contract = contract;
        this.runtime = runtime;
        loadBlockChain();
    }

    private void loadBlockChain() {
        try {
            prevBlock = blockStore.get(genesisBlock.getHash());
        } catch (NonExistObjectException e) {
            for (TransactionHusk tx : genesisBlock.getBody()) {
                transactionStore.put(tx.getHash(), tx);
            }
            blockStore.put(genesisBlock.getHash(), genesisBlock);
            prevBlock = genesisBlock;
            batchTxs(genesisBlock);
        }
    }

    public void init(ContractEventListener contractEventListener) {
        contract.setListener(contractEventListener);
        for (int i = 0; i < blockStore.size(); i++) {
            BlockHusk storedBlock = blockStore.get(i);
            executeAllTx(new TreeSet<>(storedBlock.getBody()));
            log.debug("Load idx=[{}], tx={}, branch={}, blockHash={}", storedBlock.getIndex(),
                    storedBlock.getBody().size(), storedBlock.getBranchId(), storedBlock.getHash());
            this.prevBlock = storedBlock;
        }
    }

    public void addListener(BranchEventListener listener) {
        listenerList.add(listener);
    }

    public Contract getContract() {
        return contract;
    }

    Runtime<?> getRuntime() {
        return runtime;
    }

    void generateBlock(Wallet wallet) {
        BlockHusk block = new BlockHusk(wallet,
                new ArrayList<>(transactionStore.getUnconfirmedTxs()), getPrevBlock());
        addBlock(block, true);
    }

    Collection<TransactionHusk> getRecentTxs() {
        return transactionStore.getRecentTxs();
    }

    List<TransactionHusk> getUnconfirmedTxs() {
        return new ArrayList<>(transactionStore.getUnconfirmedTxs());
    }

    long countOfTxs() {
        return transactionStore.countOfTxs();
    }

    public BranchId getBranchId() {
        return genesisBlock.getBranchId();
    }

    public String getBranchName() {
        return branchName;
    }

    void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    BlockHusk getGenesisBlock() {
        return this.genesisBlock;
    }

    public BlockHusk getPrevBlock() {
        return this.prevBlock;
    }

    /**
     * Gets last block index.
     *
     * @return the last block index
     */
    public long getLastIndex() {
        if (isGenesisBlockChain()) {
            return 0;
        }
        return prevBlock.getIndex();
    }

    /**
     * Add block.
     *
     * @param nextBlock the next block
     * @throws NotValidateException the not validate exception
     */
    public BlockHusk addBlock(BlockHusk nextBlock, boolean broadcast) {
        if (blockStore.contains(nextBlock.getHash())) {
            return null;
        }
        if (!isValidNewBlock(prevBlock, nextBlock)) {
            throw new NotValidateException("Invalid to chain");
        }
        executeAllTx(new TreeSet<>(nextBlock.getBody()));
        this.blockStore.put(nextBlock.getHash(), nextBlock);
        this.prevBlock = nextBlock;
        log.debug("Added idx=[{}], tx={}, branch={}, blockHash={}", nextBlock.getIndex(),
                nextBlock.getBody().size(), getBranchId().toString(), nextBlock.getHash());
        batchTxs(nextBlock);
        if (!listenerList.isEmpty() && broadcast) {
            listenerList.forEach(listener -> listener.chainedBlock(nextBlock));
        }
        return nextBlock;
    }

    private boolean isValidNewBlock(BlockHusk prevBlock, BlockHusk nextBlock) {
        if (prevBlock == null) {
            return true;
        }
        log.trace(" prev : " + prevBlock.getHash());
        log.trace(" new : " + nextBlock.getHash());

        if (prevBlock.getIndex() + 1 != nextBlock.getIndex()) {
            log.warn("invalid index: prev:{} / new:{}", prevBlock.getIndex(), nextBlock.getIndex());
            return false;
        } else if (!prevBlock.getHash().equals(nextBlock.getPrevHash())) {
            log.warn("invalid previous hash={}", prevBlock.getHash());
            return false;
        }

        return true;
    }

    public TransactionHusk addTransaction(TransactionHusk tx) {
        if (transactionStore.contains(tx.getHash())) {
            throw new FailedOperationException("Duplicated " + tx.getHash().toString()
                    + " Transaction");
        } else if (!tx.verify()) {
            throw new InvalidSignatureException();
        }

        try {
            transactionStore.put(tx.getHash(), tx);
            if (!listenerList.isEmpty()) {
                listenerList.forEach(listener -> listener.receivedTransaction(tx));
            }
            return tx;
        } catch (Exception e) {
            throw new FailedOperationException("Transaction");
        }
    }

    public long size() {
        return blockStore.size();
    }

    /**
     * Is valid chain boolean.
     *
     * @return the boolean
     */
    boolean isValidChain() {
        return isValidChain(this);
    }

    /**
     * Is valid chain boolean.
     *
     * @param blockChain the block chain
     * @return the boolean
     */
    private boolean isValidChain(BlockChain blockChain) {
        if (blockChain.getPrevBlock() != null) {
            BlockHusk block = blockChain.getPrevBlock(); // Get Last Block
            while (block.getIndex() != 0L) {
                block = blockChain.getBlockByHash(block.getPrevHash());
            }
            return block.getIndex() == 0L;
        }
        return true;
    }

    public BlockHusk getBlockByIndex(long idx) {
        return blockStore.get(idx);
    }

    /**
     * Gets block by hash.
     *
     * @param hash the hash
     * @return the block by hash
     */
    public BlockHusk getBlockByHash(String hash) {
        return getBlockByHash(new Sha3Hash(hash));
    }

    /**
     * Gets block by hash.
     *
     * @param hash the hash
     * @return the block by hash
     */
    public BlockHusk getBlockByHash(Sha3Hash hash) {
        return blockStore.get(hash);
    }

    /**
     * Gets transaction by hash.
     *
     * @param hash the hash
     * @return the transaction by hash
     */
    TransactionHusk getTxByHash(Sha3Hash hash) {
        return transactionStore.get(hash);
    }

    /**
     * Is genesis block chain boolean.
     *
     * @return the boolean
     */
    private boolean isGenesisBlockChain() {
        return (this.prevBlock == null);
    }

    // TODO execute All Transaction
    private List<Boolean> executeAllTx(Set<TransactionHusk> txList) {
        return txList.stream().map(this::executeTransaction).collect(Collectors.toList());
    }

    private boolean executeTransaction(TransactionHusk tx) {
        try {
            return runtime.invoke(contract, tx);
        } catch (Exception e) {
            log.error("executeTransaction Error" + e);
            return false;
        }
    }

    private void batchTxs(BlockHusk block) {
        if (block == null || block.getBody() == null) {
            return;
        }
        Set<Sha3Hash> keys = new HashSet<>();

        for (TransactionHusk tx : block.getBody()) {
            keys.add(tx.getHash());
        }
        transactionStore.batch(keys);
    }

    @Override
    public String toString() {
        return "BlockChain{"
                + "genesisBlock=" + genesisBlock
                + ", prevBlock=" + prevBlock
                + ", height=" + this.getLastIndex()
                + '}';
    }

    public void close() {
        this.blockStore.close();
        this.transactionStore.close();
    }

    public String toStringStatus() {
        StringBuilder builder = new StringBuilder();

        builder.append("[BlockChain Status]\n")
                .append("genesisBlock=")
                .append(genesisBlock.getHash()).append("\n").append("currentBlock=" + "[")
                .append(prevBlock.getIndex()).append("]").append(prevBlock.getHash()).append("\n");

        String prevBlockHash = this.prevBlock.getPrevHash().toString();
        if (prevBlockHash == null) {
            prevBlockHash = "";
        }

        do {
            builder.append("<-- " + "[")
                    .append(blockStore.get(new Sha3Hash(prevBlockHash)).getIndex())
                    .append("]").append(prevBlockHash).append("\n");

            prevBlockHash = blockStore.get(new Sha3Hash(prevBlockHash)).getPrevHash().toString();

        } while (prevBlockHash != null
                && !prevBlockHash.equals(
                    "0000000000000000000000000000000000000000000000000000000000000000"));

        return builder.toString();

    }
}
