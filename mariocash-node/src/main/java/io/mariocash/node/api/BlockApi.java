package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrors;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.exception.InternalErrorException;
import dev.zhihexireng.core.exception.NonExistObjectException;

import java.util.Set;

@JsonRpcService("/api/block")
public interface BlockApi {
    /**
     * Returns the number of most recent block.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = InternalErrorException.class,
                    code = InternalErrorException.code)})
    int blockNumber();

    /**
     * Returns all blocks.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = InternalErrorException.class,
                          code = InternalErrorException.code)})
    Set<BlockHusk> getAllBlock();

    /**
     * Returns information about a block by hash.
     *
     * @param hashOfBlock Hash of block
     * @param bool        If true, it returns the full transaction objects,
     *                    if false only the hashes of the transactions.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    BlockHusk getBlockByHash(@JsonRpcParam(value = "hashOfBlock") String hashOfBlock,
                         @JsonRpcParam(value = "bool") Boolean bool);

    /**
     * Returns information about a block by block number.
     *
     * @param numOfBlock  Number of block
     * @param bool        If true, it returns the full transaction objects,
     *                    if false only the hashes of the transactions.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    BlockHusk getBlockByNumber(@JsonRpcParam(value = "numOfBlock") String numOfBlock,
                           @JsonRpcParam(value = "bool") Boolean bool);

    /**
     * Creates a filter in the node, to notify when a new block arrives.
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = InternalErrorException.class,
                    code = InternalErrorException.code)})
    int newBlockFilter();
}
