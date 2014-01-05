package com.infiniteautomation.datafilesource.rt;

import com.infiniteautomation.datafilesource.vo.DataFilePointLocatorVO;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class DataFilePointLocatorRT extends PointLocatorRT {
	private DataFilePointLocatorVO vo;
	
	public DataFilePointLocatorRT(DataFilePointLocatorVO vo) {
		this.vo = vo;
	}
	
	@Override
	public boolean isSettable() {
		return this.vo.isSettable();
	}
	
	public DataFilePointLocatorVO getVo() {
		return vo;
	}

}
