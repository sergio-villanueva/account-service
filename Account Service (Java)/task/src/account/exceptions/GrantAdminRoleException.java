package account.exceptions;

public class GrantAdminRoleException extends RuntimeException {
    public GrantAdminRoleException() {
    }

    public GrantAdminRoleException(String message) {
        super(message);
    }

    public GrantAdminRoleException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrantAdminRoleException(Throwable cause) {
        super(cause);
    }
}
