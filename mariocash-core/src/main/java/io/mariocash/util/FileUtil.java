package dev.zhihexireng.util;

import com.google.common.base.Strings;
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

        if (Strings.isNullOrEmpty(filePath) || Strings.isNullOrEmpty(fileName)) {
            throw new IOException("Invalid filepath or filename");
        }

        File file = new File(filePath, fileName);

        FileUtils.writeByteArrayToFile(file, data);

    }

    /**
     * Write file as byte[].
     *
     * @param file file
     * @param data data
     * @throws IOException IOException
     */
    public static void writeFile(File file, byte[] data) throws IOException {
        FileUtils.writeByteArrayToFile(file, data);
    }

    /**
     * Read file as byte[].
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

    /**
     * Get file name.
     *
     * @param filePathName file path + name
     * @return file name
     */
    public static String getFileName(String filePathName) {

        String[] splitName = filePathName.split(File.separator);

        if (splitName.length > 0) {
            return splitName[splitName.length - 1];
        } else {
            return null;
        }
    }

    /**
     * Get file path.
     *
     * @param filePathName file path + name
     * @return file path
     */
    public static String getFilePath(String filePathName) {

        String[] splitName = filePathName.split(File.separator);

        StringBuilder result = new StringBuilder();
        if (splitName.length > 0) {

            for (int i = 0; i < splitName.length - 1; i++) {
                result.append(splitName[i]).append(File.separator);
            }

            return result.toString();
        } else {
            return null;
        }
    }

}
