package dev.zhihexireng.node.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

import java.io.IOException;

@JsonRpcService("/transaction")
public interface TransactionApi {

    /* get */
    int getTransactionCount(
            @JsonRpcParam(value = "address") String address,
            @JsonRpcParam(value = "tag") String tag);

    int getTransactionCount(
            @JsonRpcParam(value = "address") String address,
            @JsonRpcParam(value = "blockNumber") int blockNumber);

    int getBlockTransactionCountByHash(
            @JsonRpcParam(value = "hashOfBlock") String hashOfBlock);

    int getBlockTransactionCountByNumber(
            @JsonRpcParam(value = "blockNumber") int blockNumber);

    int getBlockTransactionCountByNumber(
            @JsonRpcParam(value = "tag") String tag);

    String getTransactionByHash(
            @JsonRpcParam(value = "hashOfTx") String hashOfTx) throws IOException;

    String getTransactionByBlockHashAndIndex(
            @JsonRpcParam(value = "hashOfBlock") String hashOfBlock,
            @JsonRpcParam(value = "txIndexPosition") int txIndexPosition) throws IOException;

    String getTransactionByBlockNumberAndIndex(
            @JsonRpcParam(value = "blockNumber") int blockNumber,
            @JsonRpcParam(value = "txIndexPosition") int txIndexPosition) throws IOException;

    String getTransactionByBlockNumberAndIndex(
            @JsonRpcParam(value = "tag") String tag,
            @JsonRpcParam(value = "txIndexPosition") int txIndexPosition) throws IOException;

    String getTransactionReceipt(
            @JsonRpcParam(value = "hashOfTx") String hashOfTx);

    /* send */
    String sendTransaction(
            @JsonRpcParam(value = "tx") String tx);

    String sendRawTransaction(
            @JsonRpcParam(value = "rawTx") String rawTx);

    /* filter */
    int newPendingTransactionFilter();

    /* test */
    String getJsonObj(
            @JsonRpcParam(value = "tx") String tx) throws IOException, JsonProcessingException;
}


