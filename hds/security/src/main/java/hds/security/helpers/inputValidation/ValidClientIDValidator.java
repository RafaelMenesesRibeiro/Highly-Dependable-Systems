package hds.security.helpers.inputValidation;

import hds.security.ResourceManager;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class ValidClientIDValidator implements ConstraintValidator<ValidClientID, String> {
	@Override
	public void initialize(ValidClientID constraintAnnotation) { /* Nothing to do. */ }

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return isValid(value);
	}

	public static boolean isValid(String value) {
		value = inputValidation.cleanString(value);

		Pattern pattern = Pattern.compile("^[0-9]+$");
		Matcher matcher = pattern.matcher(value);
		if (!matcher.matches()) {
			return false;
		}
		int stringValue = 0;
		try {
			stringValue = Integer.parseInt(value);
		}
		catch (NumberFormatException nfex) {
			return false;
		}
		int minValue = ResourceManager.getMinClientId();
		int maxValue = ResourceManager.getMaxClientId();
		return (stringValue >= minValue && stringValue <= maxValue);
	}
}
