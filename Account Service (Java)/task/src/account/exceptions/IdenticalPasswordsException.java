package account.exceptions;

public class IdenticalPasswordsException extends RuntimeException {
    public IdenticalPasswordsException(String message) {
        super(message);
    }

    public IdenticalPasswordsException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdenticalPasswordsException(Throwable cause) {
        super(cause);
    }
}
