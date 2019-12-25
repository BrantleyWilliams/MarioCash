/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng;

import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.crypto.HashUtil;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.util.TimeUtils;
import dev.zhihexireng.util.FileUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.nio.file.Paths;
import java.util.Random;

public class TestUtils {
    private static Wallet wallet;
    public static final String YGG_HOME = "testOutput";
    private TestUtils() {}

    static {
        try {
            wallet = new Wallet();
        } catch (Exception e) {
            throw new NotValidateException(e);
        }
    }

    public static void clearOutput() {
        FileUtil.recursiveDelete(Paths.get(YGG_HOME));
    }

    public static Proto.Transaction getTransactionFixture() {
        String body = getTransfer().toString();
        return Proto.Transaction.newBuilder()
                .setHeader(Proto.Transaction.Header.newBuilder()
                        .setRawData(Proto.Transaction.Header.Raw.newBuilder()
                                .setType(ByteString.copyFrom(
                                        ByteBuffer.allocate(4).putInt(1).array()))
                                .setVersion(ByteString.copyFrom(
                                        ByteBuffer.allocate(4).putInt(1).array()))
                                .setDataHash(ByteString.copyFrom(
                                        HashUtil.sha3(body.getBytes())))
                                .setDataSize(body.getBytes().length)
                                .setTimestamp(TimeUtils.time())
                        )
                )
                .setBody(body)
                .build();
    }

    public static Proto.Block getBlockFixture() {
        return Proto.Block.newBuilder()
                .setHeader(
                        Proto.Block.Header.newBuilder()
                                .setRawData(Proto.Block.Header.Raw.newBuilder()
                                        .setType(ByteString.copyFrom(
                                                ByteBuffer.allocate(4).putInt(1).array()))
                                        .setVersion(ByteString.copyFrom(
                                                ByteBuffer.allocate(4).putInt(1).array()))
                                        .build()
                                ).build()
                )
                .addBody(getTransactionFixture())
                .addBody(getTransactionFixture())
                .addBody(getTransactionFixture())
                .build();
    }

    public static TransactionHusk createTxHusk() {
        return createTxHusk(wallet);
    }

    public static TransactionHusk createTxHusk(Wallet wallet) {
        return new TransactionHusk(getTransfer()).sign(wallet);
    }

    public static BlockHusk createGenesisBlockHusk() {
        return createGenesisBlockHusk(wallet);
    }

    public static BlockHusk createGenesisBlockHusk(Wallet wallet) {
        return BlockHusk.genesis(wallet, getTransfer());
    }

    public static BlockHusk createBlockHuskByTxList(Wallet wallet, List<TransactionHusk> txList) {
        return BlockHusk.build(wallet, txList, createGenesisBlockHusk());
    }

    public static byte[] randomBytes(int length) {
        byte[] result = new byte[length];
        new Random().nextBytes(result);
        return result;
    }

    private static JsonObject getTransfer() {
        JsonObject txObj = new JsonObject();

        txObj.addProperty("operator", "transfer");
        txObj.addProperty("to", "0x9843DC167956A0e5e01b3239a0CE2725c0631392");
        txObj.addProperty("amount", 100);

        return txObj;
    }

    public static Proto.Transaction[] getTransactionFixtures() {
        return new Proto.Transaction[] {getTransactionFixture(), getTransactionFixture()};
    }

    public static Proto.Block[] getBlockFixtures() {
        return new Proto.Block[] {getBlockFixture(), getBlockFixture(), getBlockFixture()};
    }
}
