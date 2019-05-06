package dev.zhihexireng.node.exception;

public class RejectedAccessException extends NoClassDefFoundError {
    public static final int code = -10003;
    public static final String msg = "Rejected";

    public RejectedAccessException() {
        super(msg);
    }
}
