package account.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EndpointException extends RuntimeException {
    private final String endpoint;

    public EndpointException(String endpoint) {
        this.endpoint = endpoint;
    }

    public EndpointException(String message, String endpoint) {
        super(message);
        this.endpoint = endpoint;
    }

    public EndpointException(String message, Throwable cause, String endpoint) {
        super(message, cause);
        this.endpoint = endpoint;
    }

    public EndpointException(Throwable cause, String endpoint) {
        super(cause);
        this.endpoint = endpoint;
    }
}
