package azkaban.dep.exception;

public class CycleDepRelationException extends Exception {
    public CycleDepRelationException() {
        super();
    }

    public CycleDepRelationException(String message) {
        super(message);
    }

    public CycleDepRelationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CycleDepRelationException(Throwable cause) {
        super(cause);
    }

    protected CycleDepRelationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
