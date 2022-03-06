package toyproject.runningmate.exception;

public class ExpiredTokenException extends IllegalStateException{

    public ExpiredTokenException() {
        super();
    }

    public ExpiredTokenException(String s) {
        super(s);
    }

    public ExpiredTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExpiredTokenException(Throwable cause) {
        super(cause);
    }
}