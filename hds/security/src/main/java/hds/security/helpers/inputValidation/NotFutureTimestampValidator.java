package hds.security.helpers.inputValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static hds.security.DateUtils.isFutureTimestamp;

public class NotFutureTimestampValidator implements ConstraintValidator<NotFutureTimestamp, Long> {
	@Override
	public void initialize(NotFutureTimestamp constraintAnnotation) { /* Nothing to do here. */ }

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		return !isFutureTimestamp(value);
	}
}
