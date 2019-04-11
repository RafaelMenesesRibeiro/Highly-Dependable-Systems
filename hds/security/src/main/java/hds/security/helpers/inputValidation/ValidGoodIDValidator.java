package hds.security.helpers.inputValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidGoodIDValidator implements ConstraintValidator<ValidGoodID, String> {
	@Override
	public void initialize(ValidGoodID constraintAnnotation) {}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		value = inputValidation.cleanString(value);
		Pattern pattern = Pattern.compile("^good[0-9]+$");
		Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}
}
