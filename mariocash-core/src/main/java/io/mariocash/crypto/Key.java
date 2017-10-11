package dev.zhihexireng.crypto;

public class Key {

    private byte[] privateKey;
    private byte[] publicKey;

    /**
     * Instantiates a new Key.
     *
     * @param privateKey the private key
     * @param publicKey  the public key
     */
    public Key(byte[] privateKey, byte[] publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * Instantiates a new Key.
     */
    public Key() {
        this.privateKey = generateKey();
        this.publicKey = getPubKey(this.privateKey);
    }

    // <Get_set Method>
    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }


    // <Method>

    /**
     * generate PrivateKey (temporarily)
     *
     * @return private key
     */
    public byte[] generateKey() {
        // TODO implement
        return "prikey7890123456789012".getBytes();
    }

    /**
     * generate address (temporarily)
     *
     * @return address
     */
    public byte[] getAddress() {
        // TODO implement
        return "address8901234567890".getBytes();
    }

    /**
     * get public key with private key
     *
     * @param privateKey private key
     * @return public key
     */
    public byte[] getPubKey(byte[] privateKey) {
        // TODO implement
        return "pubkey7890123456789012".getBytes();
    }

}
