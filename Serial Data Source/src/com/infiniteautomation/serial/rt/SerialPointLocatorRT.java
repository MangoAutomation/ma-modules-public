package com.infiniteautomation.serial.rt;

import java.util.regex.Pattern;

import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class SerialPointLocatorRT extends PointLocatorRT{

	private SerialPointLocatorVO vo;
	private Pattern pattern;
	
	public SerialPointLocatorRT(SerialPointLocatorVO vo){
		this.vo = vo;
		pattern = Pattern.compile(vo.getValueRegex());
	}
	
	@Override
	public boolean isSettable() {
		return this.vo.isSettable();
	}

	public SerialPointLocatorVO getVo(){
		return this.vo;
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
}
