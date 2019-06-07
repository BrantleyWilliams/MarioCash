package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.node.exception.InternalErrorException;
import dev.zhihexireng.node.exception.NonExistObjectException;
import org.springframework.stereotype.Service;

import java.util.Set;

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
            return nodeManager.getBlocks().size();
        } catch (Exception exception) {
            throw new InternalErrorException();
        }
    }

    @Override
    public Set<Block> getAllBlock() {
        return nodeManager.getBlocks();
    }

    @Override
    public Block getBlockByHash(String hashOfBlock, Boolean bool) {
        try {
            return nodeManager.getBlockByIndexOrHash(hashOfBlock);
        } catch (Exception exception) {
            throw new NonExistObjectException("block");
        }
    }

    @Override
    public Block getBlockByNumber(String numOfBlock, Boolean bool) {
        try {
            return nodeManager.getBlockByIndexOrHash(numOfBlock);
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



