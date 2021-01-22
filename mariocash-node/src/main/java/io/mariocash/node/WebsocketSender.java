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

package dev.zhihexireng.node;

import dev.zhihexireng.core.BlockChain;
import dev.zhihexireng.core.BlockHusk;
import dev.zhihexireng.core.Branch;
import dev.zhihexireng.core.BranchGroup;
import dev.zhihexireng.core.BranchId;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.event.BranchEventListener;
import dev.zhihexireng.core.event.BranchGroupEventListener;
import dev.zhihexireng.node.controller.BlockDto;
import dev.zhihexireng.node.controller.TransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebsocketSender implements BranchEventListener, BranchGroupEventListener {

    private final SimpMessagingTemplate template;

    @Autowired
    public WebsocketSender(BranchGroup branchGroup, SimpMessagingTemplate template) {
        branchGroup.setListener(this);
        branchGroup.getAllBranch().forEach(branch -> branch.addListener(this));
        this.template = template;
    }

    @Override
    public void chainedBlock(BlockHusk block) {
        String branchId = block.getBranchId().toString();
        template.convertAndSend("/topic/blocks", BlockDto.createBy(block));
        template.convertAndSend("/topic/branches/" + branchId + "/blocks",
                BlockDto.createBy(block));
        if (block.getBranchId().equals(BranchId.stem())) {
            template.convertAndSend("/topic/stem/blocks", BlockDto.createBy(block));
        }
    }

    @Override
    public void receivedTransaction(TransactionHusk tx) {
        String branchId = tx.getBranchId().toString();
        template.convertAndSend("/topic/txs", TransactionDto.createBy(tx));
        template.convertAndSend("/topic/branches/" + branchId + "/txs",
                TransactionDto.createBy(tx));
        if (tx.getBranchId().equals(BranchId.stem())) {
            template.convertAndSend("/topic/stem/txs", TransactionDto.createBy(tx));
        }
    }

    @Override
    public void newBranch(BlockChain blockChain) {
        if (Branch.YEED.equals(blockChain.getBranchName())) {
            blockChain.addListener(this);
        }
    }
}
