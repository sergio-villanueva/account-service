package account.exceptions;

public class GrantBusinessRoleException extends RuntimeException {
    public GrantBusinessRoleException() {
    }

    public GrantBusinessRoleException(String message) {
        super(message);
    }

    public GrantBusinessRoleException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrantBusinessRoleException(Throwable cause) {
        super(cause);
    }
}
