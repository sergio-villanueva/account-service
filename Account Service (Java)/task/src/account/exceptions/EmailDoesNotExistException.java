package account.exceptions;

public class EmailDoesNotExistException extends RuntimeException {
    public EmailDoesNotExistException() {
        super("email does not exist");
    }

    public EmailDoesNotExistException(String message) {
        super(message);
    }
}
