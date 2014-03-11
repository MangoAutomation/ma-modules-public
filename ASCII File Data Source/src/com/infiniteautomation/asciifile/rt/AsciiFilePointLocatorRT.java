package com.infiniteautomation.asciifile.rt;

import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

/**
 * @author Phillip Dunlap
 */

public class AsciiFilePointLocatorRT extends PointLocatorRT{

	private AsciiFilePointLocatorVO vo;
	
	public AsciiFilePointLocatorRT(AsciiFilePointLocatorVO vo){
		this.vo = vo;
	}
	
	@Override
	public boolean isSettable() {
		return this.vo.isSettable();
	}

	public AsciiFilePointLocatorVO getVo(){
		return this.vo;
	}
	
}