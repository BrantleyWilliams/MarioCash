package dev.zhihexireng.node.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.TransactionHeader;
import dev.zhihexireng.core.TransactionValidator;
import dev.zhihexireng.node.exception.FailedOperationException;
import dev.zhihexireng.node.mock.TransactionMock;
import dev.zhihexireng.node.mock.TransactionReceiptMock;
import org.spongycastle.util.Arrays;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SignatureException;

@Service
@AutoJsonRpcServiceImpl
public class TransactionApiImpl implements TransactionApi {

    private final NodeManager nodeManager;

    public TransactionApiImpl(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }


    /* get */
    @Override
    public int getTransactionCount(String address, String tag) {
        return 1;
    }

    @Override
    public int getTransactionCount(String address, int blockNumber) {
        return 2;
    }

    @Override
    public int getBlockTransactionCountByHash(String hashOfBlock) {
        return 3;
    }

    @Override
    public int getBlockTransactionCountByNumber(int blockNumber) {
        return 4;
    }

    @Override
    public int getBlockTransactionCountByNumber(String tag) {
        return 5;
    }

    @Override
    public Transaction getTransactionByHash(String hashOfTx) throws IOException {
        TransactionMock txMock = new TransactionMock(this.nodeManager);
        return txMock.retTxMock();
    }

    @Override
    public Transaction getTransactionByBlockHashAndIndex(
            String hashOfBlock, int txIndexPosition) throws IOException {
        TransactionMock txMock = new TransactionMock(this.nodeManager);
        return txMock.retTxMock();
    }

    @Override
    public Transaction getTransactionByBlockNumberAndIndex(
            int blockNumber, int txIndexPosition) throws IOException {
        TransactionMock txMock = new TransactionMock(this.nodeManager);
        return txMock.retTxMock();
    }

    @Override
    public Transaction getTransactionByBlockNumberAndIndex(
            String tag, int txIndexPosition) throws IOException {
        TransactionMock txMock = new TransactionMock(this.nodeManager);
        return txMock.retTxMock();
    }

    @Override
    public TransactionReceiptMock getTransactionReceipt(String hashOfTx) {
        return new TransactionReceiptMock();
    }

    /* send */
    @Override
    public String sendTransaction(String jsonStr) throws IOException,SignatureException {
        Transaction tx = convert(jsonStr);
        if (valiate(tx)) {
            return tx.getHashString();
        } else {
            throw new FailedOperationException("Transaction");
        }
    }

    @Override
    public byte[] sendRawTransaction(byte[] bytes) throws IOException,SignatureException {
        Transaction tx = convert(bytes);
        if (valiate(tx)) {
            return tx.getHash();
        } else {
            throw new FailedOperationException("Transaction");
        }
    }

    /* filter */
    @Override
    public int newPendingTransactionFilter() {
        return 6;
    }

    private Transaction convert(String jsonStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Transaction tx = mapper.readValue(jsonStr, Transaction.class);

        return tx;
    }

    private Transaction convert(byte[] bytes) {

        int sum = 0;
        byte[] type = new byte[4];
        type = Arrays.copyOfRange(bytes, sum, sum += type.length);
        byte[] version = new byte[4];
        version = Arrays.copyOfRange(bytes, sum, sum += version.length);
        byte[] dataHash = new byte[32];
        dataHash = Arrays.copyOfRange(bytes, sum, sum += dataHash.length);
        byte[] timestamp = new byte[8];
        timestamp = Arrays.copyOfRange(bytes, sum, sum += timestamp.length);
        byte[] dataSize = new byte[8];
        dataSize = Arrays.copyOfRange(bytes, sum, sum += dataSize.length);
        byte[] signature = new byte[65];
        signature = Arrays.copyOfRange(bytes, sum, sum += signature.length);
        byte[] data = Arrays.copyOfRange(bytes, sum, bytes.length);


        Long timestampStr = Longs.fromByteArray(timestamp);
        Long dataSizeStr = Longs.fromByteArray(dataSize);
        String dataStr = new String(data);

        TransactionHeader txHeader;
        txHeader = new TransactionHeader(
                type, version, dataHash, timestampStr, dataSizeStr, signature);

        return new Transaction(txHeader, dataStr);
    }

    private Boolean valiate(Transaction tx) throws IOException,SignatureException {
        TransactionValidator txValidator = new TransactionValidator();
        return txValidator.txSigValidate(tx.getHeader().getSignDataHash(),
                                         tx.getHeader().getSignature());
    }
}
