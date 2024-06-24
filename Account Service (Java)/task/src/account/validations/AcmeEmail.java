package account.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Validates whether the email is a valid corporate acme email address
 * */

@Documented
@Constraint(validatedBy = {})
@Email(message=ValidationMessages.INVALID_ACME_EMAIL)
@Pattern(regexp = "^[\\w.+\\-]+@acme\\.com$", message = ValidationMessages.INVALID_ACME_EMAIL)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(AcmeEmail.List.class)
public @interface AcmeEmail {
    String message() default ValidationMessages.INVALID_ACME_EMAIL;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        AcmeEmail[] value();
    }
}
