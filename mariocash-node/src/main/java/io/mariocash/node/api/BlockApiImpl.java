package dev.zhihexireng.node.api;

import com.google.gson.JsonObject;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.BlockBody;
import dev.zhihexireng.core.BlockHeader;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.Wallet;
import dev.zhihexireng.node.exception.InternalErrorException;
import dev.zhihexireng.node.exception.NonExistObjectException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AutoJsonRpcServiceImpl
public class BlockApiImpl implements BlockApi {

    private final NodeManager nodeManager;

    @Autowired
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
            return retBlockMock();
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
            return retBlockMock();
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

    public Block retBlockMock() {
        // Create transactions
        JsonObject txObj1 = new JsonObject();

        txObj1.addProperty("operator", "transfer");
        txObj1.addProperty("to", "0x9843DC167956A0e5e01b3239a0CE2725c0631392");
        txObj1.addProperty("value", 30);

        JsonObject txObj2 = new JsonObject();
        txObj2.addProperty("operator", "transfer");
        txObj2.addProperty("to", "0xdB44902E6cE92fa71Bbf06312630Cb39c5bE756C");
        txObj2.addProperty("value", 40);

        JsonObject txObj3 = new JsonObject();
        txObj3.addProperty("operator", "transfer");
        txObj3.addProperty("to", "0xA0A2fceBF3f3cc182eCfcbB65042Af0fB43dd864");
        txObj3.addProperty("value", 50);

        Wallet wallet = nodeManager.getWallet();
        Transaction tx1 = new Transaction(wallet, txObj1);
        Transaction tx2 = new Transaction(wallet, txObj2);
        Transaction tx3 = new Transaction(wallet, txObj3);

        List<Transaction> txList = new ArrayList<>();
        txList.add(tx1);
        txList.add(tx2);
        txList.add(tx3);

        // Create a blockBody
        BlockBody blockBody = new BlockBody(txList);

        // Create a blockHeader
        BlockHeader blockHeader = new BlockHeader.Builder()
                .prevBlock(null)
                .blockBody(blockBody).build(nodeManager.getWallet());

        // Return a created block
        return new Block(blockHeader, blockBody);
    }
}
