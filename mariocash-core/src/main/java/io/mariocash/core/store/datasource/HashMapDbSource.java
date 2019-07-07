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

package dev.zhihexireng.core.store.datasource;

import org.apache.commons.codec.binary.Hex;

import java.util.HashMap;

public class HashMapDbSource implements DbSource<byte[], byte[]> {
    HashMap<String, byte[]> db;

    @Override
    public void init() {
        db = new HashMap<>();
    }

    @Override
    public byte[] get(byte[] key) {
        return db.get(Hex.encodeHexString(key));
    }

    @Override
    public void put(byte[] key, byte[] value) {
        db.put(Hex.encodeHexString(key), value);
    }

    @Override
    public long count() {
        return db.size();
    }
}
