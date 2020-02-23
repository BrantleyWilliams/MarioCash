package dev.zhihexireng.node.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import com.google.gson.JsonObject;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.node.NodeManagerImpl;
import dev.zhihexireng.node.TestUtils;
import dev.zhihexireng.node.controller.TransactionDto;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SignatureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TransactionApiImplTest {

    private static final Logger log = LoggerFactory.getLogger(TransactionApi.class);
    private static final BlockApi blockApi = new JsonRpcConfig().blockApi();
    private static final TransactionApi txApi = new JsonRpcConfig().transactionApi();

    private final TransactionApiImpl txApiImpl = new TransactionApiImpl(new NodeManagerImpl());
    private final String address = "0x407d73d8a49eeb85d32cf465507dd71d507100c1";
    private final String tag = "latest";
    private final String hashOfTx =
            "0xbd729cb4ecbcbd3fc66bedb43dbb856f5e71ebefff95fc9503b92921b8466bab";
    private final String hashOfBlock =
            "0x76a9fa4681a8abf94618543872444ba079d5302203ac6a5b5b2087a9f56ea8bf";
    private final int blockNumber = 1;
    private final int txIndexPosition = 1;
    private Wallet wallet;

    @Before
    public void setUp() throws IOException, InvalidCipherTextException {
        this.wallet = new Wallet();
    }

    @Test
    public void blockApiIsNotNull() {
        assertThat(blockApi).isNotNull();
    }

    @Test
    public void txApiIsNotNull() {
        assertThat(txApi).isNotNull();
    }

    @Test
    public void getTransactionCountTest() {
        try {
            assertThat(txApi.getTransactionCount(address, tag)).isNotZero();
        } catch (Exception exception) {
            log.debug("\n\ngetTransactionCountTest :: exception => " + exception);
        }
    }

    @Test
    public void getBlockTransactionCountByHashTest() {
        try {
            assertThat(txApi.getBlockTransactionCountByHash(hashOfTx)).isNotZero();
        } catch (Exception exception) {
            log.debug("\n\ngetBlockTransactionCountByHashTest :: exception => " + exception);
        }
    }

    @Test
    public void getBlockTransactionCountByNumberTest() {
        try {
            assertThat(txApi.getBlockTransactionCountByNumber(blockNumber)).isNotZero();
        } catch (Exception exception) {
            log.debug("\n\ngetBlockTransactionCountByNumberTest :: exception => " + exception);
        }
    }

    @Test
    public void getTransactionByHashTest() {
        try {
            TransactionHusk tx = TestUtils.createTxHusk();

            txApi.sendTransaction(TransactionDto.createBy(tx));
            assertThat(txApi.getTransactionByHash(hashOfTx)).isNotNull();
        } catch (Exception exception) {
            log.debug("\n\ngetTransactionByHashTest :: exception => " + exception);
        }
    }

    @Test
    public void getTransactionByBlockHashAndIndexTest() {
        try {
            TransactionHusk tx = new TransactionHusk(getJson()).sign(wallet);
            if (txApi.sendTransaction(TransactionDto.createBy(tx)) != null) {
                Thread.sleep(10000);
                String hashOfBlock = blockApi.getBlockByHash("1", true).getHash().toString();
                assertThat(hashOfBlock).isNotEmpty();
                assertThat(txApi.getTransactionByBlockHashAndIndex(hashOfBlock, 0)).isNotNull();
            } else {
                log.error("Send Transaction Failed!");
            }
        } catch (Exception exception) {
            log.debug("\n\ngetTransactionByBlockHashAndIndexTest :: exception => " + exception);
        }
    }

    @Test
    public void getTransactionByBlockNumberAndIndexTest() {
        try {
            assertThat(txApi.getTransactionByBlockNumberAndIndex(blockNumber, txIndexPosition))
                    .isNotNull();
        } catch (Exception exception) {
            log.debug("\n\ngetTransactionByBlockNumberAndIndexTest :: exception => " + exception);
        }
    }

    @Test
    public void getTransactionReceiptTest() {
        try {
            assertThat(txApi.getTransactionReceipt(hashOfTx)).isNotNull();
        } catch (Exception exception) {
            log.debug("\n\ngetTransactionReceiptTest :: exception => " + exception);
        }
    }

    @Test
    public void checkTransactionJsonFormat() throws IOException {
        TransactionHusk tx = TestUtils.createTxHusk();
        ObjectMapper objectMapper = TestUtils.getMapper();
        log.debug("\n\nTransaction Format : "
                + objectMapper.writeValueAsString(TransactionDto.createBy(tx)));
    }

    @Test
    public void sendTransactionTest() {
        // Get Transaction of JsonString as Param
        JsonObject txObj = new JsonObject();
        // curl -H "Content-Type:application/json" -d '{"id":"417871823","jsonrpc":"2.0","method":"sendTransaction","params":{"tx":{"type":"AAAAAQ==","version":"AAAAAQ==","data":"{\"operator\":\"transfer\",\"to\":\"aaa2aaab0fb041c5cb2a60a12291cbc3097352bb\",\"amount\":\"5000\"}","dataHash":"pSVVRYkZoZ/vwloCiMYOOvVetfjH/v2H72cCQQPxcCQ=","dataSize":87,"timestamp":37359937272746,"signature":"HKFdNaoBNeUEd1sLLBm1/Wy3x/GvJFP1PA86x8OTbxV5FypW/GZ2t4tFwp+giBnU/6BUZni8QTJFyNqRznl7ZmY=","address":"6f19c769c78513a3a60a3618c6a11eb9a886086a","hash":"078ecf76b29485b6cfd4b929e944efc5f290e7428f8179c86a9acf8c22b9907e"}}}' localhost:8080/api/transaction
        txObj.addProperty("operator", "transfer");
        txObj.addProperty("to", "aaa2aaab0fb041c5cb2a60a12291cbc3097352bb");
        txObj.addProperty("amount", "5000");

        TransactionHusk tx = new TransactionHusk(txObj).sign(wallet);

        // Request Transaction with jsonStr
        try {
            assertThat(txApi.sendTransaction(TransactionDto.createBy(tx))).isNotEmpty();
        } catch (Exception exception) {
            log.debug("\n\njsonStringToTxTest :: exception => " + exception);
        }
    }

    @Test
    public void sendRawTransactionTest() {
        // Create an input parameter
        byte[] type = new byte[4];
        byte[] version = new byte[4];
        byte[] dataHash = new byte[32];
        type = "0000".getBytes();
        version = "0000".getBytes();
        dataHash = Base64.decode("bQ4ti+Xk4rGhhFrfNDuMmt+KMw0yVRL0rsfAAUEXASM=");
        byte[] timestamp = Longs.toByteArray(Long.parseLong("155810745733540"));
        byte[] dataSize = Longs.toByteArray((long) 38);

        byte[] signature = new byte[65];
        signature = Base64.decode("HMddN4GjlGPV4x26730eQoHwS9DVmGg0iXmyeJG4H0kqM8UffWs"
                + "QwARCGHnLa4Su7QOsfEUjP65oEs1fxWKUT8k=");
        byte[] data = "{\"id\":\"0\",\"name\":\"Rachael\",\"age\":\"27\"}".getBytes();

        int totalLength = type.length + version.length + dataHash.length + timestamp.length
                        + dataSize.length + signature.length + data.length;

        ByteBuffer bb = ByteBuffer.allocate(totalLength);
        bb.put(type);
        bb.put(version);
        bb.put(dataHash);
        bb.put(timestamp);
        bb.put(dataSize);
        bb.put(signature);
        bb.put(data);

        byte[] input = bb.array();

        // Request Transaction with byteArr
        try {
            // Convert byteArray to Transaction
            assertThat(txApi.sendRawTransaction(input)).isNotEmpty();
        } catch (Exception exception) {
            log.debug("\n\nsendRawTransactionTest :: exception => " + exception);
        }
    }

    @Test
    public void newPendingTransactionFilterTest() {
        try {
            assertThat(txApi.newPendingTransactionFilter()).isNotZero();
        } catch (Exception exception) {
            log.debug("\n\njsonStringToTxTest :: exception => " + exception);
        }
    }

    @Test
    public void transactionApiImplTest() {
        try {
            assertThat(1).isEqualTo(txApiImpl.getTransactionCount(address, tag));
            assertThat(2).isEqualTo(txApiImpl.getTransactionCount(address, blockNumber));
            assertThat(3).isEqualTo(txApiImpl.getBlockTransactionCountByHash(hashOfBlock));
            assertThat(4).isEqualTo(txApiImpl.getBlockTransactionCountByNumber(blockNumber));
            assertThat(5).isEqualTo(txApiImpl.getBlockTransactionCountByNumber(tag));
            assertThat(6).isEqualTo(txApiImpl.newPendingTransactionFilter());
        } catch (Exception exception) {
            log.debug("\n\ntransactionApiImplTest :: exception => " + exception);
        }
    }

    @Test
    public void txSigValidateTest() throws IOException,SignatureException {
        // Create Transaction
        TransactionHusk tx = new TransactionHusk(getJson()).sign(wallet);

        ObjectMapper mapper = TestUtils.getMapper();
        String jsonStr = mapper.writeValueAsString(TransactionDto.createBy(tx));

        // Receive Transaction
        TransactionDto resDto = mapper.readValue(jsonStr, TransactionDto.class);

        // Signature Validation
        assertTrue(TransactionDto.of(resDto).verify());
    }

    private JsonObject getJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "0");
        json.addProperty("name", "Rachael");
        json.addProperty("age", "27");
        return json;
    }
}
