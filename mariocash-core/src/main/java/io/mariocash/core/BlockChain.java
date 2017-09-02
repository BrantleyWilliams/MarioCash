package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import dev.zhihexireng.core.exception.NotValidteException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class BlockChain {
    private static final Logger log = LoggerFactory.getLogger(BlockChain.class);

    // <Variable>
    private Block genesisBlock;
    private Block prevBlock;
    private Map<Object, Block> blocks; // <blockheader_hash, block>
    private JsonObject packageInfo;


    // <Constructor>
    public BlockChain() throws IOException {
        this.packageInfo = new JsonObject();
        this.blocks = new HashMap<>();
    }

    // create blockchain & add genesis block
    public BlockChain(JsonObject packageInfo) throws IOException {
        this.packageInfo = packageInfo;
        this.blocks = new HashMap<>();
    }

    public JsonObject getPackageInfo() {
        return packageInfo;
    }

    // <Get_Set Method>
    public Block getGenesisBlock() {
        return this.genesisBlock;
    }

    public Block getPrevBlock() {
        return this.prevBlock;
    }

    public Map<Object, Block> getBlocks() {
        return blocks;
    }

    // <Method>

    public void addBlock(Block nextBlock) throws IOException, NotValidteException {
        if(isGenesisBlock(nextBlock)) {
                this.genesisBlock = nextBlock;
        } else if(!isValidNewBlock(prevBlock, nextBlock)) {
            throw new NotValidteException();
        }
        log.debug("blockHash : " + nextBlock.getBlockHash());
        // ADD List hash
        // TODO CHANGE DATABASE
        this.blocks.put(nextBlock.getBlockHash(), nextBlock);
        this.blocks.put(nextBlock.getHeader().getIndex(), nextBlock);
        this.prevBlock = nextBlock;
    }

    private boolean isGenesisBlock(Block newBlock) {
        return genesisBlock == null && prevBlock == null && newBlock.getHeader().getIndex() == 0;
    }

    private boolean isValidNewBlock(Block prevBlock, Block nextBlock) throws IOException {
        if (prevBlock == null) return true;
        BlockHeader prevBlockHeader = prevBlock.getHeader();
        BlockHeader nextBlockHeader = nextBlock.getHeader();
        log.debug(" prev : " + prevBlock.getBlockHash());
        log.debug(" new : " + nextBlock.getBlockHash());

        if (prevBlockHeader.getIndex() + 1 != nextBlockHeader.getIndex()) {
            log.warn("invalid index: prev:{} / new:{}", prevBlockHeader.getIndex(), nextBlockHeader.getIndex());
            return false;
        } else if (!Arrays.equals(prevBlockHeader.getBlockHash(), nextBlockHeader.getPrevBlockHash())) {
            log.warn("invalid previous hash");
            return false;
        }

        return true;
    }

    public int size() {
        return blocks.size()/2;
    }

    public boolean isValidChain() throws IOException {
        return isValidChain(this);
    }

    public boolean isValidChain(BlockChain blockChain) throws IOException {
        if(blockChain.getPrevBlock() != null){
            Block block = blockChain.getPrevBlock(); // Get Last Block
            while(block.getHeader().getIndex() != 0L) {
                block = blockChain.getBlockByHash(Hex.encodeHexString(block.getHeader().getPrevBlockHash()));
            }
            return block.getHeader().getIndex() == 0L;
        }
        return true;
    }

    public Block getBlockByIndex(long index) {
        return blocks.get(new Long(index));
    }

    public Block getBlockByHash(String hash) {
        return blocks.get(hash);
    }

    public Block getBlockByHash(byte[] hash) {
        return blocks.get(Hex.encodeHexString(hash));
    }


    public void replaceChain(BlockChain otherChain) throws IOException {
        if(isValidChain(otherChain) && otherChain.size() > this.size()) {
            log.info("Received blockchain is valid. Replacing current blockchain with received " +
                    "blockchain");
            this.blocks = otherChain.blocks;
            //TODO broadcastLatest();
        } else {
            log.info("Received blockchain invalid");
        }
    }

    public boolean isGenesisBlockChain() {
        return (this.prevBlock == null);
    }

    public void printBlockChain() {
        // TODO CHAINGE toString overwrite
        System.out.println("BlockChain");
        System.out.println("genesisBlock=");
        this.genesisBlock.printBlock();
        System.out.println("prevBlock=");
        if(this.prevBlock != null) this.prevBlock.printBlock();

        System.out.println("\nBlockChain");
        for (Object key : this.blocks.keySet()) {
            System.out.print("-"+this.blocks.get(key).getHeader().getIndex());
        }
    }

    public void clear() {
        this.blocks.clear();
        this.prevBlock = null;
        this.genesisBlock = null;
    }
}
