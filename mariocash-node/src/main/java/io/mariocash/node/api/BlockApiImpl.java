package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.exception.InternalErrorException;
import dev.zhihexireng.core.exception.NonExistObjectException;
import dev.zhihexireng.node.controller.BlockDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AutoJsonRpcServiceImpl
public class BlockApiImpl implements BlockApi {

    private final BranchGroup branchGroup;

    @Autowired
    public BlockApiImpl(BranchGroup branchGroup) {
        this.branchGroup = branchGroup;
    }

    @Override
    public int blockNumber() {
        try {
            return branchGroup.getBlocks().size();
        } catch (Exception exception) {
            throw new InternalErrorException();
        }
    }

    @Override
    public Set<BlockHusk> getAllBlock() {
        return branchGroup.getBlocks();
    }

    @Override
    public BlockHusk getBlockByHash(String hashOfBlock, Boolean bool) {
        try {
            return branchGroup.getBlockByIndexOrHash(hashOfBlock);
        } catch (Exception exception) {
            throw new NonExistObjectException("block");
        }
    }

    @Override
    public BlockHusk getBlockByNumber(String numOfBlock, Boolean bool) {
        try {
            return branchGroup.getBlockByIndexOrHash(numOfBlock);
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

    @Override
    public BlockHusk getLastBlock() {
        return nodeManager.getBlocks().stream().sorted(Comparator.reverseOrder())
                .collect(Collectors.toList()).get(0);
    }
}
