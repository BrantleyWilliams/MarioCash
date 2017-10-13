package dev.zhihexireng.util;

public class SerializeUtils {

    public static byte[] serialize(Object obj) {

        return obj.toString().getBytes();
    }


}
