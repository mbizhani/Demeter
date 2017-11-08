package org.devocative.demeter.web.component;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.wickomp.form.validator.WPasswordStrengthValidator;

public class DPasswordStrengthValidator extends WPasswordStrengthValidator {
	private static final long serialVersionUID = 5955082329447306795L;

	public DPasswordStrengthValidator() {
		hasDigit(ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthDigit));
		hasLowerCase(ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthLowerCase));
		hasUpperCase(ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthUpperCase));
		hasSpecialChar(ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthSpecialChar));
		hasNoWhiteSpace(ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthNoWhiteSpace));
		setMinLength(ConfigUtil.getInteger(DemeterConfigKey.WebPasswordStrengthMinLength));
		setMaxLength(ConfigUtil.getInteger(DemeterConfigKey.WebPasswordStrengthMaxLength));
	}
}
