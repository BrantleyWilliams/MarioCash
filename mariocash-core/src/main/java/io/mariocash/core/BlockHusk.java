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

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.exception.InvalidSignatureException;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.trie.Trie;
import dev.zhihexireng.util.ByteUtil;
import dev.zhihexireng.util.TimeUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SignatureException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public BlockHusk(Proto.Block.Header blockHeader, Wallet wallet, List<TransactionHusk> body) {

        try {
            byte[] hashDataForSign = BlockHeader.toBlockHeader(blockHeader).getHashForSignning();

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

    public BlockHusk(Wallet wallet, List<TransactionHusk> body, BlockHusk prevBlock) {

        byte[] merkleRoot = Trie.getMerkleRootHusk(body);
        if (merkleRoot == null) {
            merkleRoot = EMPTY_BYTE;
        }

        long length = 0;

        for (TransactionHusk txHusk: body) {
            length += txHusk.getCoreTransaction().getBody().length();
        }

        Proto.Block.Header blockHeader  = getHeader(
                prevBlock.getHeader().getChain().toByteArray(),
                new byte[8],
                new byte[8],
                prevBlock.getHash().getBytes(),
                prevBlock.getIndex() + 1,
                TimeUtils.time(),
                merkleRoot,
                length);

        try {
            byte[] hashDataForSign = BlockHeader.toBlockHeader(blockHeader).getHashForSignning();

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
        return new Address(ecKey().getAddress());
    }

    public Sha3Hash getPrevHash() {
        return Sha3Hash.createByHashed(getHeader().getPrevBlockHash().toByteArray());
    }

    public long getIndex() {
        return ByteUtil.byteArrayToLong(this.protoBlock.getHeader().getIndex().toByteArray());
    }

    public long nextIndex() {
        return getIndex() + 1;
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

    /**
     * Get ECKey(include pubKey) using sig & signData.
     *
     * @return ECKey(include pubKey)
     */
    private ECKey ecKey() {
        try {
            byte[] hashedRawData = new Sha3Hash(getHeader().toByteArray()).getBytes();
            byte[] signatureBin = this.protoBlock.getSignature().toByteArray();
            return ECKey.signatureToKey(hashedRawData, signatureBin);
        } catch (SignatureException e) {
            throw new InvalidSignatureException(e);
        }
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

    @VisibleForTesting
    public static BlockHusk genesis(Wallet wallet, JsonObject jsonObject) {
        try {
            JsonArray jsonArrayTxBody = new JsonArray();
            jsonArrayTxBody.add(jsonObject);

            TransactionBody txBody = new TransactionBody(jsonArrayTxBody);
            TransactionHeader txHeader = new TransactionHeader(
                    new byte[20],
                    new byte[8],
                    new byte[8],
                    TimeUtils.time(),
                    txBody);

            Transaction tx = new Transaction(txHeader, wallet, txBody);
            List<Transaction> txList = new ArrayList<>();
            txList.add(tx);

            BlockBody blockBody = new BlockBody(txList);
            BlockHeader blockHeader = new BlockHeader(
                    new byte[20],
                    new byte[8],
                    new byte[8],
                    new byte[32],
                    0L,
                    TimeUtils.time(),
                    blockBody.getMerkleRoot(),
                    blockBody.length());

            Block coreBlock = new Block(blockHeader, wallet, blockBody);

            return new BlockHusk(Block.toProtoBlock(coreBlock));
        } catch (Exception e) {
            throw new NotValidateException();
        }
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

    private static long getBodySize(List<TransactionHusk> body) {
        long size = 0;
        if (body == null || body.isEmpty()) {
            return size;
        }
        for (TransactionHusk tx : body) {
            if (tx.getInstance() != null) {
                size += tx.getInstance().toByteArray().length;
            }
        }
        return size;
    }

    @Override
    public int compareTo(BlockHusk o) {
        return Long.compare(getIndex(), o.getIndex());
    }
}
