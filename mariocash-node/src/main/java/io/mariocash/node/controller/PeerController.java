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

package dev.zhihexireng.node.controller;

import dev.zhihexireng.core.net.Peer;
import dev.zhihexireng.core.net.PeerChannelGroup;
import dev.zhihexireng.core.net.PeerGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("peers")
class PeerController {

    private final PeerGroup peerGroup;
    private final PeerChannelGroup peerChannelGroup;

    @Autowired
    public PeerController(PeerGroup peerGroup, PeerChannelGroup peerChannelGroup) {
        this.peerGroup = peerGroup;
        this.peerChannelGroup = peerChannelGroup;
    }

    @PostMapping
    public ResponseEntity add(@RequestBody PeerDto peerDto) {
        Peer peer = PeerDto.of(peerDto);
        peerGroup.addPeer(peer);
        return ResponseEntity.ok(peerDto);
    }

    @GetMapping
    public ResponseEntity getAll() {
        return ResponseEntity.ok(peerGroup.getPeers());
    }

    @GetMapping("/active")
    public ResponseEntity getAllActivePeer() {
        return ResponseEntity.ok(peerChannelGroup.getActivePeerList());
    }
}
