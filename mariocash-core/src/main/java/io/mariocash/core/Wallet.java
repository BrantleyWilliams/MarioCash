package dev.zhihexireng.core;

import dev.zhihexireng.crypto.AESEncrypt;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.crypto.Password;
import dev.zhihexireng.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

/**
 * Wallet Class.
 */
public class Wallet {

    // todo: check method, security

    private static final Logger logger = LoggerFactory.getLogger(Wallet.class);

    private ECKey key;
    private String path;
    private String keyName;

    /**
     * Wallet Constuctor.
     *
     * @param path     file path(directory)
     * @param password password
     * @throws InvalidCipherTextException InvalidCipherTextException
     * @throws IOException                IOException
     */
    public Wallet(String path, String password) throws InvalidCipherTextException, IOException {
        this(new ECKey(), path, password);
    }

    /**
     * Wallet Consturctor as generating a new key.
     *
     * @param key      ECKey
     * @param path     file path(directory)
     * @param password password
     * @throws IOException                IOException
     * @throws InvalidCipherTextException InvalidCipherTextException
     */
    public Wallet(ECKey key, String path, String password)
            throws IOException, InvalidCipherTextException {

        if (!Password.passwordValid(password)) {
            logger.error("password invalid : " + password);
            throw new IOException("Invalid Password");
        }

        this.key = key;
        this.path = path;
        this.keyName = Hex.toHexString(key.getAddress());

        byte[] kdfPass = Password.generateKeyDerivation(password.getBytes(), 32);
        byte[] encData = AESEncrypt.encrypt(key.getPrivKeyBytes(), kdfPass);

        FileUtil.writeFile(this.path, this.keyName, encData);

    }

    /**
     * Wallet Constructor as loading the keyfile.
     *
     * @param path     file path
     * @param fileName file name
     * @param password password
     * @throws IOException                IOException
     * @throws InvalidCipherTextException InvalidCipherTextException
     */
    public Wallet(String path, String fileName, String password)
            throws IOException, InvalidCipherTextException {

        byte[] encData = FileUtil.readFile(path, fileName);
        byte[] kdfPass = Password.generateKeyDerivation(password.getBytes(), 32);

        byte[] priKey = AESEncrypt.decrypt(encData, kdfPass);
        this.key = ECKey.fromPrivate(priKey);
        this.path = path;
        this.keyName = fileName;

    }

    /**
     * get wallet file path.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * get keyName(filename, address).
     *
     * @return key name(filename)
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * get ECKey object.
     *
     * @return ECKey
     */
    public ECKey getKey() {
        return key;
    }

}
