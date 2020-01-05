package dev.zhihexireng.core;

import dev.zhihexireng.TestUtils;
import dev.zhihexireng.config.Constants;
import dev.zhihexireng.config.DefaultConfig;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class BlockChainTest {
    private static final Logger log = LoggerFactory.getLogger(BlockChainTest.class);
    private static Wallet wallet;
    private static DefaultConfig defaultConfig;
    private String chainId = "chainId";
    private File sampleBranchInfo;

    @Before
    public void init() throws Exception {
        defaultConfig = new DefaultConfig();
        wallet = new Wallet(defaultConfig);
        sampleBranchInfo = new File(Objects.requireNonNull(getClass().getClassLoader()
                .getResource("branch-sample.json")).getFile());
    }

    @After
    public void tearDown() throws Exception {
        clearTestDb();
    }

    @Test
    public void shouldBeGetBlockByHash() {
        BlockChain blockChain = generateTestBlockChain();
        BlockHusk prevBlock = blockChain.getPrevBlock(); // goto Genesis
        long blockIndex = blockChain.size();
        BlockHusk testBlock = new BlockHusk(
                TestUtils.getBlockFixture(blockIndex, prevBlock.getHash()));
        blockChain.addBlock(testBlock);

        assertThat(blockChain.getBlockByHash(testBlock.getHash()))
                .isEqualTo(testBlock);
    }

    @Test
    public void shouldBeGetBlockByIndex() {
        BlockChain blockChain = generateTestBlockChain();
        BlockHusk prevBlock = blockChain.getPrevBlock(); // goto Genesis
        long blockIndex = blockChain.size();
        BlockHusk testBlock = new BlockHusk(
                TestUtils.getBlockFixture(blockIndex, prevBlock.getHash()));
        blockChain.addBlock(testBlock);

        assertThat(blockChain.getBlockByIndex(blockIndex))
                .isEqualTo(testBlock);
    }

    @Test
    public void shouldBeVerifiedBlockChain() {
        BlockChain blockChain = generateTestBlockChain();
        assertThat(blockChain.isValidChain()).isEqualTo(true);
    }

    private BlockChain generateTestBlockChain() {
        BlockChain blockChain = new BlockChain(sampleBranchInfo);
        BlockHusk genesisBlock = blockChain.getGenesisBlock();
        BlockHusk block1 = new BlockHusk(
                TestUtils.getBlockFixture(1L, genesisBlock.getHash()));
        blockChain.addBlock(block1);
        BlockHusk block2 = new BlockHusk(
                TestUtils.getBlockFixture(2L, block1.getHash()));
        blockChain.addBlock(block2);
        return blockChain;
    }

    @Test(expected = NotValidateException.class)
    public void shouldBeExceptedNotValidateException() {
        BlockChain blockChain = new BlockChain(sampleBranchInfo);
        BlockHusk block1 = new BlockHusk(TestUtils.getBlockFixture(1L));
        blockChain.addBlock(block1);
        BlockHusk block2 = new BlockHusk(TestUtils.getBlockFixture(2L));
        blockChain.addBlock(block2);
        blockChain.isValidChain();
    }

    @Test
    public void shouldBeLoadedStoredBlocks() {
        BlockChain blockChain = new BlockChain(sampleBranchInfo);

        BlockHusk testBlock = new BlockHusk(TestUtils.getBlockFixture(1L));
        blockChain.addBlock(testBlock);
        blockChain.close();

        BlockChain otherBlockChain = new BlockChain(sampleBranchInfo);
        BlockHusk foundBlock = otherBlockChain.getBlockByHash(testBlock.getHash());
        assertThat(otherBlockChain.size()).isEqualTo(2);
        assertThat(testBlock).isEqualTo(foundBlock);
    }

    @Test
    public void shouldBeCreatedNewBlockChain() {
        BlockChain blockChain = new BlockChain(sampleBranchInfo);
        assertThat(blockChain.size()).isEqualTo(1L);
    }

    private void clearTestDb() {
        String dbPath = defaultConfig.getConfig().getString(Constants.DATABASE_PATH);
        FileUtil.recursiveDelete(Paths.get(dbPath));
    }
}
