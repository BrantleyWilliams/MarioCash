package dev.zhihexireng.core.format;

import dev.zhihexireng.core.TransactionHeader;

import java.io.IOException;
 public interface TransactionFormat {
    public String getHashString() throws IOException;
    public byte[] getHash() throws IOException;
    public String getData();
    public TransactionHeader getHeader();
    public String toString();
}

