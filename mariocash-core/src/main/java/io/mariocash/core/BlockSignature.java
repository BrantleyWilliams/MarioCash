package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import dev.zhihexireng.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.util.Arrays;

public class BlockSignature implements Cloneable {

    private byte[] signature;
    private byte[] headerHash;

    private ECKey.ECDSASignature ecdsaSignature;
    private ECKey ecKeyPub;

    public BlockSignature(byte[] signature, byte[] headerHash) throws SignatureException {
        this.signature = signature;
        this.headerHash = headerHash;

        this.ecdsaSignature = new ECKey.ECDSASignature(this.signature);
        this.ecKeyPub = ECKey.signatureToKey(this.headerHash, this.ecdsaSignature);

        if (!this.ecKeyPub.verify(this.headerHash, this.ecdsaSignature)) {
            throw new SignatureException();
        }
    }

    public BlockSignature(Wallet wallet, byte[] headerHash) throws SignatureException {
        this(wallet.signHashedData(headerHash), headerHash);

        if (!Arrays.equals(this.ecKeyPub.getPubKey(), wallet.getPubicKey())) {
            throw new SignatureException();
        }
    }

    public BlockSignature(JsonObject jsonObject) throws SignatureException {
        this.signature = Hex.decode(jsonObject.get("signature").getAsString());
        this.headerHash = Hex.decode(jsonObject.get("headerHash").getAsString());

        this.ecdsaSignature = new ECKey.ECDSASignature(this.signature);
        this.ecKeyPub = ECKey.signatureToKey(this.headerHash, this.ecdsaSignature);

        if (!this.ecKeyPub.verify(this.headerHash, this.ecdsaSignature)) {
            throw new SignatureException();
        }
    }

    public long length() {
        return this.getSignature().length;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public String getSignatureHexString() {
        return Hex.toHexString(this.signature);
    }

    public byte[] getHeaderHash() {
        return this.headerHash;
    }

    public String getHeaderHashHexString() {
        return Hex.toHexString(this.headerHash);
    }

    public ECKey.ECDSASignature getEcdsaSignature() {
        return this.ecdsaSignature;
    }

    public ECKey getEcKeyPub() {
        return this.ecKeyPub;
    }

    @Override
    public BlockSignature clone() throws CloneNotSupportedException {
        return (BlockSignature) super.clone();
    }

}
