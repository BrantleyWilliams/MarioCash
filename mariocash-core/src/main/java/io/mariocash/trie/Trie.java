package dev.zhihexireng.trie;

import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.Transactions;
import dev.zhihexireng.util.HashUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Trie {

    public static byte[] getMerkleRoot(List<Transaction> txs) {

        ArrayList<byte[]> tree = new ArrayList<>();

        for (Transaction tx : txs) {
            tree.add(tx.getHash());
        }

        int levelOffset = 0;
        for (int levelSize = txs.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {

            for (int left = 0; left < levelSize; left += 2) {
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = reverseBytes(tree.get(levelOffset + left));
                byte[] rightBytes = reverseBytes(tree.get(levelOffset + right));
                tree.add(reverseBytes(hashTwice(leftBytes, 0, 32, rightBytes, 0, 32)));
            }
            levelOffset += levelSize;
        }

        return HashUtils.sha256(tree.get(tree.size()-1));
    }

    private static byte[] reverseBytes(byte[] bytes) {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = bytes[bytes.length - 1 - i];
        return buf;
    }

    private static byte[] hashTwice(byte[] input1, int offset1, int length1,
                                   byte[] input2, int offset2, int length2) {
        MessageDigest digest = newDigest();
        digest.update(input1, offset1, length1);
        digest.update(input2, offset2, length2);
        return digest.digest(digest.digest());
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
