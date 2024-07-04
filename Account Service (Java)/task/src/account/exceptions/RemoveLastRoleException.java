package account.exceptions;

public class RemoveLastRoleException extends RuntimeException {
    public RemoveLastRoleException() {
    }

    public RemoveLastRoleException(String message) {
        super(message);
    }

    public RemoveLastRoleException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoveLastRoleException(Throwable cause) {
        super(cause);
    }
}
