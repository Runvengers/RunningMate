package toyproject.runningmate.exception;

public class UserNullException extends RuntimeException {

    public UserNullException() {
        super();
    }

    public UserNullException(String message) {
        super(message);
    }
    public UserNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNullException(Throwable cause) {
        super(cause);
    }

    protected UserNullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
