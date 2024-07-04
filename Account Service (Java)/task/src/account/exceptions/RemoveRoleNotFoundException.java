package account.exceptions;

public class RemoveRoleNotFoundException extends RuntimeException {
    public RemoveRoleNotFoundException() {
    }

    public RemoveRoleNotFoundException(String message) {
        super(message);
    }

    public RemoveRoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoveRoleNotFoundException(Throwable cause) {
        super(cause);
    }
}
