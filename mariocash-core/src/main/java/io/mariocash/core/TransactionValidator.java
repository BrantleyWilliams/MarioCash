package dev.zhihexireng.core;

import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.util.ByteUtil;
import dev.zhihexireng.util.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SignatureException;

public class TransactionValidator {

    public TransactionValidator() {
    }

    public Boolean txSigValidate(Transaction tx) throws IOException,SignatureException {
        byte[] bin = SerializeUtils.serialize(tx.getData());

        ByteArrayOutputStream tmpTx = new ByteArrayOutputStream();

        tmpTx.write(tx.getHeader().getType());
        tmpTx.write(tx.getHeader().getVersion());
        tmpTx.write(HashUtil.sha3(bin));
        tmpTx.write(ByteUtil.longToBytes(tx.getHeader().getTimestamp()));
        tmpTx.write(ByteUtil.longToBytes(bin.length));

        byte[] tmpSignDataHash = HashUtil.sha3(tmpTx.toByteArray());

        ECKey.ECDSASignature sig = new ECKey.ECDSASignature(tx.getHeader().getSignature());
        ECKey keyFromSig = ECKey.signatureToKey(tx.getHeader().getSignDataHash(), sig);

        return keyFromSig.verify(tmpSignDataHash, sig);
    }

    public void txFormatValidate() {
        // todo transaction format validation
    }
}
