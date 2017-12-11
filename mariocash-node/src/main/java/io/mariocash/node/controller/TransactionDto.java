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

import com.google.gson.JsonObject;
import dev.zhihexireng.core.Account;
import dev.zhihexireng.core.Transaction;

import java.io.IOException;

public class TransactionDto {
    private String from;
    private String txHash;
    private String data;

    public static Transaction of(TransactionDto transactionDto) throws IOException {
        // TODO Account from 에서 가져와서 실제 Account로 변환합니다.
        Account account = new Account();
        JsonObject jsonData = new JsonObject();
        jsonData.addProperty("data", transactionDto.getData());
        return new Transaction(account, jsonData);
    }

    public static TransactionDto createBy(Transaction tx) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setFrom(tx.getFrom());
        transactionDto.setData(tx.getData());
        transactionDto.setTxHash(tx.getHashString());
        return transactionDto;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}
