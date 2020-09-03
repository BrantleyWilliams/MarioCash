/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.node;

import dev.zhihexireng.node.config.NodeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NodeServerRunner implements CommandLineRunner, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(NodeServerRunner.class);

    private final NodeProperties nodeProperties;
    private final GRpcNodeServer nodeServer;

    @Autowired
    public NodeServerRunner(NodeProperties nodeProperties, GRpcNodeServer nodeServer) {
        this.nodeProperties = nodeProperties;
        this.nodeServer = nodeServer;
    }

    @Override
    public void run(String... args) throws Exception {
        String host = nodeProperties.getGrpc().getHost();
        int port = nodeProperties.getGrpc().getPort();
        nodeServer.start(host, port);
        startDaemonAwaitThread();
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(() -> {
            try {
                nodeServer.blockUntilShutdown();
            } catch (InterruptedException e) {
                log.error("gRPC server stopped.", e);
            }
        });

        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    @Override
    public void destroy() {
        log.info("Shutting down gRPC server...");
        nodeServer.stop();
    }
}
