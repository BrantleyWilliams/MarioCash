package dev.zhihexireng.core.genesis;

import dev.zhihexireng.config.DefaultConfig;
import dev.zhihexireng.util.FileUtil;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class GenesisBlockTest {

    private static final Logger log = LoggerFactory.getLogger(GenesisBlockTest.class);

    private GenesisBlock genesisBlock;

    @Before
    public void setUp() throws Exception {
        this.genesisBlock = new GenesisBlock();

    }

    @Test(expected = NullPointerException.class)
    public void generateGenesisBlock() throws IOException {
        this.genesisBlock.generateGenesisBlockFile();

        ClassLoader classLoader = getClass().getClassLoader();
        File genesisFile = new File(classLoader.getResource(
                new DefaultConfig().getConfig().getString("genesis.block")).getFile());
        String genesisString = FileUtil.readFileToString(genesisFile);

        log.debug(genesisString);
    }

}
