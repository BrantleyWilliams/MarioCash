package dev.zhihexireng.core;

import com.google.common.base.Strings;
import dev.zhihexireng.config.DefaultConfig;
import dev.zhihexireng.crypto.AESEncrypt;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.crypto.Password;
import dev.zhihexireng.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static dev.zhihexireng.config.Constants.PROPERTY_KEYPASSWORD;
import static dev.zhihexireng.config.Constants.PROPERTY_KEYPATH;

/**
 * Wallet Class.
 */
public class Wallet {

    // todo: check security

    private static final Logger logger = LoggerFactory.getLogger(Wallet.class);

    private ECKey key;
    private String keyPath;
    private String keyName;
    private byte[] address;
    private byte[] publicKey;

    /**
     * Wallet Constructor(generate key file).
     *
     * @param key      ECKey
     * @param keyPath  keyPath(directory)
     * @param keyName  keyName
     * @param password password
     * @throws IOException                IOException
     * @throws InvalidCipherTextException InvalidCipherTextException
     */
    public Wallet(ECKey key, String keyPath, String keyName, String password)
            throws IOException, InvalidCipherTextException {

        if (!Password.passwordValid(password)) {
            logger.error("Invalid Password");
            throw new IOException("Invalid Password");
        }

        if (key == null) {
            key = new ECKey();
        }

        this.key = key;
        this.keyPath = keyPath;
        this.keyName = keyName;
        this.address = key.getAddress();
        this.publicKey = key.getPubKey();

        byte[] kdfPass = Password.generateKeyDerivation(password.getBytes(), 32);
        byte[] encData = AESEncrypt.encrypt(key.getPrivKeyBytes(), kdfPass);

        File file = new File(this.keyPath, this.keyName);
        Set<PosixFilePermission> perms = new HashSet<>();

        if (file.exists()) {
            perms.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(file.toPath(), perms);
        }

        FileUtil.writeFile(file, encData);

        perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        Files.setPosixFilePermissions(file.toPath(), perms);
    }

    /**
     * Wallet Constructor as loading the key file.
     *
     * @param keyPath  keyPath(directory)
     * @param keyName  keyName
     * @param password password
     * @throws IOException                IOException
     * @throws InvalidCipherTextException InvalidCipherTextException
     */
    public Wallet(String keyPath, String keyName, String password)
            throws IOException, InvalidCipherTextException {

        byte[] encData = FileUtil.readFile(keyPath, keyName);
        byte[] kdfPass = Password.generateKeyDerivation(password.getBytes(), 32);

        byte[] priKey = AESEncrypt.decrypt(encData, kdfPass);
        this.key = ECKey.fromPrivate(priKey);
        this.keyPath = keyPath;
        this.keyName = keyName;
        this.address = key.getAddress();
        this.publicKey = key.getPubKey();

    }

    /**
     * Wallet constructor.
     *
     * @param keyPathName key file path + name
     * @param password    password
     * @throws IOException                IOException
     * @throws InvalidCipherTextException InvalidCipherTextException
     */
    public Wallet(String keyPathName, String password)
            throws IOException, InvalidCipherTextException {

        this(FileUtil.getFilePath(keyPathName), FileUtil.getFileName(keyPathName), password);
    }

    /**
     * Wallet constructor.
     *
     * @throws IOException                IOException
     * @throws InvalidCipherTextException InvalidCipherTextException
     */
    public Wallet() throws IOException, InvalidCipherTextException {
        this(new DefaultConfig());
    }

    /**
     * Wallet constructor with DefaultConfig.
     *
     * @param config DefaultConfig
     * @throws IOException                IOException
     * @throws InvalidCipherTextException InvalidCipherTextException
     */
    public Wallet(DefaultConfig config) throws IOException, InvalidCipherTextException {
        //todo: change password logic to CLI for security

        String keyFilePathName = config.getConfig().getString(PROPERTY_KEYPATH);
        String keyPassword = config.getConfig().getString(PROPERTY_KEYPASSWORD);

        if (Strings.isNullOrEmpty(keyFilePathName) || Strings.isNullOrEmpty(keyPassword)) {
            logger.error("Invalid keyPath or keyPassword");
            throw new IOException("Invalid keyPath or keyPassword");
        } else {
            // check password validation
            if (!Password.passwordValid(keyPassword)) {
                logger.error("Invalid keyPassword format"
                        + "(length:12-32, 1 more lower/upper/digit/special");
                throw new IOException("Invalid keyPassword format");
            }

            Path path = Paths.get(keyFilePathName);
            String keyPath = path.getParent().toString();
            String keyName = path.getFileName().toString();

            try {
                byte[] encData = FileUtil.readFile(keyPath, keyName);
                byte[] kdfPass = Password.generateKeyDerivation(keyPassword.getBytes(), 32);
                byte[] priKey = AESEncrypt.decrypt(encData, kdfPass);

                this.key = ECKey.fromPrivate(priKey);
                this.keyPath = keyPath;
                this.keyName = keyName;
                this.address = key.getAddress();
                this.publicKey = key.getPubKey();
            } catch (Exception e) {
                logger.debug("Key file is not exist. Create New key file.");

                try {
                    this.key = new ECKey();
                    this.keyPath = keyPath;
                    this.keyName = keyName;
                    this.address = key.getAddress();
                    this.publicKey = key.getPubKey();

                    byte[] kdfPass = Password.generateKeyDerivation(keyPassword.getBytes(), 32);
                    byte[] encData = AESEncrypt.encrypt(key.getPrivKeyBytes(), kdfPass);

                    File file = new File(this.keyPath, this.keyName);
                    Set<PosixFilePermission> perms = new HashSet<>();

                    if (file.exists()) {
                        perms.add(PosixFilePermission.OWNER_WRITE);
                        Files.setPosixFilePermissions(file.toPath(), perms);
                    }

                    FileUtil.writeFile(file, encData);

                    perms = new HashSet<>();
                    perms.add(PosixFilePermission.OWNER_READ);
                    Files.setPosixFilePermissions(file.toPath(), perms);

                } catch (IOException ioe) {
                    logger.error("Cannot generate the Key file at " + keyPath + keyName);
                    throw new IOException("Cannot generate the Key file");
                } catch (InvalidCipherTextException ice) {
                    logger.error("Error InvalidCipherTextException: " + keyPath + keyName);
                    throw new InvalidCipherTextException("Error InvalidCipherTextException");
                }
            }
        }

    }

    /**
     * Get wallet file keyPath.
     *
     * @return keyPath
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * Get keyName(filename, address).
     *
     * @return key name(filename)
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Get public key
     *
     * @return public key
     */
    public byte[] getPubicKey() {
        return key.getPubKey();
    }

    /**
     * Get address as byte[20]
     *
     * @return address as byte[20]
     */
    public byte[] getAddress() {
        return this.address;
    }

    /**
     * Get hex address
     *
     * @return hex address
     */
    public String getHexAddress() {
        return Hex.toHexString(address);
    }

    /**
     * Sign the plain data.
     *
     * @param data plain data
     * @return signature as byte[65]
     */
    public byte[] sign(byte[] data) {
        return key.sign(HashUtil.sha3(data)).toBinary();
    }

    /**
     * Sign the hashed data by sha3().
     *
     * @param hashedData hashed data
     * @return signature as byte[65]
     */
    public byte[] signHashedData(byte[] hashedData) {
        return key.sign(hashedData).toBinary();
    }

    /**
     * Verify the signature with hashed data.
     *
     * @param hashedData hashed Data
     * @param signature  signature
     * @return verification result
     */
    public boolean verifyHashedData(byte[] hashedData, byte[] signature) {

        ECKey.ECDSASignature sig = new ECKey.ECDSASignature(signature);

        return key.verify(hashedData, sig);
    }

    /**
     * Verify the signature with plain data.
     *
     * @param data      plain data for signed
     * @param signature signature
     * @return verification result
     */
    public boolean verify(byte[] data, byte[] signature) {

        ECKey.ECDSASignature sig = new ECKey.ECDSASignature(signature);

        return key.verify(HashUtil.sha3(data), sig);
    }

    /**
     * Verify the signature as static
     *
     * @param data signed data
     * @param signature  signature
     * @param hashed whether hashed data or not
     * @return verification result
     */
    public static boolean verify(byte[] data, byte[] signature, boolean hashed) {

        return verify(data, signature, hashed, null);
    }

    /**
     * Verify the signature with public key
     *
     * @param data signed data
     * @param signature  signature
     * @param hashed whether hashed data or not
     * @param pubKey public key for verifying
     * @return verification result
     */
    public static boolean verify(byte[] data, byte[] signature, boolean hashed, byte[] pubKey) {

        ECKey.ECDSASignature ecdsaSignature = new ECKey.ECDSASignature(signature);
        byte[] hashedData = hashed ? data : HashUtil.sha3(data);
        ECKey ecKeyPub;
        try {
            ecKeyPub = ECKey.signatureToKey(hashedData, ecdsaSignature);
        } catch (SignatureException e) {
            logger.debug("Invalid signature" + e.getMessage());
            return false;
        }

        if (pubKey != null && !Arrays.equals(ecKeyPub.getPubKey(), pubKey)) {
            logger.debug("Invalid signature");
            return false;
        }

        return ecKeyPub.verify(hashedData, ecdsaSignature);
    }

    /**
     * Gets node id.
     *
     * @return the node id string
     */
    public String getNodeId() {
        return Hex.toHexString(key.getNodeId());
    }

    @Override
    public String toString() {
        return "Wallet{"
                + "keyPath=" + keyPath
                + ", keyName=" + keyName
                + ", address=" + getHexAddress()
                + ", publicKey=" + Hex.toHexString(publicKey)
                + '}';
    }

}
