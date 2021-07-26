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

import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.TransactionHusk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("branches/{branchId}/txs")
public class TransactionController {

    private final BranchGroup branchGroup;

    @Autowired
    public TransactionController(BranchGroup branchGroup) {
        this.branchGroup = branchGroup;
    }

    @PostMapping
    public ResponseEntity add(@PathVariable(name = "branchId") String branchId,
                              @RequestBody TransactionDto request) {
        TransactionHusk tx = TransactionDto.of(request);
        if (BranchId.of(branchId).equals(tx.getBranchId())) {
            TransactionHusk addedTx = branchGroup.addTransaction(tx);
            return ResponseEntity.ok(TransactionDto.createBy(addedTx));
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity get(@PathVariable(name = "branchId") String branchId,
                              @PathVariable String id) {
        TransactionHusk tx = branchGroup.getTxByHash(BranchId.of(branchId), id);

        if (tx == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(TransactionDto.createBy(tx));
    }

    @GetMapping
    public ResponseEntity getAll(@PathVariable(name = "branchId") String branchId) {
        long countOfTotal = branchGroup.countOfTxs(BranchId.of(branchId));
        List<TransactionHusk> txs =
                new ArrayList<>(branchGroup.getRecentTxs(BranchId.of(branchId)));
        List<TransactionDto> dtoList = txs.stream().sorted(Comparator.reverseOrder())
                .map(TransactionDto::createBy).collect(Collectors.toList());

        Map<String, Object> res = new HashMap<>();
        res.put("countOfTotal", countOfTotal);
        res.put("txs", dtoList);
        return ResponseEntity.ok(res);
    }
}
