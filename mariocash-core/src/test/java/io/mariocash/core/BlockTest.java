/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import dev.zhihexireng.config.DefaultConfig;
import dev.zhihexireng.core.mapper.BlockMapper;
import dev.zhihexireng.proto.BlockChainProto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.SerializationUtils;

import java.util.Collections;

public class BlockTest {

    private Block block;

    @Before
    public void setUp() throws Exception {
        Wallet wallet = new Wallet(new DefaultConfig());

        JsonObject json = new JsonObject();
        json.addProperty("data", "TEST");
        Transaction tx = new Transaction(json);
        WalletMock.sign(tx);
        BlockBody sampleBody = new BlockBody(Collections.singletonList(tx));

        BlockHeader genesisBlockHeader = new BlockHeader.Builder()
                .blockBody(sampleBody)
                .prevBlock(null)
                .build(wallet);

        BlockHeader blockHeader = new BlockHeader.Builder()
                .blockBody(sampleBody)
                .prevBlock(new Block(genesisBlockHeader, sampleBody)) // genesis block
                .build(wallet);
        this.block = new Block(blockHeader, sampleBody);
    }

    @Test
    public void blockTest() {
        assert !block.getBlockHash().isEmpty();
        assert block.getIndex() == 1;
    }

    @Test
    public void deserializeBlockFromSerializerTest() {
        byte[] bytes = SerializationUtils.serialize(block);
        assert bytes != null;
        ByteString byteString = ByteString.copyFrom(bytes);
        byte[] byteStringBytes = byteString.toByteArray();
        assert bytes.length == byteStringBytes.length;
        Block deserializeBlock = (Block) SerializationUtils.deserialize(byteStringBytes);
        assert deserializeBlock != null;
        assert block.getBlockHash().equals(deserializeBlock.getBlockHash());
    }

    @Test
    public void deserializeTransactionFromProtoTest() {
        BlockChainProto.Block protoBlock = BlockMapper.blockToProtoBlock(block);
        Block deserializeBlock = BlockMapper.protoBlockToBlock(protoBlock);
        assert block.getBlockHash().equals(deserializeBlock.getBlockHash());
    }

}
