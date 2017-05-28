package dev.zhihexireng.core;

import org.junit.Test;

public class HashUtilsTests {
    @Test
    public void test() {
        String test = HashUtils.hashString("test");
        System.out.println(test);
    }
}
