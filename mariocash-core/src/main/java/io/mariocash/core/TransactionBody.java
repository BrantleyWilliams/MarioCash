package dev.zhihexireng.core;

import com.google.gson.JsonArray;
import dev.zhihexireng.crypto.HashUtil;
import org.spongycastle.util.encoders.Hex;

public class TransactionBody {

    private JsonArray body;

    public TransactionBody(JsonArray body) {
        this.body = body;
    }

    public long length() {
        return this.body.toString().length();
    }

    public String getHexString() {
        return Hex.toHexString(this.body.toString().getBytes());
    }

    public byte[] getBinary() {
        return this.body.toString().getBytes();
    }

    public JsonArray getBody() {
        return this.body;
    }

    public byte[] getBodyHash() {
        return HashUtil.sha3(this.getBinary());
    }

}
