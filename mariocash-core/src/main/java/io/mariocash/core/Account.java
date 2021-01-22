package dev.zhihexireng.core;

import dev.zhihexireng.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

/**
 * Account Class.
 */
public class Account {

    // <Variable>
    private final ECKey key;
    private final byte[] address;

    /**
     * Account Constructor.
     * - generate account with new key
     */
    public Account() {
        this.key = new ECKey();
        this.address = this.key.getAddress();
    }

    /**
     * get Account Key.
     *
     * @return key
     */
    public ECKey getKey() {
        return key;
    }

    /**
     * get Account Address.
     *
     * @return address
     */
    public byte[] getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Account{"
                + "publicKey=" + Hex.toHexString(key.getPubKey())
                + ",address=" + Hex.toHexString(address)
                + '}';
    }

}
