package org.devocative.demeter.web.component;

import org.devocative.adroit.ConfigUtil;
import org.devocative.demeter.DemeterConfigKey;
import org.devocative.wickomp.form.validator.WPasswordStrengthValidator;

public class DPasswordStrengthValidator extends WPasswordStrengthValidator {
	private static final long serialVersionUID = 5955082329447306795L;

	@Override
	public boolean isEnabled() {
		return ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrength);
	}

	@Override
	public boolean isDigit() {
		return ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthDigit);
	}

	@Override
	public boolean isLowerCase() {
		return ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthLowerCase);
	}

	@Override
	public boolean isUpperCase() {
		return ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthUpperCase);
	}

	@Override
	public boolean isSpecialChar() {
		return ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthSpecialChar);
	}

	@Override
	public boolean isNoWhiteSpace() {
		return ConfigUtil.getBoolean(DemeterConfigKey.WebPasswordStrengthNoWhiteSpace);
	}

	@Override
	public int getMinLength() {
		return ConfigUtil.getInteger(DemeterConfigKey.WebPasswordStrengthMinLength);
	}

	@Override
	public Integer getMaxLength() {
		return ConfigUtil.getInteger(DemeterConfigKey.WebPasswordStrengthMaxLength);
	}
}
