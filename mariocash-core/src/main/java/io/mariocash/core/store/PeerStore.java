/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.core.store;

import dev.zhihexireng.core.exception.NonExistObjectException;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerId;
import dev.zhihexireng.core.store.datasource.DbSource;

public class PeerStore implements Store<PeerId, Peer> {
    private final DbSource<byte[], byte[]> db;

    PeerStore(DbSource<byte[], byte[]> dbSource) {
        this.db = dbSource.init();
    }

    @Override
    public void put(PeerId key, Peer value) {
        db.put(key.getBytes(), value.toString().getBytes());
    }

    @Override
    public Peer get(PeerId key) {
        byte[] foundValue = db.get(key.getBytes());
        if (foundValue != null) {
            return Peer.valueOf(foundValue);
        }

        throw new NonExistObjectException("Not Found [" + key + "]");
    }

    @Override
    public boolean contains(PeerId key) {
        return db.get(key.getBytes()) != null;
    }

    public void close() {
        this.db.close();
    }
}
