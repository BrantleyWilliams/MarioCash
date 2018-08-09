package dev.zhihexireng.util;


import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FileUtilTest {

    @Test
    public void writeFileTest01() throws IOException {
        String filePath = "tmp/tmp";
        String fileName = "tmp.txt";
        byte[] data = "test data".getBytes();
        FileUtil.writeFile(filePath, fileName, data);

        assertArrayEquals(FileUtil.readFile(filePath, fileName), data);
    }

    @Test
    public void writeFileTest02() throws IOException {
        String filePath = "tmp/tmp02";
        String fileName = "tmp02.txt";
        byte[] data = "test data".getBytes();

        File file =  new File(filePath, fileName);

        FileUtil.writeFile(file, data);

        assertArrayEquals(FileUtil.readFile(filePath, fileName), data);
    }

    @Test
    public void getFileNameTest() {
        String testName = "tmp/tmp01/tmp011/tmp01.txt";

        assertEquals(FileUtil.getFileName(testName), "tmp01.txt");
    }

    @Test
    public void getFilePath() {
        String testPath = "tmp/tmp01/tmp011/tmp01.txt";

        assertEquals(FileUtil.getFilePath(testPath), "tmp/tmp01/tmp011/");
    }

}
