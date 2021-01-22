package dev.zhihexireng.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import dev.zhihexireng.core.exception.InternalErrorException;
import dev.zhihexireng.core.exception.InvalidSignatureException;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.core.genesis.BlockInfo;
import dev.zhihexireng.core.genesis.TransactionInfo;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.trie.Trie;
import dev.zhihexireng.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block implements Cloneable {

    private static final Logger log = LoggerFactory.getLogger(Block.class);

    private static final int SIGNATURE_LENGTH = 65;

    private BlockHeader header;
    private byte[] signature;
    private BlockBody body;

    public Block(BlockHeader header, byte[] signature, BlockBody body) {
        this.header = header;
        this.signature = signature;
        this.body = body;
        verify();
    }

    public Block(BlockHeader header, Wallet wallet, BlockBody body) {
        this(header, wallet.signHashedData(header.getHashForSigning()), body);
    }

    public Block(JsonObject jsonObject) {
        this(new BlockHeader(jsonObject.get("header").getAsJsonObject()),
                Hex.decode(jsonObject.get("signature").getAsString()),
                new BlockBody(jsonObject.getAsJsonArray("body")));
    }

    public BlockHeader getHeader() {
        return header;
    }

    public byte[] getSignature() {
        return signature;
    }

    public BlockBody getBody() {
        return body;
    }

    private byte[] getHash() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        bao.write(this.header.toBinary());
        bao.write(this.signature);

        return HashUtil.sha3(bao.toByteArray());
    }

    String getHashHexString() throws IOException {
        return org.spongycastle.util.encoders.Hex.toHexString(this.getHash());
    }

    byte[] getPubKey() throws SignatureException {
        ECKey.ECDSASignature ecdsaSignature = new ECKey.ECDSASignature(this.signature);
        ECKey ecKeyPub = ECKey.signatureToKey(this.header.getHashForSigning(), ecdsaSignature);

        return ecKeyPub.getPubKey();
    }

    String getPubKeyHexString() throws SignatureException {
        return Hex.toHexString(this.getPubKey());
    }

    public byte[] getAddress() throws SignatureException {
        byte[] pubBytes = this.getPubKey();
        return HashUtil.sha3omit12(
                Arrays.copyOfRange(pubBytes, 1, pubBytes.length));
    }

    String getAddressHexString() throws SignatureException {
        return Hex.toHexString(getAddress());
    }

    public long length() {
        return this.header.length() + this.signature.length + this.body.length();
    }

    public boolean verify() {

        try {
            if (!this.verifyData()) {
                return false;
            }
        } catch (IOException e) {
            throw new InternalErrorException("verifyData error");
        }

        ECKey.ECDSASignature ecdsaSignature = new ECKey.ECDSASignature(this.signature);
        byte[] hashedHeader = this.header.getHashForSigning();
        ECKey ecKeyPub;
        try {
            ecKeyPub = ECKey.signatureToKey(hashedHeader, ecdsaSignature);
        } catch (SignatureException e) {
            throw new InvalidSignatureException(e);
        }

        return ecKeyPub.verify(hashedHeader, ecdsaSignature);
    }

    private boolean verifyCheckLengthNotNull(byte[] data, int length, String msg) {

        boolean result = !(data == null || data.length != length);

        if (!result) {
            log.debug(msg + " is not valid.");
        }

        return result;
    }

    /**
     * Verify a block about block format.
     *
     * @return true(success), false(fail)
     */
    private boolean verifyData() throws IOException {
        // TODO CheckByValidate Code
        boolean check = true;
        check &= verifyCheckLengthNotNull(
                this.header.getChain(), BlockHeader.CHAIN_LENGTH, "chain");
        check &= verifyCheckLengthNotNull(
                this.header.getVersion(), BlockHeader.VERSION_LENGTH, "version");
        check &= verifyCheckLengthNotNull(header.getType(), BlockHeader.TYPE_LENGTH, "type");
        check &= verifyCheckLengthNotNull(
                this.header.getPrevBlockHash(), BlockHeader.PREVBLOCKHASH_LENGTH, "prevBlockHash");
        check &= verifyCheckLengthNotNull(
                this.header.getMerkleRoot(), BlockHeader.MERKLEROOT_LENGTH, "merkleRootLength");
        check &= verifyCheckLengthNotNull(this.signature, SIGNATURE_LENGTH, "signature");
        check &= this.header.getIndex() >= 0;
        check &= this.header.getTimestamp() > 0;
        check &= !(this.header.getBodyLength() <= 0
                || this.header.getBodyLength() != this.getBody().length());
        check &= Arrays.equals(
                this.header.getMerkleRoot(), Trie.getMerkleRoot(this.body.getBody()));

        return check;
    }

    public JsonObject toJsonObject() {

        JsonObject jsonObject = new JsonObject();

        jsonObject.add("header", this.header.toJsonObject());
        jsonObject.addProperty("signature", Hex.toHexString(this.signature));
        jsonObject.add("body", this.body.toJsonArray());

        return jsonObject;
    }

    public String toString() {
        return this.toJsonObject().toString();
    }

    public String toStringPretty() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.toJsonObject());
    }

    public byte[] toBinary() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        bao.write(this.header.toBinary());
        bao.write(this.signature);
        bao.write(this.body.toBinary());

        return bao.toByteArray();
    }

    @Override
    public Block clone() throws CloneNotSupportedException {
        Block block = (Block) super.clone();
        block.header = this.header.clone();
        block.signature = this.signature.clone();
        block.body = this.body.clone();

        return block;
    }

    public Proto.Block toProtoBlock() {
        return toProtoBlock(this);
    }

    private Proto.Block toProtoBlock(Block block) {
        Proto.Block.Header protoHeader;
        protoHeader = Proto.Block.Header.newBuilder()
            .setChain(ByteString.copyFrom(block.getHeader().getChain()))
            .setVersion(ByteString.copyFrom(block.getHeader().getVersion()))
            .setType(ByteString.copyFrom(block.getHeader().getType()))
            .setPrevBlockHash(ByteString.copyFrom(block.getHeader().getPrevBlockHash()))
            .setIndex(ByteString.copyFrom(ByteUtil.longToBytes(block.getHeader().getIndex())))
            .setTimestamp(
                    ByteString.copyFrom(ByteUtil.longToBytes(block.getHeader().getTimestamp())))
            .setMerkleRoot(ByteString.copyFrom(block.getHeader().getMerkleRoot()))
            .setBodyLength(
                    ByteString.copyFrom(ByteUtil.longToBytes(block.getHeader().getBodyLength())))
            .build();

        Proto.TransactionList.Builder builder = Proto.TransactionList.newBuilder();
        for (Transaction tx : block.getBody().getBody()) {
            builder.addTransactions(Transaction.toProtoTransaction(tx));
        }

        Proto.Block protoBlock = Proto.Block.newBuilder()
                .setHeader(protoHeader)
                .setSignature(ByteString.copyFrom(block.getSignature()))
                .setBody(builder.build())
                .build();

        return protoBlock;
    }

    static Block toBlock(Proto.Block protoBlock) {

        BlockHeader blockHeader = new BlockHeader(
                protoBlock.getHeader().getChain().toByteArray(),
                protoBlock.getHeader().getVersion().toByteArray(),
                protoBlock.getHeader().getType().toByteArray(),
                protoBlock.getHeader().getPrevBlockHash().toByteArray(),
                ByteUtil.byteArrayToLong(protoBlock.getHeader().getIndex().toByteArray()),
                ByteUtil.byteArrayToLong(protoBlock.getHeader().getTimestamp().toByteArray()),
                protoBlock.getHeader().getMerkleRoot().toByteArray(),
                ByteUtil.byteArrayToLong(protoBlock.getHeader().getBodyLength().toByteArray())
        );

        List<Transaction> txList = new ArrayList<>();

        for (Proto.Transaction tx : protoBlock.getBody().getTransactionsList()) {
            txList.add(Transaction.toTransaction(tx));
        }

        BlockBody txBody = new BlockBody(txList);

        return new Block(blockHeader, protoBlock.getSignature().toByteArray(), txBody);

    }

    public static BlockHusk loadGenesis(InputStream branchInfoStream) {
        try {
            BlockInfo blockinfo = new ObjectMapper().readValue(branchInfoStream, BlockInfo.class);
            return new BlockHusk(Block.fromBlockInfo(blockinfo).toProtoBlock());
        } catch (Exception e) {
            throw new NotValidateException(e);
        }
    }

    private static Block fromBlockInfo(BlockInfo blockinfo) {
        BlockHeader blockHeader = new BlockHeader(
                Hex.decode(blockinfo.header.chain),
                Hex.decode(blockinfo.header.version),
                Hex.decode(blockinfo.header.type),
                Hex.decode(blockinfo.header.prevBlockHash),
                ByteUtil.byteArrayToLong(Hex.decode(blockinfo.header.index)),
                ByteUtil.byteArrayToLong(Hex.decode(blockinfo.header.timestamp)),
                Hex.decode(blockinfo.header.merkleRoot),
                ByteUtil.byteArrayToLong(Hex.decode(blockinfo.header.bodyLength))
        );

        List<Transaction> txList = new ArrayList<>();

        for (TransactionInfo txi : blockinfo.body) {
            txList.add(Transaction.fromTransactionInfo(txi));
        }

        BlockBody txBody = new BlockBody(txList);

        return new Block(blockHeader, Hex.decode(blockinfo.signature), txBody);
    }

}
