package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrors;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.exception.NonExistObjectException;

import java.util.Collection;
import java.util.List;

@JsonRpcService("/api/peer")
public interface PeerApi {
    /**
     * Returns all peers
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    Peer add(@JsonRpcParam(value = "peer") Peer peer);

    /**
     * Returns all peers
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    Collection<Peer> getAll();

    /**
     * Returns all active peers
     */
    @JsonRpcErrors({
            @JsonRpcError(exception = NonExistObjectException.class,
                    code = NonExistObjectException.code)})
    List<String> getAllActivePeer();
}
