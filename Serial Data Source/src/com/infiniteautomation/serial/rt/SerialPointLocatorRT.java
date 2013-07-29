package com.infiniteautomation.serial.rt;

import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class SerialPointLocatorRT extends PointLocatorRT{

	private SerialPointLocatorVO vo;
	
	public SerialPointLocatorRT(SerialPointLocatorVO vo){
		this.vo = vo;
	}
	
	@Override
	public boolean isSettable() {
		return this.vo.isSettable();
	}

}
