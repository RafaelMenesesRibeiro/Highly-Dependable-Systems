package hds.security.helpers.inputValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidGoodIDValidator.class)
@Documented
public @interface ValidGoodID {
	String message() default "The GoodID does not exist.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
