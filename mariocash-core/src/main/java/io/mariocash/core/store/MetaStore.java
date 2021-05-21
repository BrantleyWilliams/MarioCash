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

import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.store.datasource.DbSource;

public class MetaStore implements Store<MetaStore.MetaInfo, Sha3Hash> {
    private final DbSource<byte[], byte[]> db;

    MetaStore(DbSource<byte[], byte[]> dbSource) {
        this.db = dbSource.init();
    }

    @Override
    public void put(MetaInfo key, Sha3Hash value) {
        db.put(key.name().getBytes(), value.getBytes());
    }

    @Override
    public Sha3Hash get(MetaInfo key) {
        return Sha3Hash.createByHashed(db.get(key.name().getBytes()));
    }

    @Override
    public boolean contains(MetaInfo key) {
        return db.get(key.name().getBytes()) != null;
    }

    @Override
    public void close() {
        db.close();
    }

    public enum MetaInfo {
        BEST_BLOCK
    }
}
