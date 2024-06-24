package account.exceptions;

public class PeriodDoesNotExistException extends RuntimeException {
    public PeriodDoesNotExistException() {
    }

    public PeriodDoesNotExistException(String message) {
        super(message);
    }

    public PeriodDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public PeriodDoesNotExistException(Throwable cause) {
        super(cause);
    }
}
