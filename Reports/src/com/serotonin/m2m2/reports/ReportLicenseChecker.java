package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.LicenseViolatedException;
import com.serotonin.m2m2.i18n.TranslatableMessage;

public class ReportLicenseChecker {
	private static final String GATEWAY = "Gateway";
	/**
	 * Checks if this is a core gateway
	 * 
	 * @throws LicenseViolatedException
	 */
	public static void checkLicense() {
		if(Common.license() != null && GATEWAY.equals(Common.license().getLicenseType()))
			throw new LicenseViolatedException(new TranslatableMessage("reports.license.notGateway"));
	}
}
