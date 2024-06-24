package account.exceptions;

public class EmailAlreadyExistsException extends EndpointException {
    public EmailAlreadyExistsException(String endpoint) {
        super(endpoint);
    }

    public EmailAlreadyExistsException(String message, String endpoint) {
        super(message, endpoint);
    }

    public EmailAlreadyExistsException(String message, Throwable cause, String endpoint) {
        super(message, cause, endpoint);
    }

    public EmailAlreadyExistsException(Throwable cause, String endpoint) {
        super(cause, endpoint);
    }
}
