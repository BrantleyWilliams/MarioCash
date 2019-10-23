package dev.zhihexireng.core.genesis;

import dev.zhihexireng.config.DefaultConfig;
import dev.zhihexireng.util.FileUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class GenesisBlockTest {

    private GenesisBlock genesisBlock;

    @Before
    public void setUp() throws Exception {
        this.genesisBlock = new GenesisBlock();

    }

    @Test
    public void generateGenesisBlock() {

        try {
            this.genesisBlock.generateGenesisBlockFile();

            ClassLoader classLoader = getClass().getClassLoader();
            File genesisFile = new File(classLoader.getResource(
                    new DefaultConfig().getConfig().getString("genesis.block")).getFile());
            String genesisString = FileUtil.readFileToString(genesisFile);

            System.out.println(genesisString);

        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

}
