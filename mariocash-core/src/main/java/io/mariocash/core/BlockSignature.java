package dev.zhihexireng.core;

import dev.zhihexireng.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.util.Arrays;

public class BlockSignature implements Cloneable {

    private byte[] signature;
    private byte[] bodyHash;

    private ECKey.ECDSASignature ecdsaSignature;
    private ECKey ecKeyPub;

    public BlockSignature(byte[] signature, byte[] bodyHash) throws SignatureException {
        this.signature = signature;
        this.bodyHash = bodyHash;

        this.ecdsaSignature = new ECKey.ECDSASignature(this.signature);
        this.ecKeyPub = ECKey.signatureToKey(this.bodyHash, this.ecdsaSignature);

        if (!this.ecKeyPub.verify(this.bodyHash, this.ecdsaSignature)) {
            throw new SignatureException();
        }
    }

    public BlockSignature(Wallet wallet, byte[] bodyHash) throws SignatureException {
        this(wallet.signHashedData(bodyHash), bodyHash);

        if (!Arrays.equals(this.ecKeyPub.getPubKey(), wallet.getPubicKey())) {
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

    public byte[] getBodyHash() {
        return this.bodyHash;
    }

    public String getBodyHashHexString() {
        return Hex.toHexString(this.bodyHash);
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
