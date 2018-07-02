package dev.zhihexireng.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * File Utility
 * extends org.apache.commons.io.FileUtils.
 */
public class FileUtil extends org.apache.commons.io.FileUtils {

    /**
     * write file as byte[].
     *
     * @param filePath file path
     * @param fileName file name
     * @param data     data
     * @throws IOException IOException
     */
    public static void writeFile(String filePath, String fileName, byte[] data)
            throws IOException {

        //todo: check exception, return boolean, check file permission

        File file = new File(filePath, fileName);

        FileUtils.writeByteArrayToFile(file, data);

    }

    /**
     * write file as byte[].
     *
     * @param file file
     * @param data data
     * @throws IOException IOException
     */
    public static void writeFile(File file, byte[] data) throws IOException {
        FileUtils.writeByteArrayToFile(file, data);
    }

    /**
     * read file as byte[].
     *
     * @param filePath file path
     * @param fileName file name
     * @return data
     * @throws IOException IOException
     */
    public static byte[] readFile(String filePath, String fileName) throws IOException {

        File file = FileUtils.getFile(filePath + "/" + fileName);

        return FileUtils.readFileToByteArray(file);
    }


}
