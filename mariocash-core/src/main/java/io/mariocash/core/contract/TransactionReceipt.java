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

package dev.zhihexireng.core.contract;

import java.util.HashMap;
import java.util.Map;

public class TransactionReceipt {
    public static final int SUCCESS = 1;

    private String transactionHash =
            "0xb903239f8543d04b5dc1ba6579132b143087c68db1b2168786408fcbce568238";
    private final int transactionIndex = 1;
    private final String blockHash =
            "0xc6ef2fc5426d6ad6fd9e2a26abeab0aa2411b7ab17f30a99d3cb96aed1d1055b";
    private final int yeedUsed = 30000;
    private final String branchAddress =
            "0xb60e8dd61c5d32be8058bb8eb970870f07233155";
    private final Map<String, Object> txLog = new HashMap<>();
    private int status = 0;

    public void putLog(String key, Object value) {
        txLog.put(key, value);
    }

    public Object getLog(String key) {
        return txLog.get(key);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public int getYeedUsed() {
        return yeedUsed;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public Map<String, Object> getTxLog() {
        return txLog;
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == SUCCESS;
    }

    @Override
    public String toString() {
        return "TransactionReceipt{"
                + "transactionHash='" + transactionHash + '\''
                + ", transactionIndex=" + transactionIndex
                + ", blockHash='" + blockHash + '\''
                + ", yeedUsed=" + yeedUsed
                + ", branchAddress='" + branchAddress + '\''
                + ", txLog=" + txLog
                + ", status=" + status
                + '}';
    }
}
