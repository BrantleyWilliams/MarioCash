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

import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.BranchId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("branches")
public class StateController {

    private final BranchGroup branchGroup;

    @Autowired
    public StateController(BranchGroup branchGroup) {
        this.branchGroup = branchGroup;
    }

    @GetMapping("/active")
    public ResponseEntity getAll() {
        Map<String, Long> activeMap = new HashMap<>();
        branchGroup.getAllBranch().forEach(branch ->
                activeMap.put(branch.getBranchId().toString(), branch.getLastIndex()));
        return ResponseEntity.ok(activeMap);
    }

    @GetMapping("/{branchId}/states")
    public ResponseEntity getStates(@PathVariable(name = "branchId") String branchId) {
        List state = branchGroup.getStateStore(BranchId.of(branchId)).getStateList();
        return ResponseEntity.ok(state);
    }
}
