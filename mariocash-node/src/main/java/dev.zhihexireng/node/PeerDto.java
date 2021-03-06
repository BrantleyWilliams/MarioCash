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

import dev.zhihexireng.core.net.Peer;

class PeerDto {
    private String host;
    private int port;

    PeerDto() {}

    PeerDto(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static Peer of(PeerDto peerDto) {
        return new Peer(peerDto.host, peerDto.port);
    }

    public static PeerDto createdBy(Peer peer) {
        return new PeerDto(peer.getHost(), peer.getPort());
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
