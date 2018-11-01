package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.node.exception.InternalErrorException;
import dev.zhihexireng.node.exception.NonExistObjectException;
import dev.zhihexireng.node.mock.BlockMock;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AutoJsonRpcServiceImpl
public class BlockApiImpl implements BlockApi {

    @Override
    public int blockNumber() {
        try {
            return 0;
        } catch (Exception exception) {
            throw new InternalErrorException();
        }
    }

    @Override
    public String getBlockByHash(String address, String tag) {
        try {
            BlockMock blockMock = new BlockMock();
            return blockMock.retBlockMock();
        } catch (Exception exception) {
            throw new NonExistObjectException("block");
        }
    }

    @Override
    public String getBlockByNumber(String hashOfBlock, Boolean bool) {
        try {
            BlockMock blockMock = new BlockMock();
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



