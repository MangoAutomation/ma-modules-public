/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;

/**
 * @author Terry Packer
 *
 */
public class SerialDataSourceTestData {
	
	
	
	public static SerialDataSourceVO getStandardDataSourceVO(){
		SerialDataSourceVO vo = new SerialDataSourceVO();
		vo.setId(1);
		vo.setXid("serial-xid");
		
		return vo;
	}
	

}
