package dev.zhihexireng.core.exception;

public class DuplicatedException extends RuntimeException {

    public DuplicatedException() {
        super();
    }

    public DuplicatedException(String s) {
        super(s);
    }

    public DuplicatedException(Throwable cause) {
        super(cause);
    }
}
