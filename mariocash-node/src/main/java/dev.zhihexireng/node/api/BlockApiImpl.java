package dev.zhihexireng.node.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.node.exception.InternalErrorException;
import dev.zhihexireng.node.exception.NonExistObjectException;
import dev.zhihexireng.node.mock.BlockMock;
import org.springframework.stereotype.Service;

@Service
@AutoJsonRpcServiceImpl
public class BlockApiImpl implements BlockApi {

    private final NodeManager nodeManager;

    public BlockApiImpl(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @Override
    public int blockNumber() {
        try {
            return 0;
        } catch (Exception exception) {
            throw new InternalErrorException();
        }
    }

    @Override
    public Block getBlockByHash(String address, Boolean bool) {
        try {
            //todo: getBlockByNumber
            BlockMock blockMock = new BlockMock(nodeManager);
            Block block = blockMock.retBlockMock();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonStr = mapper.writeValueAsString(block);
            return mapper.readValue(jsonStr, Block.class);
        } catch (Exception exception) {
            System.out.println("\n\nException :: getBlockHashImp");
            exception.printStackTrace();
            throw new NonExistObjectException("block");
        }
    }

    @Override
    public Block getBlockByNumber(String hashOfBlock, Boolean bool) {
        try {
            //todo: getBlockByNumber
            BlockMock blockMock = new BlockMock(nodeManager);
            return blockMock.retBlockMock();
        } catch (Exception exception) {
            throw new NonExistObjectException("block");
        }
    }

    @Override
    public int newBlockFilter() {
        try {
            return 0;
        } catch (Exception exception) {
            throw new InternalErrorException();
        }
    }
}



