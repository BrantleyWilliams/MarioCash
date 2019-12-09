/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.node.controller;

import dev.zhihexireng.core.NodeManager;
import dev.zhihexireng.core.husk.TransactionHusk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("txs")
public class TransactionController {

    private final NodeManager nodeManager;

    @Autowired
    public TransactionController(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    @PostMapping
    public ResponseEntity add(@RequestBody TransactionDto request) {
        TransactionHusk tx = TransactionDto.of(request);
        TransactionHusk addedTx = nodeManager.addTransaction(tx);
        return ResponseEntity.ok(TransactionDto.createBy(addedTx));
    }

    @GetMapping("{id}")
    public ResponseEntity get(@PathVariable String id) {
        TransactionHusk tx = nodeManager.getTxByHash(id);

        if (tx == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(TransactionDto.createBy(tx));
    }
}
