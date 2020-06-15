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

package dev.zhihexireng.core;

import com.google.gson.JsonObject;
import dev.zhihexireng.common.Sha3Hash;
import dev.zhihexireng.core.exception.NotValidateException;
import dev.zhihexireng.crypto.ECKey;
import dev.zhihexireng.proto.Proto;
import dev.zhihexireng.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Objects;

public class TransactionHusk implements ProtoHusk<Proto.Transaction>, Comparable<TransactionHusk> {

    private static final Logger log = LoggerFactory.getLogger(TransactionHusk.class);

    private Proto.Transaction protoTransaction;
    private Transaction coreTransaction;

    public TransactionHusk(Proto.Transaction transaction) {
        this.protoTransaction = transaction;
        try {
            this.coreTransaction = Transaction.toTransaction(transaction);
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public TransactionHusk(Transaction transaction) {
        this.coreTransaction = transaction;
        try {
            this.protoTransaction = Transaction.toProtoTransaction(transaction);
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public TransactionHusk(byte[] data){
        try {
            this.protoTransaction = Proto.Transaction.parseFrom(data);
            this.coreTransaction = Transaction.toTransaction(protoTransaction);
        } catch (Exception e) {
            throw new NotValidateException();
        }
    }

    public TransactionHusk(JsonObject jsonObject) {
        try {
            this.coreTransaction = new Transaction(jsonObject);
            this.protoTransaction = this.coreTransaction.toProtoTransaction();
        } catch (SignatureException e) {
            throw new NotValidateException();
        }
    }

//    private Proto.Transaction.Header getHeader() {
//        return this.protoTransaction.getHeader();
//    }

    public Proto.Transaction getProtoTransaction() {
        return protoTransaction;
    }

    public Transaction getCoreTransaction() {
        return coreTransaction;
    }

    public byte[] getSignature() {
        return this.protoTransaction.getSignature().toByteArray();
    }

    public String getBody() {
        return this.protoTransaction.getBody().toString();
    }

    public TransactionHusk sign(Wallet wallet) {

        try {
            this.coreTransaction =
                    new Transaction(this.coreTransaction.getHeader(), wallet, this.coreTransaction.getBody());
            this.protoTransaction = this.coreTransaction.toProtoTransaction();
        } catch (Exception e) {
            throw new NotValidateException();
        }

        return this;
    }

    public Sha3Hash getHash() {
        try {
            return new Sha3Hash(this.coreTransaction.getHash(), true);
        } catch (IOException e) {
            throw new NotValidateException();
        }
    }

    @Override
    public byte[] getData() {
        return this.protoTransaction.toByteArray();
    }

    @Override
    public Proto.Transaction getInstance() {
        return this.protoTransaction;
    }

    @Override
    public String toString() {
        return this.coreTransaction.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionHusk that = (TransactionHusk) o;
        return Objects.equals(protoTransaction, that.protoTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protoTransaction);
    }

    public boolean isSigned() {
        return !this.protoTransaction.getSignature().isEmpty();
    }

    public boolean verify() {
        return this.coreTransaction.getSignature().verify();
    }

    /**
     * Get the address.
     *
     * @return address
     */
    public Address getAddress() {
        return new Address(this.coreTransaction.getAddress());
    }

    /**
     * Get ECKey(include pubKey) using sig & signData.
     *
     * @return ECKey(include pubKey)
     */
    private ECKey ecKey() {
        return this.coreTransaction.getSignature().getEcKeyPub();
    }

    public JsonObject toJsonObject() {
        return this.coreTransaction.toJsonObject();
    }

    @Override
    public int compareTo(TransactionHusk o) {
        return Long.compare(ByteUtil.byteArrayToLong(protoTransaction.getHeader().getTimestamp().toByteArray()),
                ByteUtil.byteArrayToLong(o.getInstance().getHeader().getTimestamp().toByteArray()));
    }


}
