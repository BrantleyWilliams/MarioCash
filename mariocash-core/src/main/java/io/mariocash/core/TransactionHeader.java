package dev.zhihexireng.core;

import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.util.ByteUtil;
import dev.zhihexireng.util.TimeUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.SignatureException;

public class TransactionHeader implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(TransactionHeader.class);
    //todo: check Logger type for serializing(transit) or performance

    private byte[] type;
    private byte[] version;
    private byte[] dataHash;
    private long dataSize;
    private long timestamp;
    private byte[] signature;

    public TransactionHeader() {
    }

    public TransactionHeader(byte[] type,
                             byte[] version,
                             byte[] dataHash,
                             long dataSize,
                             long timestamp,
                             byte[] signature) {
        this.type = type;
        this.version = version;
        this.dataHash = dataHash;
        this.dataSize = dataSize;
        this.timestamp = timestamp;
        this.signature = signature;
    }

    /**
     * TransactionHeader Constructor.
     *
     * @param dataHash data hash
     * @param dataSize data size
     */
    public TransactionHeader(Wallet wallet, byte[] dataHash, long dataSize) {
        if (dataHash == null) {
            throw new NotValidateException("dataHash is not valid");
        }

        if (dataSize <= 0) {
            throw new NotValidateException("dataSize is not valid");
        }

        this.type = new byte[4];
        this.version = new byte[4];
        this.dataHash = dataHash;
        this.dataSize = dataSize;
        this.timestamp = TimeUtils.time();
        this.signature = wallet.signHashedData(getDataHashForSigning());
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getVersion() {
        return version;
    }

    public byte[] getDataHash() {
        return dataHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDataSize() {
        return dataSize;
    }

    /**
     * Get the transaction hash.
     *
     * @return transaction hash
     */
    public byte[] getHash() {
        ByteArrayOutputStream transaction = new ByteArrayOutputStream();

        try {
            transaction.write(type);
            transaction.write(version);
            transaction.write(dataHash);
            transaction.write(ByteUtil.longToBytes(dataSize));
            transaction.write(ByteUtil.longToBytes(timestamp));
            transaction.write(signature);
        } catch (IOException e) {
            throw new NotValidateException(e);
        }

        return HashUtil.sha3(transaction.toByteArray());
    }

    /**
     * Get the transaction hash as hex string.
     *
     * @return transaction hash as hex string
     */
    public String getHashString() {
        return Hex.encodeHexString(this.getHash());
    }

    /**
     * Get transaction signature.
     *
     * @return transaction signature
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Get the data hash for signing.
     *
     * @return hash of sign data
     */
    public byte[] getDataHashForSigning() {
        ByteArrayOutputStream transaction = new ByteArrayOutputStream();

        try {
            transaction.write(type);
            transaction.write(version);
            transaction.write(dataHash);
            transaction.write(ByteUtil.longToBytes(dataSize));
            transaction.write(ByteUtil.longToBytes(timestamp));
        } catch (IOException e) {
            throw new NotValidateException(e);
        }

        return HashUtil.sha3(transaction.toByteArray());
    }

    /**
     * Get the address.
     *
     * @return address
     */
    public byte[] getAddress() {
        return ecKey().getAddress();
    }

    /**
     * Get the address.
     *
     * @return address
     */
    public String getAddressToString() {
        return Hex.encodeHexString(getAddress());
    }

    /**
     * Get the public key.
     *
     * @return public key
     */
    public byte[] getPubKey() {
        return ecKey().getPubKey();
    }

    /**
     * Get ECKey(include pubKey) using sig & signData.
     *
     * @return ECKey(include pubKey)
     */
    public ECKey ecKey() {
        try {
            return ECKey.signatureToKey(getDataHashForSigning(), signature);
        } catch (SignatureException e) {
            throw new NotValidateException(e);
        }
    }

    @Override
    public String toString() {
        return "TransactionHeader{"
                + "type=" + Hex.encodeHexString(type)
                + ", version=" + Hex.encodeHexString(version)
                + ", dataHash=" + Hex.encodeHexString(dataHash)
                + ", dataSize=" + dataSize
                + ", timestamp=" + timestamp
                + ", signature=" + Hex.encodeHexString(signature)
                + '}';
    }
}
