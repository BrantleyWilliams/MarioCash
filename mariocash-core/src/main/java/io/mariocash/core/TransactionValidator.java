package dev.zhihexireng.core;

import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.util.ByteUtil;
import dev.zhihexireng.util.SerializeUtils;

import java.io.ByteArrayOutputStream;

@Deprecated
public class TransactionValidator {

    public Boolean txSigValidate(Transaction tx) {
        byte[] bin = SerializeUtils.serialize(tx.getData());

        ByteArrayOutputStream tmpTx = new ByteArrayOutputStream();
        try {
            tmpTx.write(tx.getHeader().getType());
            tmpTx.write(tx.getHeader().getVersion());
            tmpTx.write(HashUtil.sha3(bin));
            tmpTx.write(ByteUtil.longToBytes(bin.length));
            tmpTx.write(ByteUtil.longToBytes(tx.getHeader().getTimestamp()));

            byte[] tmpSignDataHash = HashUtil.sha3(tmpTx.toByteArray());

            ECKey.ECDSASignature sig = new ECKey.ECDSASignature(tx.getHeader().getSignature());
            ECKey keyFromSig = ECKey.signatureToKey(tx.getHeader().getDataHashForSigning(), sig);

            return keyFromSig.verify(tmpSignDataHash, sig);

        } catch (Exception e) {
            throw new NotValidateException(e);
        }
    }

    public void txFormatValidate() {
        // todo transaction format validation
    }
}
