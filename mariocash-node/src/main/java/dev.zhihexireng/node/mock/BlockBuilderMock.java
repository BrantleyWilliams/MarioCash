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

package dev.zhihexireng.node.mock;

import dev.zhihexireng.core.Account;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockBody;
import dev.zhihexireng.core.BlockHeader;
import dev.zhihexireng.node.BlockBuilder;

import java.io.IOException;
import java.util.Arrays;

public class BlockBuilderMock implements BlockBuilder {
    @Override
    public Block build(String data) throws IOException {
        Account account = new Account();
        BlockBody blockBody = new BlockBody(Arrays.asList());
        BlockHeader blockHeader = new BlockHeader.Builder()
                .prevBlock(null)
                .blockBody(blockBody).build(account);
        return new Block(blockHeader, blockBody);
    }
}
