package dev.zhihexireng.node.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.zhihexireng.common.crypto.HashUtil;
import dev.zhihexireng.common.util.ByteUtil;
import dev.zhihexireng.core.account.Wallet;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

public class AdminApiImplTest {

    private static long COMMAND_ACTIVE_TIME = 3 * 60 * 1000;

    private static final Logger log = LoggerFactory.getLogger(AdminApiImplTest.class);

    @Test
    public void testJsonMessage() throws IOException {

        String jsonMsg = "{"
                + "\"header\": \"{\\\"timestamp\\\":\\\"00000166818E7D38\\\",\\\"nonce\\\":"
                + "\\\"0000000000000000aabb165899f98a8\\\",\\\"bodyHash\\\":"
                + "\\\"3717ec34f5b0345c3b480d9cd402f0be1111c0e04cb9dbe1da5b933e353a5bba\\\","
                + "\\\"bodyLength\\\":\\\"0000000000000018\\\"}\","
                + "\"signature\": \"1bc1822935fc15c172305d59f134f3f27a305ca97be9926e3cd5e8d4bf5780"
                + "a8332ef34bae62ddb1fe00903b4bf4bfe8c6d5e898cc4f291a3ccf8307d4cc6aec46\","
                + "\"body\": \"[{\\\"method\\\":\\\"nodeHello\\\"}]\""
                + "}";

        log.info(jsonMsg);

        AdminDto command = new ObjectMapper().readValue(jsonMsg, AdminDto.class);

        JsonObject header = new JsonParser().parse(command.getHeader()).getAsJsonObject();
        log.debug(header.toString());

        JsonArray body = new JsonParser().parse(command.getBody()).getAsJsonArray();
        log.debug(body.toString());

        String method = body.get(0).getAsJsonObject().get("method").getAsString();

        // body length check
        long bodyLength = ByteUtil.byteArrayToLong(
                Hex.decode(header.get("bodyLength").getAsString()));

        assert command.getBody().length() == bodyLength;

        // body message check
        assert body.get(0).getAsJsonObject().get("method").getAsString().equals("nodeHello");

        // timestamp check (3 min)
        long timestamp = ByteUtil.byteArrayToLong(
                Hex.decode(header.get("timestamp").getAsString()));
        if (timestamp < System.currentTimeMillis() - (COMMAND_ACTIVE_TIME)) {
            log.error("Timestamp is not valid.");
            //assert false;
        }

        // check bodyHash
        if (!header.get("bodyHash").getAsString().equals(
                Hex.toHexString(HashUtil.sha3(body.toString().getBytes())))) {
            log.error("BodyHash is not valid.");
            assert false;
        }

        // verify signature
        String signature = command.getSignature();
        byte[] dataToSign = header.toString().getBytes();
        if (!Wallet.verify(dataToSign, Hex.decode(signature), false)) {
            log.error("Signature is not valid.");
            //assert false;
        }

    }


}
