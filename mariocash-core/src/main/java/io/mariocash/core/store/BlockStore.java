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

package dev.zhihexireng.core.store;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.husk.BlockHusk;
import dev.zhihexireng.core.store.datasource.LevelDbDataSource;

public class BlockStore implements Store<Sha3Hash, BlockHusk> {
    private LevelDbDataSource db;

    public BlockStore() {
        this.db = new LevelDbDataSource("block");
        db.init();
    }

    @Override
    public void put(Sha3Hash key, BlockHusk value) {
        this.db.put(key.getBytes(), value.getData());
    }

    @Override
    public BlockHusk get(Sha3Hash key) throws InvalidProtocolBufferException {
        return new BlockHusk(db.get(key.getBytes()));
    }
}
