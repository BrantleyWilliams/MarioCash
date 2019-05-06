package dev.zhihexireng.node.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class JsonRpcConfig {
    private static final String endpoint = "http://localhost:8080/api/transaction";

    @Bean
    public JsonRpcHttpClient jsonRpcHttpClient() {
        URL url = null;
        //You can add authentication headers etc to this map
        Map<String, String> map = new HashMap<>();
        try {
            url = new URL(endpoint);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new JsonRpcHttpClient(objectMapper, url, map);
    }
}
