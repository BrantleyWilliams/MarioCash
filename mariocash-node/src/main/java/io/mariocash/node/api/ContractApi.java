package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrors;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import dev.zhihexireng.core.exception.NonExistObjectException;

@JsonRpcService("/api/contract")
public interface ContractApi {

    /**
     * Handles all queries that are dispatched to the contract
     * @param data query string
     * @return result of query
     * @throws Exception exception
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    String query(@JsonRpcParam(value = "data") String data) throws Exception;
}
