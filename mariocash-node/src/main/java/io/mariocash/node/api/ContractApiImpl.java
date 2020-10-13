package dev.zhihexireng.node.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.contract.Contract;
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
    private final Contract contract;

    @Autowired
    public ContractApiImpl(Runtime runtime, Contract contract) {
        this.runtime = runtime;
        this.contract = contract;
    }

    @Override
    public String query(String data) throws Exception {
        log.debug("[ContractAPI | data]" + data);
        JsonParser jsonParser = new JsonParser();
        JsonObject query = (JsonObject) jsonParser.parse(data);
        return runtime.query(contract, query).toString();
    }
}
