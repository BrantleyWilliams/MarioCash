package dev.zhihexireng.node;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import dev.zhihexireng.core.exception.FailedOperationException;
import dev.zhihexireng.core.net.DiscoveryClient;
import dev.zhihexireng.core.net.KademliaOptions;
import dev.zhihexireng.node.api.PeerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonRpcDiscoverClient implements DiscoveryClient {

    private static final Logger log = LoggerFactory.getLogger(JsonRpcDiscoverClient.class);

    @Override
    public List<String> getAllActivePeer(String host, int port) {
        PeerApi peerApi = peerApi(host, port);
        return peerApi.getAllActivePeer();
    }

    private JsonRpcHttpClient jsonRpcHttpClient(URL endpoint) {
        URL url = null;
        Map<String, String> map = new HashMap<>();
        try {
            url = endpoint;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonRpcHttpClient jsonRpcHttpClient = new JsonRpcHttpClient(objectMapper, url, map);
        jsonRpcHttpClient.setConnectionTimeoutMillis((int) KademliaOptions.REQ_TIMEOUT);
        return jsonRpcHttpClient;
    }

    private PeerApi peerApi(String host, int port) {
        try {
            String spec = "http://" + host + ":" + port + "/api/peer";
            URL url = new URL(spec);
            return ProxyUtil.createClientProxy(getClass().getClassLoader(),
                    PeerApi.class, jsonRpcHttpClient(url));
        } catch (Exception e) {
            throw new FailedOperationException(e);
        }
    }
}
