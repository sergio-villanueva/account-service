package account.exceptions;

public class ModifyAdminAccessException extends RuntimeException {
    public ModifyAdminAccessException() {
    }

    public ModifyAdminAccessException(String message) {
        super(message);
    }

    public ModifyAdminAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModifyAdminAccessException(Throwable cause) {
        super(cause);
    }
}
