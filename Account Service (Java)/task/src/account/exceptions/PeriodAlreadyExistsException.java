package account.exceptions;

public class PeriodAlreadyExistsException extends RuntimeException {
    public PeriodAlreadyExistsException() {
    }

    public PeriodAlreadyExistsException(String message) {
        super(message);
    }

    public PeriodAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public PeriodAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public PeriodAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
