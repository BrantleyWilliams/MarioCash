package dev.zhihexireng.core;

import dev.zhihexireng.crypto.ECKey;

public class TransactionSignature {

    private byte[] signature;
    private byte[] data;

    private ECKey.ECDSASignature ecdsaSignature;

    public TransactionSignature() {

    }
}
