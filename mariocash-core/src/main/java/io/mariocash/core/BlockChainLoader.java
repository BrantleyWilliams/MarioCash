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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import static dev.zhihexireng.core.BranchInfo.BranchData;

public class BlockChainLoader {
    private ObjectMapper mapper = new ObjectMapper();
    private InputStream branchInfoStream;

    public BlockChainLoader(InputStream branchInfoStream) {
        this.branchInfoStream = branchInfoStream;
    }

    public BlockChainLoader(File infoFile) {
        try {
            this.branchInfoStream = new FileInputStream(infoFile);
        } catch (FileNotFoundException e) {
            throw new FailedOperationException(e);
        }
    }

    public BlockHusk getGenesis() {
        return convertJsonToBlock();
    }

    public BranchInfo loadBranchInfo() throws IOException {
        return mapper.readValue(branchInfoStream, BranchInfo.class);
    }

    private BlockHusk convertJsonToBlock() {
        //TODO 브랜치 정보 파일 컨버팅
        try {
            BranchInfo branchInfo = loadBranchInfo();

            return convertBlock(branchInfo);
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    private BlockHusk convertBlock(BranchInfo branchInfo) throws JsonProcessingException {
        return new BlockHusk(Proto.Block.newBuilder()
                .setHeader(Proto.Block.Header.newBuilder()
                        .setChain(ByteString.copyFrom(Hex.decode(branchInfo.chain)))
                        .setVersion(ByteString.copyFrom(Hex.decode(branchInfo.version)))
                        .setType(ByteString.copyFrom(Hex.decode(branchInfo.type)))
                        .setPrevBlockHash(ByteString.copyFrom(Hex.decode(branchInfo.prevBlockHash)))
                        .setIndex(ByteString.copyFrom(ByteUtil.longToBytes(Long.parseLong(branchInfo.index))))
                        .setTimestamp(ByteString.copyFrom(ByteUtil.longToBytes(Long.parseLong(branchInfo.timestamp))))
                        .setMerkleRoot(ByteString.copyFrom(branchInfo.merkleRoot.getBytes()))
                        .setBodyLength(ByteString.copyFrom(ByteUtil.longToBytes(Long.parseLong(branchInfo.bodyLength))))
                        .build())
                .setSignature(ByteString.copyFrom(Hex.decode(branchInfo.signature)))
                .setBody(convertTransaction(branchInfo.body))
                .build());
    }

    private Proto.TransactionList convertTransaction(List<BranchData> branchDataList) throws
            JsonProcessingException {

        Proto.TransactionList txList = Proto.TransactionList.newBuilder().build();
        List<Proto.Transaction> list = txList.getTransactionsList();

        for (BranchData branchData : branchDataList) {
            list.add(Proto.Transaction.newBuilder()
                    .setHeader(Proto.Transaction.Header.newBuilder()
                        .setType(ByteString.copyFrom(Hex.decode(branchData.type)))
                        .setVersion(ByteString.copyFrom(Hex.decode(branchData.version)))
                        .setTimestamp(ByteString.copyFrom(ByteUtil.longToBytes(Long.parseLong(branchData.timestamp))))
                        .setBodyHash(ByteString.copyFrom(Hex.decode(branchData.bodyHash)))
                        .setBodyLength(ByteString.copyFrom(ByteUtil.longToBytes(Long.parseLong(branchData.bodyLength))))
                        .build())
                    .setSignature(ByteString.copyFrom(Hex.decode(branchData.signature)))
                    .setBody(ByteString.copyFrom(mapper.writeValueAsString(branchData.body).getBytes())).build()
            );
        }
        return txList;
    }
}
