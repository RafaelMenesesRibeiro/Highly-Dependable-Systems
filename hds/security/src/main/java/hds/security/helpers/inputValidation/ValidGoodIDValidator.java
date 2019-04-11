package hds.security.helpers.inputValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidGoodIDValidator implements ConstraintValidator<ValidGoodID, String> {
	@Override
	public void initialize(ValidGoodID constraintAnnotation) {}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// TODO - Move here code in isValidGoodID. //
		return true;
	}
}
