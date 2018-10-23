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

import dev.zhihexireng.core.Transaction;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface TransactionPool {
    Transaction get(byte[] key);

    Transaction put(byte[] key, Transaction tx) throws IOException;

    Map<byte[], Transaction> getAll(Set<byte[]> keys);

    void remove(Set<byte[]> keys);

    void clear();
}
