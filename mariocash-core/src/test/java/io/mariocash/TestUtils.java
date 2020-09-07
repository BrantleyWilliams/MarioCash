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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockBody;
import dev.zhihexireng.core.BlockHeader;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.BlockHuskBuilder;
import dev.zhihexireng.core.BlockSignature;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.TransactionBody;
import dev.zhihexireng.core.TransactionHeader;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionSignature;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.exception.InvalidSignatureException;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.util.TimeUtils;

import java.nio.ByteBuffer;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestUtils {
    public static final String YGG_HOME = "testOutput";

    private static Wallet wallet;
    private static byte[] type =
            ByteBuffer.allocate(4).putInt(BlockHuskBuilder.DEFAULT_TYPE).array();
    private static byte[] version =
            ByteBuffer.allocate(4).putInt(BlockHuskBuilder.DEFAULT_VERSION).array();

    private TestUtils() {}

    static {
        try {
            wallet = new Wallet();
        } catch (Exception e) {
            throw new InvalidSignatureException(e);
        }
    }

    public static Proto.Transaction getTransactionFixture() {

        try {
            return new Transaction(sampleTxObject(null)).toProtoTransaction();
        } catch (SignatureException e) {
            throw new NotValidateException();
        }
    }

    public static Proto.Transaction[] getTransactionFixtures() {
        return new Proto.Transaction[] {getTransactionFixture(), getTransactionFixture()};
    }

    public static Proto.Block getBlockFixture() {
        return getBlockFixture(999L);
    }

    public static Proto.Block getBlockFixture(Long index) {
        return getBlockFixture(index,
                new Sha3Hash("9358888ca1ccd444ad11fb0ea1b5d03483f87664183c6e91ddab1b577cce2c06"));
    }

    public static Proto.Block getBlockFixture(Long index, Sha3Hash prevHash) {

        try {
            Block tmpBlock = sampleBlock();
            BlockHeader tmpBlockHeader = tmpBlock.getHeader();
            BlockBody tmpBlockBody = tmpBlock.getBody();

            BlockHeader newBlockHeader = new BlockHeader(
                    tmpBlockHeader.getChain(),
                    tmpBlockHeader.getVersion(),
                    tmpBlockHeader.getType(),
                    prevHash.getBytes(),
                    index,
                    TimeUtils.time(),
                    tmpBlockBody);

            return new Block(newBlockHeader, wallet, tmpBlockBody).toProtoBlock();
        } catch (Exception e) {
            throw new NotValidateException();
        }


    }

    public static TransactionHusk createTxHusk() {
        return createTxHusk(wallet);
    }

    public static TransactionHusk createTxHusk(Wallet wallet) {
        return new TransactionHusk(sampleTx(wallet));
    }

    public static BlockHusk createGenesisBlockHusk() {
        return createGenesisBlockHusk(wallet);
    }

    public static BlockHusk createGenesisBlockHusk(Wallet wallet) {
        return BlockHusk.genesis(wallet, sampleTxObject(null));
    }

    public static BlockHusk createBlockHuskByTxList(Wallet wallet, List<TransactionHusk> txList) {
        return new BlockHusk(wallet, txList, createGenesisBlockHusk());
    }

    public static byte[] randomBytes(int length) {
        byte[] result = new byte[length];
        new Random().nextBytes(result);
        return result;
    }

    public static JsonObject sampleTxObject(Wallet newWallet) {

        JsonArray params = new JsonArray();
        JsonObject param1 = new JsonObject();
        param1.addProperty("address", "0xe1980adeafbb9ac6c9be60955484ab1547ab0b76");
        JsonObject param2 = new JsonObject();
        param2.addProperty("amount", 100);
        params.add(param1);
        params.add(param2);

        JsonObject txObj = new JsonObject();
        txObj.addProperty("method", "transfer");
        txObj.add("params", params);

        return sampleTxObject(newWallet, txObj);

    }

    public static JsonObject sampleTxObject(Wallet newWallet, JsonObject body) {

        Wallet nodeWallet;
        TransactionSignature txSig;
        Transaction tx;

        if (newWallet == null) {
            nodeWallet = wallet;
        } else {
            nodeWallet = newWallet;
        }

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(body);

        TransactionBody txBody;
        txBody = new TransactionBody(jsonArray);

        byte[] chain = new byte[20];
        byte[] version = new byte[8];
        byte[] type = new byte[8];
        long timestamp = TimeUtils.time();

        TransactionHeader txHeader;
        txHeader = new TransactionHeader(chain, version, type, timestamp, txBody);

        try {
            txSig = new TransactionSignature(nodeWallet, txHeader.getHashForSignning());
            tx = new Transaction(txHeader, txSig, txBody);

            return tx.toJsonObject();

        } catch (Exception e) {
            return null;
        }

    }

    public static Transaction sampleTx() {
        try {
            return new Transaction(sampleTxObject(null));
        } catch (SignatureException e) {
            return null;
        }
    }

    public static Transaction sampleTx(JsonObject body) {
        try {
            return new Transaction(sampleTxObject(null));
        } catch (SignatureException e) {
            return null;
        }
    }

    public static Transaction sampleTx(Wallet wallet) {
        try {
            return new Transaction(sampleTxObject(wallet));
        } catch (SignatureException e) {
            return null;
        }
    }

    public static JsonObject sampleBlockObject() {

        List<Transaction> txs1 = new ArrayList<>();
        txs1.add(sampleTx());

        BlockBody blockBody = new BlockBody(txs1);

        long index = 0;
        long timestamp = TimeUtils.time();
        BlockHeader blockHeader = null;
        try {
            blockHeader = new BlockHeader(
                    new byte[20], new byte[8], new byte[8], new byte[32], index, timestamp,
                    blockBody.getMerkleRoot(), blockBody.length());

            BlockSignature blockSig = new BlockSignature(wallet, blockHeader.getHashForSignning());

            Block block = new Block(blockHeader, blockSig, blockBody);

            return block.toJsonObject();
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public static Block sampleBlock() {
        try {
            return new Block(sampleBlockObject());
        } catch (SignatureException e) {
            throw new NotValidateException();
        }
    }

    public static Proto.Transaction sampleProtoTx() {
        return sampleTx().toProtoTransaction();
    }

    public static Proto.Block[] getBlockFixtures() {
        return new Proto.Block[] {getBlockFixture(), getBlockFixture(), getBlockFixture()};
    }
}
