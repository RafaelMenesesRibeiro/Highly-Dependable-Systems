package hds.security.helpers.inputValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static hds.security.DateUtils.isFreshTimestamp;

public class RelevantTimestampValidator implements ConstraintValidator<RelevantTimestamp, Long> {
	@Override
	public void initialize(RelevantTimestamp constraintAnnotation) { /* Nothing to do here. */ }

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		return isFreshTimestamp(value);
	}
}
