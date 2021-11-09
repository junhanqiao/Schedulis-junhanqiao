package azkaban.dep.exception;

public class FollowingFlowNotFinishException extends Exception {
    public FollowingFlowNotFinishException() {
    }

    public FollowingFlowNotFinishException(String message) {
        super(message);
    }

    public FollowingFlowNotFinishException(String message, Throwable cause) {
        super(message, cause);
    }

    public FollowingFlowNotFinishException(Throwable cause) {
        super(cause);
    }

    public FollowingFlowNotFinishException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
