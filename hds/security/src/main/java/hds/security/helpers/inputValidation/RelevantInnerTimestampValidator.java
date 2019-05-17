package hds.security.helpers.inputValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static hds.security.DateUtils.*;

public class RelevantInnerTimestampValidator implements ConstraintValidator<RelevantInnerTimestamp, Long> {
	@Override
	public void initialize(RelevantInnerTimestamp constraintAnnotation) { /* Nothing to do here. */ }

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		return isFreshTimestamp(value, getInnerPastTolerance(), getFutureTolerance());
	}
}
