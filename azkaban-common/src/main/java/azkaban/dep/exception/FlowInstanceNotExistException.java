package azkaban.dep.exception;

public class FlowInstanceNotExistException extends Exception {
    public FlowInstanceNotExistException() {
        super();
    }

    public FlowInstanceNotExistException(String message) {
        super(message);
    }

    public FlowInstanceNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowInstanceNotExistException(Throwable cause) {
        super(cause);
    }

    protected FlowInstanceNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
