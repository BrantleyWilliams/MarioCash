package dev.zhihexireng.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.common.util.ByteUtil;
import dev.zhihexireng.common.util.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TransactionHeaderTest {

    private static final Logger log = LoggerFactory.getLogger(TransactionHeaderTest.class);

    private final byte[] chain = new byte[20];
    private final byte[] version = new byte[8];
    private final byte[] type = new byte[8];
    private long timestamp;
    private byte[] bodyHash;
    private long bodyLength;

    private TransactionBody txBody;


    @Before
    public void init() {

        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("test1", "01");

        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("test2", "02");

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject1);
        jsonArray.add(jsonObject2);

        timestamp = TimeUtils.time();
        txBody = new TransactionBody(jsonArray);
        bodyHash = txBody.getBodyHash();
        bodyLength = txBody.length();
    }

    @Test
    public void testTransactionHeader() {

        TransactionHeader txHeader1 =
                new TransactionHeader(chain, version, type, timestamp, bodyHash, bodyLength);

        log.debug(txHeader1.toString());
        log.debug(txHeader1.toJsonObject().toString());

        log.debug("chain=" + Hex.toHexString(txHeader1.getChain()));
        log.debug("version=" + Hex.toHexString(txHeader1.getVersion()));
        log.debug("type=" + Hex.toHexString(txHeader1.getType()));
        log.debug("timestamp="
                + Hex.toHexString(ByteUtil.longToBytes(txHeader1.getTimestamp())));
        log.debug("bodyHash="
                + Hex.toHexString(txHeader1.getBodyHash()));
        log.debug("bodyLength="
                + Hex.toHexString(ByteUtil.longToBytes(txHeader1.getBodyLength())));

        TransactionHeader txHeader2
                = new TransactionHeader(chain, version, type, timestamp, txBody);

        log.debug(txHeader2.toString());
        log.debug(txHeader2.toJsonObject().toString());

        log.debug("chain=" + Hex.toHexString(txHeader2.getChain()));
        log.debug("version=" + Hex.toHexString(txHeader2.getVersion()));
        log.debug("type=" + Hex.toHexString(txHeader2.getType()));
        log.debug("timestamp="
                + Hex.toHexString(ByteUtil.longToBytes(txHeader2.getTimestamp())));
        log.debug("bodyHash="
                + Hex.toHexString(txHeader2.getBodyHash()));
        log.debug("bodyLength="
                + Hex.toHexString(ByteUtil.longToBytes(txHeader2.getBodyLength())));

        assertEquals(txHeader1.toJsonObject(), txHeader2.toJsonObject());

        assertArrayEquals(txHeader1.getHashForSigning(), txHeader2.getHashForSigning());

        JsonObject jsonObject3 = txHeader2.toJsonObject();
        jsonObject3.addProperty("timestamp",
                Hex.toHexString(ByteUtil.longToBytes(TimeUtils.time() + 1)));
        log.debug("jsonObject3=" + jsonObject3.toString());

        TransactionHeader txHeader3 = new TransactionHeader(jsonObject3);
        log.debug("txHeader1=" + txHeader1.toJsonObject());
        log.debug("txHeader3=" + txHeader3.toJsonObject());
        assertNotEquals(txHeader1.toJsonObject(), txHeader3.toJsonObject());


        TransactionHeader txHeader4 = new TransactionHeader(txHeader1.toJsonObject());
        log.debug("txHeader4=" + txHeader4.toJsonObject());

        assertEquals(txHeader1.toJsonObject(), txHeader4.toJsonObject());
    }

    @Test
    public void testTransactionHeaderClone() throws Exception {
        TransactionHeader txHeader1
                = new TransactionHeader(chain, version, type, timestamp, bodyHash, bodyLength);

        TransactionHeader txHeader2 = txHeader1.clone();
        log.debug("txHeader1=" + txHeader1.toJsonObject());
        log.debug("txHeader2=" + txHeader2.toJsonObject());
        assertEquals(txHeader1.toJsonObject(), txHeader2.toJsonObject());

        JsonObject jsonObject3 = txHeader1.toJsonObject();
        jsonObject3.addProperty("timestamp",
                Hex.toHexString(ByteUtil.longToBytes(TimeUtils.time() + 1)));
        log.debug("jsonObject3=" + jsonObject3.toString());

        TransactionHeader txHeader3 = new TransactionHeader(jsonObject3);
        log.debug("txHeader1=" + txHeader1.toJsonObject());
        log.debug("txHeader3=" + txHeader3.toJsonObject());
        assertNotEquals(txHeader1.toJsonObject(), txHeader3.toJsonObject());
    }
}
