package dev.zhihexireng.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class DefaultConfigTest {

    /**
     * This is the default config test code.
     * get system config, spring config, mariocash.conf config.
     */
    @Test
    public void defaultConfigTest() {
        DefaultConfig defaultConfig = new DefaultConfig();
        for (Map.Entry<String, ConfigValue> entry : defaultConfig.getConfig().entrySet()) {
            System.out.println("Name:  " + entry.getKey());
            System.out.println(entry);
        }

        assertThat(defaultConfig.getConfig().getString("java.version"), containsString("1.8"));
        System.out.println("DefaultConfig java.version: "
                + defaultConfig.getConfig().getString("java.version"));

    }

    /**
     * This is the config test as java version.
     */
    @Test
    public void javaVersionConfigTest() {
        DefaultConfig defaultConfig = new DefaultConfig();

        assertThat(defaultConfig.getConfig().getString("java.version"), containsString("1.8"));

        System.out.println("DefaultConfig java.version: "
                + defaultConfig.getConfig().getString("java.version"));

    }

    /**
     * This is the config test as mariocash.conf.
     */
    @Test
    public void mariocashConfConfigTest() {
        DefaultConfig defaultConfig = new DefaultConfig();

        assertThat(defaultConfig.getConfig().getString("node.name"), containsString("mariocash"));

        System.out.println("mariocash.conf node.name: "
                + defaultConfig.getConfig().getString("node.name"));

    }

    /**
     * This is the test for new config file mariocash_sample.conf.
     * get system config, spring config, mariocash.conf & mariocash_sample.conf config.
     */
    @Test
    public void newConfigFileTest() {
        Config config = ConfigFactory.parseResources("mariocash_sample.conf");

        DefaultConfig defaultConfig = new DefaultConfig(config);

        assertThat(defaultConfig.getConfig().getString("key.path"), containsString("nodePri2.key"));

        System.out.println("newConfigFile key.path: "
                + defaultConfig.getConfig().getString("key.path"));

    }

    /**
     * This is the test for printing Class.
     */
    @Test
    public void testToString() {
        DefaultConfig defaultConfig = new DefaultConfig();

        System.out.println(defaultConfig.toString());
    }
}
