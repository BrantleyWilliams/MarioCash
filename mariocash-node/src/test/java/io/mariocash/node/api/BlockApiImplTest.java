package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import dev.zhihexireng.core.Block;
import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.node.MessageSender;
import dev.zhihexireng.node.config.NodeProperties;
import dev.zhihexireng.node.mock.BlockBuilderMock;
import dev.zhihexireng.node.mock.BlockMock;
import dev.zhihexireng.node.mock.NodeManagerMock;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockApiImplTest {
    private static final Logger log = LoggerFactory.getLogger(TransactionApi.class);

    private final NodeManager nodeManager
            = new NodeManagerMock(new MessageSender(), null, new NodeProperties.Grpc());

    private final JsonRpcHttpClient jsonRpcHttpClient = new JsonRpcConfig().jsonRpcHttpClient();

    @Test
    public void blockNumberTest() {
        try {
            BlockApi api = ProxyUtil.createClientProxy(getClass().getClassLoader(),
                    BlockApi.class, jsonRpcHttpClient);
            assertThat(api).isNotNull();
            assertThat(api.blockNumber()).isZero();
        } catch (Exception exception) {
            log.debug("blockNumberTest :: exception : " + exception);
        }
    }

    @Test
    public void getBlockByHashTest() {
        try {
            BlockApi api = ProxyUtil.createClientProxy(getClass().getClassLoader(),
                    BlockApi.class, jsonRpcHttpClient);
            assertThat(api).isNotNull();
            assertThat(api.getBlockByHash("0x2Aa4BCaC31F7F67B9a15681D5e4De2FBc778066A",
                    true)).isNotNull();
        } catch (Exception exception) {
            log.debug("getBlockByHashTest :: exception : " + exception);
        }
    }

    @Test
    public void getBlockByNumberTest() {
        try {
            BlockApi api = ProxyUtil.createClientProxy(getClass().getClassLoader(),
                    BlockApi.class, jsonRpcHttpClient);
            assertThat(api).isNotNull();
            assertThat(api.getBlockByNumber("0xbbF5029Fd710d227630c8b7d338051B8E76d50B3",
                    true)).isNotNull();
        } catch (Exception exception) {
            log.debug("getBlockByNumberTest :: exception : " + exception);
        }
    }

    @Test
    public void createBlockMock() throws IOException {
        BlockMock blockMock = new BlockMock(nodeManager);
        log.debug("blockMock" + blockMock.retBlockMock());
    }

    @Test
    public void blockBuildMockTest() throws IOException {
        BlockBuilderMock blockBuilderMock = new BlockBuilderMock(nodeManager);
        Block block = blockBuilderMock.build(nodeManager.getWallet());
        log.debug("blockBuilderMock : " + block.toString());
    }

    @Test
    public void newBlockFilter() {
        try {
            BlockApi api = ProxyUtil.createClientProxy(getClass().getClassLoader(),
                    BlockApi.class, jsonRpcHttpClient);
            assertThat(api).isNotNull();
            assertThat(api.newBlockFilter()).isZero();
        } catch (Exception exception) {
            log.debug("newBlockFilter :: exception : " + exception);
        }
    }

}
