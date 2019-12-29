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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import dev.zhihexireng.proto.Proto;

import java.io.File;
import java.io.IOException;

class BlockChainLoader {
    private final File infoFile;

    public BlockChainLoader(File infoFile) {
        this.infoFile = infoFile;
    }

    public BlockHusk getGenesis() throws IOException {
        return convertJsonToBlock(this.infoFile);
    }

    public BranchInfo loadBranchInfo(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, BranchInfo.class);
    }

    private BlockHusk convertJsonToBlock(File file) throws IOException {
        BranchInfo branchInfo = loadBranchInfo(file);
        //TODO 브랜치 정보 파일 컨버팅
        return new BlockHusk(Proto.Block.newBuilder()
                .setHeader(Proto.Block.Header.newBuilder()
                        .setRawData(Proto.Block.Header.Raw.newBuilder()
                                .setType(ByteString.copyFrom(branchInfo.type.getBytes()))
                                .build()
                        )
                        .setSignature(ByteString.copyFromUtf8(branchInfo.signature))
                        .build()
                )
                .addBody(Proto.Transaction.getDefaultInstance())
                .build());
    }
}
