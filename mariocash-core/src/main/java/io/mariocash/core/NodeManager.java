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

package dev.zhihexireng.core;

import dev.zhihexireng.config.DefaultConfig;
import dev.zhihexireng.core.event.PeerEventListener;
import dev.zhihexireng.core.exception.NotValidateException;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.Set;

public interface NodeManager extends PeerEventListener {

    void init();

    Transaction addTransaction(Transaction tx) throws IOException,SignatureException;

    List<Transaction> getTransactionList();

    Transaction getTxByHash(String id);

    Block generateBlock() throws IOException, NotValidateException;

    Block addBlock(Block block) throws IOException, NotValidateException;

    Set<Block> getBlocks();

    Block getBlockByIndexOrHash(String indexOrHash);

    String getNodeUri();

    void addPeer(String peer);

    void removePeer(String peer);

    List<String> getPeerUriList();

    DefaultConfig getDefaultConfig();

    Wallet getWallet();
}
