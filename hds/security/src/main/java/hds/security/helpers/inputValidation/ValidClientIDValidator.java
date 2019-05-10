package hds.security.helpers.inputValidation;

import hds.security.ResourceManager;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidClientIDValidator implements ConstraintValidator<ValidClientID, String> {
	private int minValue = -100;
	private int maxValue = -101;

	@Override
	public void initialize(ValidClientID constraintAnnotation) {
		this.minValue = ResourceManager.getMinClientId();
		this.maxValue = ResourceManager.getMaxClientId();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
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
		return (stringValue >= this.minValue && stringValue <= maxValue);
	}
}
