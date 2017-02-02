package com.infiniteautomation.serial.rt;

import java.util.regex.Pattern;

import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class SerialPointLocatorRT extends PointLocatorRT<SerialPointLocatorVO>{

	private Pattern pattern;
	
	public SerialPointLocatorRT(SerialPointLocatorVO vo){
		super(vo);
		pattern = Pattern.compile(vo.getValueRegex());
	}
	
	@Override
	public boolean isSettable() {
		return this.vo.isSettable();
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
}
