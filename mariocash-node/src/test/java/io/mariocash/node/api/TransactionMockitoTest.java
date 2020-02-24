package dev.zhihexireng.node.api;

import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionReceipt;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.node.TestUtils;
import dev.zhihexireng.node.controller.TransactionDto;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionMockitoTest {

    @Mock
    private NodeManager nodeManagerMock;
    @Mock
    private TransactionReceiptStore txReceiptStoreMock;
    private TransactionHusk tx;
    private BlockHusk block;
    private Wallet wallet;

    private TransactionApiImpl txApiImpl;
    private String hashOfTx;
    private String hashOfBlock;
    private TransactionReceipt txRecipt;

    private HashMap<String, TransactionReceipt> txReceiptStore;

    @Before
    public void setup() throws Exception {
        txReceiptStore = new HashMap<>();
        wallet = new Wallet();
        txApiImpl = new TransactionApiImpl(nodeManagerMock, txReceiptStoreMock);

        tx = TestUtils.createTxHusk(wallet);
        hashOfTx = tx.getHash().toString();
        List<TransactionHusk> txList = new ArrayList<>();
        txList.add(tx);
        txList.add(tx);
        txList.add(tx);
        txRecipt = new TransactionReceipt();
        txRecipt.setTransactionHash(tx.getHash().toString());
        txReceiptStore.put(tx.getHash().toString(), txRecipt);
        block = TestUtils.createBlockHuskByTxList(wallet, txList);
        hashOfBlock = block.getHash().toString();
    }

    private static final Logger log = LoggerFactory.getLogger(TransactionApi.class);

    @Test
    public void getTransactionCountTest() {
        when(nodeManagerMock.getBlockByIndexOrHash(any())).thenReturn(block);
        Integer res = txApiImpl.getTransactionCount(wallet.getHexAddress(), 1);
        Integer res2 = txApiImpl.getTransactionCount(wallet.getHexAddress(), "latest");
        Integer sizeOfTxList = 3;
//        assertThat(res).isEqualTo(sizeOfTxList);
//        assertThat(res2).isEqualTo(res);
    }

    @Test
    public void hexEncodeAndDecodeByteArray() throws Exception {
        byte[] origin = tx.getAddress().getBytes();
        String encoded = Hex.encodeHexString(origin);
        byte[] decoded = Hex.decodeHex(encoded);

        assertArrayEquals(decoded, origin);
    }

    @Test
    public void getTransactionByHash() {
        when(nodeManagerMock.getTxByHash(hashOfTx)).thenReturn(tx);
        TransactionHusk res = txApiImpl.getTransactionByHash(hashOfTx);
        assertThat(res).isNotNull();
        assertEquals(res.getHash().toString(), hashOfTx);
    }

    @Test
    public void getTransactionByBlockHashAndIndexTest() {
        when(nodeManagerMock.getBlockByIndexOrHash(hashOfBlock)).thenReturn(block);
        TransactionHusk res = txApiImpl.getTransactionByBlockHashAndIndex(hashOfBlock, 0);
        assertEquals(res.getHash().toString(), hashOfTx);
    }

    @Test
    public void getTransactionByBlockNumberAndIndexTest() {
        when(nodeManagerMock.getBlockByIndexOrHash(anyString())).thenReturn(block);
        TransactionHusk res = txApiImpl.getTransactionByBlockNumberAndIndex(0, 0);
        TransactionHusk res2 = txApiImpl.getTransactionByBlockNumberAndIndex("latest", 0);
        assertEquals(res.getHash(), res2.getHash());
    }

    @Test
    public void getTransactionReceiptTest() {
        when(txReceiptStoreMock.get(hashOfTx)).thenReturn(txRecipt);
        TransactionReceipt res = txApiImpl.getTransactionReceipt(hashOfTx);
        assertEquals(res.transactionHash, hashOfTx);
    }

    @Test
    public void getAllTransactionReceiptTest() {
        when(txReceiptStoreMock.getTxReciptStore()).thenReturn(txReceiptStore);
        HashMap<String, TransactionReceipt> res = txApiImpl.getAllTransactionReceipt();
        assertThat(res.containsKey(hashOfTx)).isTrue();
    }

    @Test
    public void sendTransactionTest() {
        when(nodeManagerMock.addTransaction(tx)).thenReturn(tx);
        String res = txApiImpl.sendTransaction(TransactionDto.createBy(tx));
        assertEquals(res, hashOfTx);
    }

    @Test
    public void sendRawTransaction() throws Exception {
        when(nodeManagerMock.addTransaction(any(TransactionHusk.class))).thenReturn(tx);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(tx);
        out.flush();
        byte[] txBytes = bos.toByteArray();
        bos.close();
        byte[] res = txApiImpl.sendRawTransaction(txBytes);
        log.debug("\n\nres :: " + Hex.encodeHexString(res));
        assertThat(res).isNotEmpty();
    }
}
