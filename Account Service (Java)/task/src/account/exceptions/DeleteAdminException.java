package account.exceptions;

public class DeleteAdminException extends RuntimeException {
    public DeleteAdminException() {
    }

    public DeleteAdminException(String message) {
        super(message);
    }

    public DeleteAdminException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeleteAdminException(Throwable cause) {
        super(cause);
    }
}
