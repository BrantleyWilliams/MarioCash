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

package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.trie.Trie;
import dev.zhihexireng.util.ByteUtil;
import dev.zhihexireng.util.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BlockHusk implements ProtoHusk<Proto.Block>, Comparable<BlockHusk> {
    private static final byte[] EMPTY_BYTE = new byte[32];

    private Proto.Block protoBlock;
    private Block coreBlock;

    public BlockHusk(byte[] bytes) {
        try {
            this.protoBlock = Proto.Block.parseFrom(bytes);
            this.coreBlock = Block.toBlock(this.protoBlock);
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public BlockHusk(Proto.Block block) {
        this.protoBlock = block;
        try {
            this.coreBlock = Block.toBlock(this.protoBlock);
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public BlockHusk(Wallet wallet, List<TransactionHusk> body, BlockHusk prevBlock) {

        if (wallet == null || body == null || prevBlock == null) {
            throw new NotValidateException();
        }

        byte[] merkleRoot = Trie.getMerkleRootHusk(body);
        if (merkleRoot == null) {
            merkleRoot = EMPTY_BYTE;
        }

        long length = 0;

        for (TransactionHusk txHusk: body) {
            length += txHusk.getCoreTransaction().getBody().length();
        }

        Proto.Block.Header blockHeader = getHeader(
                prevBlock.getHeader().getChain().toByteArray(),
                new byte[8],
                new byte[8],
                prevBlock.getHash().getBytes(),
                prevBlock.getIndex() + 1,
                TimeUtils.time(),
                merkleRoot,
                length);

        try {
            byte[] hashDataForSign = BlockHeader.toBlockHeader(blockHeader).getHashForSigning();

            Proto.TransactionList.Builder builder = Proto.TransactionList.newBuilder();
            for (TransactionHusk tx : body) {
                builder.addTransactions(tx.getProtoTransaction());
            }

            Proto.Block protoBlock = Proto.Block.newBuilder()
                    .setHeader(blockHeader)
                    .setSignature(ByteString.copyFrom(wallet.signHashedData(hashDataForSign)))
                    .setBody(builder.build())
                    .build();

            this.protoBlock = protoBlock;
            this.coreBlock = Block.toBlock(this.protoBlock);

        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public Sha3Hash getHash() {

        return new Sha3Hash(protoBlock.getHeader().toByteArray());
    }

    public Address getAddress() {
        try {
            return new Address(this.coreBlock.getAddress());
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public BranchId getBranchId() {
        byte[] chain = protoBlock.getHeader().getChain().toByteArray();
        return new BranchId(Sha3Hash.createByHashed(chain));
    }

    public Sha3Hash getPrevHash() {

        return Sha3Hash.createByHashed(getHeader().getPrevBlockHash().toByteArray());
    }

    public long getIndex() {

        return ByteUtil.byteArrayToLong(this.protoBlock.getHeader().getIndex().toByteArray());
    }

    public List<TransactionHusk> getBody() {
        List<TransactionHusk> result = new ArrayList<>();
        for (Proto.Transaction tx : protoBlock.getBody().getTransactionsList()) {
            result.add(new TransactionHusk(tx));
        }
        return result;
    }

    @Override
    public byte[] getData() {
        return protoBlock.toByteArray();
    }

    @Override
    public Proto.Block getInstance() {
        return this.protoBlock;
    }

    public boolean verify() {
        return this.coreBlock.verify();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockHusk other = (BlockHusk) o;
        return Arrays.equals(getHash().getBytes(), other.getHash().getBytes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(protoBlock);
    }

    /**
     * Convert from BlockHusk.class to JSON string.
     * @return block as JsonObject
     */
    public JsonObject toJsonObject() {
        return this.coreBlock.toJsonObject();
    }

    private Proto.Block.Header getHeader() {
        return this.protoBlock.getHeader();
    }

    private static Proto.Block.Header getHeader(
            byte[] chain,
            byte[] version,
            byte[] type,
            byte[] prevBlockHash,
            long index,
            long timestamp,
            byte[] merkleRoot,
            long bodyLength) {

        return Proto.Block.Header.newBuilder()
                .setChain(ByteString.copyFrom(chain))
                .setVersion(ByteString.copyFrom(version))
                .setType(ByteString.copyFrom(type))
                .setPrevBlockHash(ByteString.copyFrom(prevBlockHash))
                .setIndex(ByteString.copyFrom(ByteUtil.longToBytes(index)))
                .setTimestamp(ByteString.copyFrom(ByteUtil.longToBytes(timestamp)))
                .setMerkleRoot(ByteString.copyFrom(merkleRoot))
                .setBodyLength(ByteString.copyFrom(ByteUtil.longToBytes(bodyLength)))
                .build();
    }

    @Override
    public int compareTo(BlockHusk o) {
        return Long.compare(getIndex(), o.getIndex());
    }
}
