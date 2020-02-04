package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrors;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionReceipt;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.exception.NonExistObjectException;
import dev.zhihexireng.core.exception.RejectedAccessException;
import dev.zhihexireng.node.controller.TransactionDto;

import java.util.HashMap;

@JsonRpcService("/api/transaction")
public interface TransactionApi {

    /* get */

    /**
     * Returns the number of transactions sent from an address.
     *
     * @param address account address
     * @param tag     "latest","earlest","pending"
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    int getTransactionCount(
            @JsonRpcParam(value = "address") String address,
            @JsonRpcParam(value = "tag") String tag);

    /**
     * Returns information about a block by hash.
     *
     * @param address     account address
     * @param blockNumber integer of block number
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    int getTransactionCount(
            @JsonRpcParam(value = "address") String address,
            @JsonRpcParam(value = "blockNumber") int blockNumber);

    /**
     * Returns the number of transactions in a block from a block matching the given block hash.
     *
     * @param hashOfBlock hash of block
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    int getBlockTransactionCountByHash(
            @JsonRpcParam(value = "hashOfBlock") String hashOfBlock);

    /**
     * Returns the number of transactions in a block matching the given block number.
     *
     * @param blockNumber integer of block number
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    int getBlockTransactionCountByNumber(
            @JsonRpcParam(value = "blockNumber") int blockNumber);

    /**
     * Returns the number of transactions in a block matching the given block number.
     *
     * @param tag "latest","earlest","pending"
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    int getBlockTransactionCountByNumber(
            @JsonRpcParam(value = "tag") String tag);

    /**
     * Returns the information about a transaction requested by transaction hash.
     *
     * @param hashOfTx hash of transaction
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    TransactionHusk getTransactionByHash(
            @JsonRpcParam(value = "hashOfTx") String hashOfTx);

    /**
     * Returns information about a transaction by block hash and transaction index position.
     *
     * @param hashOfBlock     hash of block
     * @param txIndexPosition integer of the transaction index position.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    TransactionHusk getTransactionByBlockHashAndIndex(
            @JsonRpcParam(value = "hashOfBlock") String hashOfBlock,
            @JsonRpcParam(value = "txIndexPosition") int txIndexPosition);

    /**
     * Returns information about a transaction by block number and transaction index position.
     *
     * @param blockNumber     a block number
     * @param txIndexPosition the transaction index position.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    TransactionHusk getTransactionByBlockNumberAndIndex(
            @JsonRpcParam(value = "blockNumber") int blockNumber,
            @JsonRpcParam(value = "txIndexPosition") int txIndexPosition);

    /**
     * Returns information about a transaction by block number and transaction index position.
     *
     * @param tag             "latest","earlest","pending"
     * @param txIndexPosition the transaction index position.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    TransactionHusk getTransactionByBlockNumberAndIndex(
            @JsonRpcParam(value = "tag") String tag,
            @JsonRpcParam(value = "txIndexPosition") int txIndexPosition);


    /* send */

    /**
     * Creates new message call transaction or a contract creation,
     * if the data field contains code.
     *
     * @param tx The transaction
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = FailedOperationException.class,
                    code = FailedOperationException.code)})
    String sendTransaction(
            @JsonRpcParam(value = "tx") TransactionDto tx);

    /**
     * Creates new message call transaction or a contract creation for signed transactions.
     *
     * @param rawTx The signed transaction data.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = FailedOperationException.class,
                    code = FailedOperationException.code)})
    byte[] sendRawTransaction(
            @JsonRpcParam(value = "rawTx") byte[] rawTx);

    /**
     * Creates a filter in the node, to notify when new pending transactions arrive.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = RejectedAccessException.class,
                    code = RejectedAccessException.code)})
    int newPendingTransactionFilter();

    /**
     * Returns all TransactionRecipts
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    HashMap<String, TransactionReceipt> getAllTransactionReceipt();

    /**
     * Returns the TransactionRecipt of transaction hash
     *
     * @param hashOfTx  hash of transaction
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    TransactionReceipt getTransactionReceipt(
            @JsonRpcParam(value = "hashOfTx") String hashOfTx);
}
