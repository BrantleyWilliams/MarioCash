package dev.zhihexireng.util;

public class TimeUtils {

    public static long getCurrenttime() {
        return System.currentTimeMillis();
    }

    public static long time() {
        return System.nanoTime();
    }
}
