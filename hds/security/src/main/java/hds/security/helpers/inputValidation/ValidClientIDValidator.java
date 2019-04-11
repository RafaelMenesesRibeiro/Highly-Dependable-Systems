package hds.security.helpers.inputValidation;

import hds.security.ResourceManager;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidClientIDValidator implements ConstraintValidator<ValidClientID, String> {
	private int minValue = -100;
	private int maxValue = -101;

	@Override
	public void initialize(ValidClientID constraintAnnotation) {
		this.minValue = ResourceManager.getServerPort() + 1;
		this.maxValue = ResourceManager.getMaxClientId();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		int stringValue = 0;
		try {
			stringValue = Integer.parseInt(value);
		}
		catch (NumberFormatException nfex) {
			return false;
		}
		return (stringValue >= this.minValue && stringValue <= maxValue);

		// TODO - Move here code in isVaklidClientID. //
	}
}
