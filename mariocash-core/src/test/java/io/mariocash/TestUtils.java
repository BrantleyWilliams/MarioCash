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

import dev.zhihexireng.core.Address;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.TransactionHeader;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.proto.BlockChainProto;

import java.util.Random;

public class TestUtils {
    public static byte[] randomBytes(int length) {
        byte[] result = new byte[length];
        new Random().nextBytes(result);
        return result;
    }

    public static Transaction createDummyTx() {
        TransactionHeader transactionHeader = new TransactionHeader(
                TestUtils.randomBytes(4),
                TestUtils.randomBytes(4),
                TestUtils.randomBytes(32),
                8L,
                8L,
                TestUtils.randomBytes(65));
        return new Transaction(transactionHeader, "dummy");
    }

    public static Address getTestAddress() {
        return new Address(new ECKey().getAddress());
    }

    public static BlockChainProto.Block getBlockFixture() {
        BlockChainProto.BlockHeader defaultHeader =
                BlockChainProto.BlockHeader.getDefaultInstance();
        BlockChainProto.BlockBody defaultBody = BlockChainProto.BlockBody.getDefaultInstance();
        return BlockChainProto.Block.newBuilder()
                .setHeader(defaultHeader)
                .setData(defaultBody)
                .build();
    }
}
