package account.exceptions;

public class RemoveAdminException extends RuntimeException {
    public RemoveAdminException() {
    }

    public RemoveAdminException(String message) {
        super(message);
    }

    public RemoveAdminException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoveAdminException(Throwable cause) {
        super(cause);
    }
}
