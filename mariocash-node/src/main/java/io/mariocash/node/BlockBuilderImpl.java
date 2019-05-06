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

package dev.zhihexireng.node;

import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockBody;
import dev.zhihexireng.core.BlockBuilder;
import dev.zhihexireng.core.BlockHeader;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.Wallet;

import java.util.List;

public class BlockBuilderImpl implements BlockBuilder {

    @Override
    public Block build(Wallet wallet, List<Transaction> txList, Block prevBlock) {

        BlockBody blockBody = new BlockBody(txList);
        BlockHeader blockHeader = new BlockHeader.Builder()
                .prevBlock(prevBlock)
                .blockBody(blockBody).build(wallet);
        return new Block(blockHeader, blockBody);
    }

}
