package dev.zhihexireng.node.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.contract.StemContract;
import dev.zhihexireng.core.Runtime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AutoJsonRpcServiceImpl
public class ContractApiImpl implements ContractApi {

    private static final Logger log = LoggerFactory.getLogger(ContractApiImpl.class);
    private final Runtime runtime;

    @Autowired
    public ContractApiImpl(Runtime runtime) {
        this.runtime = runtime;
    }

    @Override
    public String query(String data) throws Exception {
        log.debug("[ContractAPI | data]" + data);
        JsonParser jsonParser = new JsonParser();
        JsonObject query = (JsonObject) jsonParser.parse(data);
        return runtime.query(new StemContract(), query).toString();
    }
}
