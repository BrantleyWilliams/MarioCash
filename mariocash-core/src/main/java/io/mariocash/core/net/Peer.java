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

package dev.zhihexireng.core.net;

import dev.zhihexireng.core.Account;
import org.spongycastle.util.encoders.Hex;

import java.net.URI;
import java.net.URISyntaxException;

public class Peer {
    public static final String MARIOCASH_NODE_SCHEMA = "ynode";
    private static final String PEER_URI_FORMAT = "%s://%s@%s";
    private byte[] id;
    private String host;
    private int port;
    private String ynodeUri;

    public Peer() {
    }

    private Peer(String ynodeUri) {
        try {
            URI uri = new URI(ynodeUri);
            if (!uri.getScheme().equals(MARIOCASH_NODE_SCHEMA)) {
                throw new RuntimeException("expecting URL in the format ynode://PUBKEY@HOST:PORT");
            }
            this.id = Hex.decode(uri.getUserInfo());
            this.host = uri.getHost();
            this.port = uri.getPort();
            this.ynodeUri = ynodeUri;
        } catch (URISyntaxException e) {
            throw new RuntimeException("expecting URL in the format ynode://PUBKEY@HOST:PORT", e);
        }
    }

    public static Peer valueOf(String nodeId, String host, int port) {
        return new Peer(String.format(PEER_URI_FORMAT, MARIOCASH_NODE_SCHEMA,
                nodeId, host + ":" + port));
    }

    public static Peer valueOf(String addressOrYnode) {
        try {
            URI uri = new URI(addressOrYnode);
            if (uri.getScheme().equals(MARIOCASH_NODE_SCHEMA)) {
                return new Peer(addressOrYnode);
            }
        } catch (URISyntaxException e) {
            // continue
        }
        final String generatedNodeId = Hex.toHexString(new Account().getKey().getNodeId());
        return new Peer(String.format(PEER_URI_FORMAT, MARIOCASH_NODE_SCHEMA,
                generatedNodeId, addressOrYnode));
    }

    public String getYnodeUri() {
        return ynodeUri;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
