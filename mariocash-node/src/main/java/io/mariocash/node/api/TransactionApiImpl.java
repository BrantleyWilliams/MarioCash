package dev.zhihexireng.node.api;

import com.google.common.primitives.Longs;
import com.google.protobuf.ByteString;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionReceipt;
import dev.zhihexireng.core.exception.NonExistObjectException;
import dev.zhihexireng.core.store.TransactionReceiptStore;
import dev.zhihexireng.node.controller.TransactionDto;
import dev.zhihexireng.proto.Proto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AutoJsonRpcServiceImpl
public class TransactionApiImpl implements TransactionApi {

    private static final Logger log = LoggerFactory.getLogger(TransactionApiImpl.class);

    private final BranchGroup branchGroup;
    private final TransactionReceiptStore txReceiptStore;

    @Autowired
    public TransactionApiImpl(BranchGroup branchGroup, TransactionReceiptStore txReceiptStore) {
        this.branchGroup = branchGroup;
        this.txReceiptStore = txReceiptStore;
    }

    public int getCount(String address, List<TransactionHusk> txList) {
        int cnt = 0;
        for (TransactionHusk tx : txList) {
            if (address.equals(tx.getAddress().toString())) {
                cnt += 1;
            }
        }
        return cnt;
    }

    /* get */
    @Override
    public int getTransactionCount(String address, String tag) {
        int blockNumber;
        if ("latest".equals(tag)) {
            blockNumber = 1;
        } else {
            blockNumber = -1;
        }
        BlockHusk block = branchGroup.getBlockByIndexOrHash(String.valueOf(blockNumber));
        return getCount(address, block.getBody());
    }

    @Override
    public int getTransactionCount(String address, int blockNumber) {
        BlockHusk block = branchGroup.getBlockByIndexOrHash(String.valueOf(blockNumber));
        return getCount(address, block.getBody());
    }

    @Override
    public int getBlockTransactionCountByHash(String hashOfBlock) {
        BlockHusk block = branchGroup.getBlockByIndexOrHash(hashOfBlock);
        return block.getBody().size();
    }

    @Override
    public int getBlockTransactionCountByNumber(int blockNumber) {
        return getBlockTransactionCountByHash(String.valueOf(blockNumber));
    }

    @Override
    public int getBlockTransactionCountByNumber(String tag) {
        if ("latest".equals(tag)) {
            return getBlockTransactionCountByNumber(0);
        } else {
            return 0;
        }
    }

    @Override
    public TransactionHusk getTransactionByHash(String hashOfTx) {
        TransactionHusk tx = branchGroup.getTxByHash(hashOfTx);
        if (tx == null) {
            throw new NonExistObjectException("Transaction");
        }
        return tx;
    }

    @Override
    public TransactionHusk getTransactionByBlockHashAndIndex(
            String hashOfBlock, int txIndexPosition) {
        BlockHusk block = branchGroup.getBlockByIndexOrHash(hashOfBlock);
        return block.getBody().get(txIndexPosition);
    }

    @Override
    public TransactionHusk getTransactionByBlockNumberAndIndex(
            int blockNumber, int txIndexPosition) {
        BlockHusk block = branchGroup.getBlockByIndexOrHash(String.valueOf(blockNumber));
        return block.getBody().get(txIndexPosition);
    }

    @Override
    public TransactionHusk getTransactionByBlockNumberAndIndex(String tag, int txIndexPosition) {
        if ("latest".equals(tag)) {
            return getTransactionByBlockNumberAndIndex(0, txIndexPosition);
        } else {
            return null;
        }
    }

    /* send */
    @Override
    public String sendTransaction(TransactionDto tx) {
        TransactionHusk addedTx = branchGroup.addTransaction(TransactionDto.of(tx));
        return addedTx.getHash().toString();
    }

    @Override
    public byte[] sendRawTransaction(byte[] bytes) {
        TransactionHusk tx = convert(bytes);
        TransactionHusk addedTx = branchGroup.addTransaction(tx);
        return addedTx.getHash().getBytes();
    }

    /* filter */
    @Override
    public int newPendingTransactionFilter() {
        return 6;
    }

    private TransactionHusk convert(byte[] bytes) {

        int sum = 0;
        byte[] type = new byte[4];
        type = Arrays.copyOfRange(bytes, sum, sum += type.length);
        byte[] version = new byte[4];
        version = Arrays.copyOfRange(bytes, sum, sum += version.length);
        byte[] dataHash = new byte[32];
        dataHash = Arrays.copyOfRange(bytes, sum, sum += dataHash.length);
        byte[] timestampByte = new byte[8];
        timestampByte = Arrays.copyOfRange(bytes, sum, sum += timestampByte.length);
        byte[] dataSizeByte = new byte[8];
        dataSizeByte = Arrays.copyOfRange(bytes, sum, sum += dataSizeByte.length);
        byte[] signature = new byte[65];
        signature = Arrays.copyOfRange(bytes, sum, sum += signature.length);
        byte[] dataByte = Arrays.copyOfRange(bytes, sum, bytes.length);

        long timestamp = Longs.fromByteArray(timestampByte);
        long dataSize = Longs.fromByteArray(dataSizeByte);
        String data = new String(dataByte);

        Proto.Transaction.Header transactionHeader = Proto.Transaction.Header.newBuilder()
                .setRawData(Proto.Transaction.Header.Raw.newBuilder()
                        .setType(ByteString.copyFrom(type))
                        .setVersion(ByteString.copyFrom(version))
                        .setDataHash(ByteString.copyFrom(dataHash))
                        .setDataSize(dataSize)
                        .setTimestamp(timestamp)
                        .build())
                .setSignature(ByteString.copyFrom(signature))
                .build();

        Proto.Transaction tx = Proto.Transaction.newBuilder()
                .setHeader(transactionHeader)
                .setBody(data)
                .build();

        return new TransactionHusk(tx);
    }

    @Override
    public Map<String, TransactionReceipt> getAllTransactionReceipt() {
        return txReceiptStore.getTxReceiptStore();
    }

    @Override
    public TransactionReceipt getTransactionReceipt(String hashOfTx) {
        return txReceiptStore.get(hashOfTx);
    }
}
