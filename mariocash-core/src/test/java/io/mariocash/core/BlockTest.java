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
import dev.zhihexireng.core.mapper.BlockMapper;
import dev.zhihexireng.proto.BlockChainProto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Arrays;

@RunWith(SpringRunner.class)
public class BlockTest {

    private Block block;

    @Before
    public void setUp() throws Exception {
        Account author = new Account();
        JsonObject json = new JsonObject();
        json.addProperty("data", "TEST");
        Transaction tx = new Transaction(author, json);
        BlockBody sampleBody = new BlockBody(Arrays.asList(new Transaction[] {tx}));

        BlockHeader genesisBlockHeader = new BlockHeader.Builder()
                .blockBody(sampleBody)
                .prevBlock(null)
                .build(author);

        BlockHeader blockHeader = new BlockHeader.Builder()
                .blockBody(sampleBody)
                .prevBlock(new Block(genesisBlockHeader, sampleBody)) // genesis block
                .build(author);
        this.block = new Block(blockHeader, sampleBody);
    }

    @Test
    public void blockTest() throws IOException {
        assert !block.getBlockHash().isEmpty();
        assert block.getIndex() == 1;
    }

    @Test
    public void deserializeBlockFromSerializerTest() throws IOException {
        byte[] bytes = SerializationUtils.serialize(block);
        ByteString byteString = ByteString.copyFrom(bytes);
        byte[] byteStringBytes = byteString.toByteArray();
        assert bytes.length == byteStringBytes.length;
        Block deserializeBlock = (Block) SerializationUtils.deserialize(byteStringBytes);
        assert block.getBlockHash().equals(deserializeBlock.getBlockHash());
    }

    @Test
    public void deserializeTransactionFromProtoTest() throws IOException {
        BlockChainProto.Block protoBlock = BlockMapper.blockToProtoBlock(block);
        Block deserializeBlock = BlockMapper.protoBlockToBlock(protoBlock);
        assert block.getBlockHash().equals(deserializeBlock.getBlockHash());
    }

}
