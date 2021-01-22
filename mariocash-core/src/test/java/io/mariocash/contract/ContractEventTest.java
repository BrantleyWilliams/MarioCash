/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.contract;

import dev.zhihexireng.TestUtils;
import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionReceipt;
import org.junit.Test;

public class ContractEventTest {

    @Test
    public void getTransactionReceipt() {
        TransactionHusk tx = TestUtils.createTransferTxHusk();
        TransactionReceipt txReceipt = new TransactionReceipt();
        ContractEvent event = ContractEvent.of(txReceipt, tx);
        assert event.getTransactionReceipt().getStatus() == txReceipt.getStatus();
        assert event.getTransactionHusk().equals(tx);
    }
}
