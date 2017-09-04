package dev.zhihexireng.node;

import dev.zhihexireng.core.TransactionPool;
import dev.zhihexireng.node.mock.BlockBuilderMock;
import dev.zhihexireng.node.mock.BlockChainMock;
import dev.zhihexireng.node.mock.TransactionPoolMock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class MarioCashNode {

    public static void main(String[] args) {
        SpringApplication.run(MarioCashNode.class, args);
    }

    @Configuration
    class NodeConfig {

        @Bean
        BlockBuilder blockBuilder() {
            return new BlockBuilderMock();
        }

        @Bean
        BlockChain blockChain() {
            return new BlockChainMock();
        }

        @Bean
        TransactionPool transactionPool() {
            return new TransactionPoolMock();
        }
    }
}
