package com.serotonin.m2m2.pointLinks;

import com.serotonin.m2m2.rt.script.JsonImportExclusion;

public class PointLinkJsonImportExclusion extends JsonImportExclusion {

	public PointLinkJsonImportExclusion(String key, String value) {
		super(key, value);
	}

	@Override
	public String getImporterType() {
		return PointLinkEmportDefinition.POINT_LINKS;
	}
}
