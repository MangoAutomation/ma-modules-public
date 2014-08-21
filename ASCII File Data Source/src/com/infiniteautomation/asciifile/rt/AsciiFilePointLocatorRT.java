package com.infiniteautomation.asciifile.rt;

import java.util.regex.Pattern;

import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

/**
 * @author Phillip Dunlap
 */

public class AsciiFilePointLocatorRT extends PointLocatorRT{

	private AsciiFilePointLocatorVO vo;
	private Pattern valuePattern;
	
	public AsciiFilePointLocatorRT(AsciiFilePointLocatorVO vo){
		this.vo = vo;
		valuePattern = Pattern.compile(vo.getValueRegex());
	}
	
	@Override
	public boolean isSettable() {
		return this.vo.isSettable();
	}

	public AsciiFilePointLocatorVO getVo(){
		return this.vo;
	}
	
	public Pattern getValuePattern() {
		return valuePattern;
	}
	
	public void setValuePattern(Pattern valuePattern) {
		this.valuePattern = valuePattern;
	}
}