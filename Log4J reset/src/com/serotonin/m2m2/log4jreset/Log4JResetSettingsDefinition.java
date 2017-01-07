package com.serotonin.m2m2.log4jreset;

import java.util.Map;

import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.SystemSettingsDefinition;

public class Log4JResetSettingsDefinition extends SystemSettingsDefinition {
    @Override
    public String getDescriptionKey() {
        return "log4JReset.settings.header";
    }

    @Override
    public String getSectionJspPath() {
        return "web/settings.jspf";
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#getDefaultValues()
	 */
	@Override
	public Map<String, Object> getDefaultValues() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#convertToValueFromCode(java.lang.String, java.lang.String)
	 */
	@Override
	public Integer convertToValueFromCode(String key, String code) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#convertToCodeFromValue(java.lang.String, java.lang.Integer)
	 */
	@Override
	public String convertToCodeFromValue(String key, Integer value) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#validateSettings(java.util.Map, com.serotonin.m2m2.i18n.ProcessResult)
	 */
	@Override
	public void validateSettings(Map<String, Object> settings, ProcessResult response) {
	}
}
