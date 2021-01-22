package dev.zhihexireng.core;

import dev.zhihexireng.TestUtils;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.proto.Proto;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.SignatureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionHuskTest {

    private static final Logger log = LoggerFactory.getLogger(TransactionHuskTest.class);

    private TransactionHusk txHusk;

    @Before
    public void setUp() {
        this.txHusk = TestUtils.createTransferTxHusk();

        log.debug("Before Transaction: " + txHusk.toString());
        log.debug("Before Transaction address: " + txHusk.getAddress().toString() + "\n");
    }

    @Test
    public void shouldBeEquals() {
        TransactionHusk tx1 = TestUtils.createTransferTxHusk();
        TransactionHusk tx2 = TestUtils.createTransferTxHusk();
        assertThat(tx1).isNotEqualTo(tx2);
    }

    @Test
    public void transactionTest() {
        assertThat(txHusk).isNotNull();
        assert txHusk.getHash() != null;
    }

    @Test
    public void deserializeTransactionFromProtoTest() {
        Proto.Transaction protoTx = txHusk.getInstance();
        TransactionHusk deserializeTx = new TransactionHusk(protoTx);
        assert txHusk.getHash().equals(deserializeTx.getHash());
    }

    @Test
    public void testMakeTransaction() {
        TransactionHusk txHusk2 = TestUtils.createTransferTxHusk();

        log.debug("Transaction 2: " + txHusk2.toString());
        log.debug("Transaction 2 address: " + txHusk2.getAddress().toString());

        assertEquals(txHusk.getAddress().toString(), txHusk2.getAddress().toString());
    }

    @Test
    public void testGetAddressWithWallet() {
        TransactionHusk txHusk2 = TestUtils.createTransferTxHusk();
        Wallet wallet = TestUtils.wallet();
        assertArrayEquals(wallet.getAddress(), txHusk.getAddress().getBytes());
        assertEquals(txHusk.getAddress(), txHusk2.getAddress());
        assertArrayEquals(wallet.getAddress(), txHusk2.getAddress().getBytes());
    }

    @Test
    public void testGetAddressWithWalletAccount() throws IOException, InvalidCipherTextException {
        Account account = new Account();
        log.debug("Account: " + account.toString());
        log.debug("Account.address: " + Hex.toHexString(account.getAddress()));

        Wallet wallet = new Wallet(account.getKey(), "tmp/path", "nodePri.key", "Aa1234567890!");
        log.debug("Wallet: " + wallet.toString());
        log.debug("Wallet.address: " + Hex.toHexString(wallet.getAddress()));

        TransactionHusk txHusk1 = TestUtils.createBranchTxHusk(wallet);
        TransactionHusk txHusk2 = TestUtils.createBranchTxHusk(wallet);

        log.debug("Test Transaction1: " + txHusk1.toString());
        log.debug("Test Transaction1 Address: " + txHusk1.getAddress());

        log.debug("Test Transaction2: " + txHusk2.toString());
        log.debug("Test Transaction2 Address: " + txHusk2.getAddress());

        log.debug("Test Transaction1: " + txHusk1.toString());
        log.debug("Test Transaction1 Address: " + txHusk1.getAddress());

        log.debug("Test Transaction2: " + txHusk2.toString());
        log.debug("Test Transaction2 Address: " + txHusk2.getAddress());

        assertArrayEquals(wallet.getAddress(), account.getAddress());
        assertEquals(txHusk1.getAddress(), txHusk2.getAddress());
        assertArrayEquals(account.getAddress(), txHusk1.getAddress().getBytes());
    }

    @Test
    public void testGetAddressWithSig()
            throws IOException, InvalidCipherTextException, SignatureException {
        Account account = new Account();
        log.debug("Account: " + account.toString());
        log.debug("Account.address: " + Hex.toHexString(account.getAddress()));
        log.debug("Account.pubKey: " + Hex.toHexString(account.getKey().getPubKey()));

        Wallet wallet = new Wallet(account.getKey(), "tmp/path", "nodePri.key", "Aa1234567890!");
        log.debug("Wallet: " + wallet.toString());
        log.debug("Wallet.address: " + Hex.toHexString(wallet.getAddress()));
        log.debug("Wallet.pubKey: " + Hex.toHexString(wallet.getPubicKey()));

        TransactionHusk txHusk1 = TestUtils.createBranchTxHusk(wallet);
        log.debug("Test Transaction1: " + txHusk1.toString());
        log.debug("Test Transaction1 Address: " + txHusk1.getAddress());

        assertTrue(txHusk1.verify());
        assertArrayEquals(wallet.getAddress(), account.getAddress());
        assertArrayEquals(wallet.getAddress(), txHusk1.getAddress().getBytes());

        byte[] hashedRawData = txHusk1.getHashForSigning().getBytes();
        log.debug("hashedRawData: " + Hex.toHexString(hashedRawData));

        byte[] signatureBin = txHusk1.getInstance().getSignature().toByteArray();
        log.debug("signatureBin: " + Hex.toHexString(signatureBin));

        ECKey.ECDSASignature ecdsaSignature = new ECKey.ECDSASignature(signatureBin);
        ECKey key = ECKey.signatureToKey(hashedRawData, ecdsaSignature);

        byte[] address = key.getAddress();
        byte[] pubKey = key.getPubKey();

        log.debug("address: " + Hex.toHexString(address));
        log.debug("pubKey: " + Hex.toHexString(pubKey));

        assertArrayEquals(account.getAddress(), address);
        assertArrayEquals(account.getKey().getPubKey(), pubKey);
    }

    @Test
    public void shouldBeSignedTransaction() {
        txHusk.sign(TestUtils.wallet());

        assertThat(txHusk.isSigned()).isTrue();
        assertThat(txHusk.verify()).isTrue();
    }
}
