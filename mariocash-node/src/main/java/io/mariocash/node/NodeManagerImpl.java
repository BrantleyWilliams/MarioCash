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

import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.contract.CoinContract;
import dev.zhihexireng.contract.StateStore;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockBuilder;
import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.Runtime;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.store.TransactionStore;
import dev.zhihexireng.core.TransactionValidator;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.net.GrpcClientChannel;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerClientChannel;
import dev.zhihexireng.core.net.PeerGroup;
import dev.zhihexireng.node.config.NodeProperties;
import dev.zhihexireng.node.exception.FailedOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class NodeManagerImpl implements NodeManager {
    private static final Logger log = LoggerFactory.getLogger(NodeManager.class);

    private BlockBuilder blockBuilder;

    private BlockChain blockChain;

    private TransactionStore transactionStore;

    private TransactionValidator txValidator;

    private NodeProperties nodeProperties;

    private Wallet wallet;

    private PeerGroup peerGroup;

    private Peer peer;

    private MessageSender<PeerClientChannel> messageSender;

    private StateStore stateStore;

    private NodeHealthIndicator nodeHealthIndicator;

    @Autowired
    public void setNodeProperties(NodeProperties nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    @Autowired
    public void setBlockBuilder(BlockBuilder blockBuilder) {
        this.blockBuilder = blockBuilder;
    }

    @Autowired
    public void setBlockChain(BlockChain blockChain) {
        this.blockChain = blockChain;
    }

    @Autowired
    public void setTransactionStore(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
    }

    @Autowired
    public void setTxValidator(TransactionValidator txValidator) {
        this.txValidator = txValidator;
    }

    @Autowired
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    @Autowired
    public void setPeerGroup(PeerGroup peerGroup) {
        this.peerGroup = peerGroup;
    }

    @Autowired
    public void setMessageSender(MessageSender<PeerClientChannel> messageSender) {
        this.messageSender = messageSender;
    }

    @Autowired
    public void setNodeHealthIndicator(NodeHealthIndicator nodeHealthIndicator) {
        this.nodeHealthIndicator = nodeHealthIndicator;
    }

    @PreDestroy
    public void destroy() {
        log.info("destroy uri=" + peer.getYnodeUri());
        messageSender.destroy(peer.getYnodeUri());
    }

    @Override
    public void init() {
        this.stateStore = new StateStore();
        log.debug("\n\n getStateStore : " + getStateStore());
        NodeProperties.Grpc grpc = nodeProperties.getGrpc();
        try {
            List<Transaction> txList = transactionStore.getAllTxs();
            executeAllTx(txList);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        messageSender.setListener(this);
        peer = Peer.valueOf(wallet.getNodeId(), grpc.getHost(), grpc.getPort());
        requestPeerList();
        activatePeers();
        if (!peerGroup.isEmpty()) {
            nodeHealthIndicator.sync();
            syncBlockAndTransaction();
        }
        peerGroup.addPeer(peer);
        log.info("Init node=" + peer.getYnodeUri());
        nodeHealthIndicator.up();
    }

    private void executeAllTx(List<Transaction> txList) throws Exception {
        CoinContract coinContract = new CoinContract(stateStore);
        Runtime runtime = new Runtime();
        for (Transaction tx : txList) {
            runtime.execute(coinContract, tx);
        }
    }

    @Override
    public Integer getBalanceOf(String address) {
        return stateStore.getState().get(address);
    }

    @Override
    public StateStore getStateStore() {
        return this.stateStore;
    }

    @Override
    public Transaction getTxByHash(String id) {
        return transactionStore.get(id);
    }

    @Override
    public Transaction addTransaction(Transaction tx) {

        if (txValidator.txSigValidate(tx)) {
            Transaction newTx = transactionStore.put(tx);
            messageSender.newTransaction(tx);
            return newTx;
        }
        throw new FailedOperationException("Transaction");
    }

    @Override
    public List<Transaction> getTransactionList() {
        return new ArrayList<>(transactionStore.getUnconfirmedTxs());
    }

    @Override
    public Set<Block> getBlocks() {
        return new TreeSet<>(blockChain.getBlocks().values());
    }

    @Override
    public Block generateBlock() {
        Block block =
                blockBuilder.build(
                        this.wallet,
                        new ArrayList<>(transactionStore.getUnconfirmedTxs()),
                        blockChain.getPrevBlock()
                );

        blockChain.addBlock(block);
        messageSender.newBlock(block);
        removeTxByBlock(block);
        return block;
    }

    @Override
    public Block addBlock(Block block) {
        Block newBlock = null;
        if (blockChain.isGenesisBlockChain() && block.getIndex() == 0) {
            blockChain.addBlock(block);
            newBlock = block;
        } else if (blockChain.getPrevBlock().nextIndex() == block.getIndex()) {
            blockChain.addBlock(block);
            newBlock = block;
        }
        removeTxByBlock(block);
        return newBlock;
    }

    @Override
    public Block getBlockByIndexOrHash(String indexOrHash) {

        if (isNumeric(indexOrHash)) {
            int index = Integer.parseInt(indexOrHash);
            return blockChain.getBlockByIndex(index);
        } else {
            return blockChain.getBlockByHash(indexOrHash);
        }
    }

    @Override
    public String getNodeUri() {
        return peer.getYnodeUri();
    }

    @Override
    public void addPeer(String ynodeUri) {
        if (peerGroup.contains(ynodeUri)) {
            log.debug("MarioCash node is exist. uri={}", ynodeUri);
            return;
        }
        Peer peer = addPeerByYnodeUri(ynodeUri);
        List<String> peerList = messageSender.broadcastPeerConnect(ynodeUri);
        addPeerByYnodeUri(peerList);
        addActivePeer(peer);
    }

    @Override
    public void removePeer(String ynodeUri) {
        if (peerGroup.removePeer(ynodeUri) != null) {
            messageSender.broadcastPeerDisconnect(ynodeUri);
        }
    }

    @Override
    public List<String> getPeerUriList() {
        return peerGroup.getPeers().stream().map(Peer::getYnodeUri).collect(Collectors.toList());
    }

    private void addPeerByYnodeUri(List<String> peerList) {
        for (String ynodeUri : peerList) {
            addPeerByYnodeUri(ynodeUri);
        }
    }

    private Peer addPeerByYnodeUri(String ynodeUri) {
        try {
            if (peerGroup.count() >= nodeProperties.getMaxPeers()) {
                log.warn("Ignore to add the peer. count={}, peer={}", peerGroup.count(), ynodeUri);
                return null;
            }
            Peer peer = Peer.valueOf(ynodeUri);
            return peerGroup.addPeer(peer);
        } catch (Exception e) {
            log.warn("ynode={}, error={}", ynodeUri, e.getMessage());
        }
        return null;
    }

    private void activatePeers() {
        for (Peer peer : peerGroup.getPeers()) {
            addActivePeer(peer);
        }
    }

    private void addActivePeer(Peer peer) {
        if (peer == null || this.peer.getYnodeUri().equals(peer.getYnodeUri())) {
            return;
        }
        messageSender.newPeerChannel(new GrpcClientChannel(peer));
    }

    private void requestPeerList() {
        List<String> seedPeerList = peerGroup.getSeedPeerList();
        if (seedPeerList == null || seedPeerList.isEmpty()) {
            return;
        }
        for (String ynodeUri : seedPeerList) {
            if (ynodeUri.equals(peer.getYnodeUri())) {
                continue;
            }
            try {
                Peer peer = Peer.valueOf(ynodeUri);
                log.info("Trying to connecting SEED peer at {}", ynodeUri);
                GrpcClientChannel client = new GrpcClientChannel(peer);
                // TODO validation peer(encrypting msg by privateKey and signing by publicKey ...)
                List<String> peerList = client.requestPeerList(getNodeUri(), 0);
                client.stop();
                addPeerByYnodeUri(peerList);
            } catch (Exception e) {
                log.warn("ynode={}, error={}", ynodeUri, e.getMessage());
            }
        }
    }

    private void syncBlockAndTransaction() {
        try {
            List<Block> blockList = messageSender.syncBlock(blockChain.getLastIndex());
            for (Block block : blockList) {
                blockChain.addBlock(block);
            }
            List<Transaction> txList = messageSender.syncTransaction();
            for (Transaction tx : txList) {
                transactionStore.put(tx);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void removeTxByBlock(Block block) {
        if (block == null || block.getData().getTransactionList() == null) {
            return;
        }
        Set<Sha3Hash> keys = new HashSet<>();

        for (Transaction tx : block.getData().getTransactionList()) {
            keys.add(new Sha3Hash(tx.getHashString()));
        }
        this.transactionStore.batch(keys);
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public Wallet getWallet() {
        return wallet;
    }

    @Override
    public void disconnected(Peer peer) {
        removePeer(peer.getYnodeUri());
    }

    public BlockChain getBlockChain() {
        return blockChain;
    }
}
