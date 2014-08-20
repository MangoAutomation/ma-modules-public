/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.infiniteautomation.serial.rt.SerialPointLocatorRT;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class SerialDataSourceTestData {
	private static int currentId = 1;
	public static Map<String,String> PATTERNS = new HashMap<String,String>();
	public static List<DataPointRT> DPRTS = new ArrayList<DataPointRT>();
	static {
		PATTERNS.put("matchAll", "()(.*)"); //Value index 2
		PATTERNS.put("newlineTerminated", "()([^\n]*)\n");
		PATTERNS.put("matchHexBytes", "[0-9A-Fa-f]{2})+");
	}
	
	// ========== POINT CREATION METHODS ===========
	public static DataPointRT getMatchAllPoint() {
		DataPointVO vo = new DataPointVO();
		vo.setName("matchAll");
		vo.setXid("serialTest_matchAll");
		vo.setId(currentId++);
		vo.setUnitString("");
		SerialPointLocatorVO plVo = new SerialPointLocatorVO();
		plVo.setDataTypeId(DataTypes.ALPHANUMERIC);
		plVo.setValueRegex(PATTERNS.get(vo.getName()));
		plVo.setValueIndex(2);
		plVo.setPointIdentifier("");
		vo.setPointLocator(plVo);
		return new DataPointRT(vo, plVo.createRuntime());
	}
	public static DataPointRT getNewlineTerminated() {
		DataPointVO vo = new DataPointVO();
		vo.setName("newlineTerminated");
		vo.setXid("serialTest_newlineTerminated");
		vo.setId(currentId++);
		SerialPointLocatorVO plVo = new SerialPointLocatorVO();
		plVo.setDataTypeId(DataTypes.ALPHANUMERIC);
		plVo.setValueRegex(PATTERNS.get(vo.getName()));
		plVo.setValueIndex(2);
		plVo.setPointIdentifier("");
		vo.setPointLocator(plVo);
		return new DataPointRT(vo, plVo.createRuntime());
	}
	// ============ END POINT CREATION SECTION =========
	
//	public static List<DataPointRT> getTwoPoints() {
//		DataPointVO vo = new DataPointVO();
//		vo.setName("cheese");
//		vo.setXid("xid");
//	}
	
	public static SerialDataSourceVO getStandardDataSourceVO(){
		SerialDataSourceVO vo = new SerialDataSourceVO();
		vo.setId(1);
		vo.setXid("serial-xid");
		vo.setMessageRegex("().*[\\r\\n]?"); //TODO
		vo.setPointIdentifierIndex(1);
		vo.setUseTerminator(true);
		vo.setMessageTerminator(";");
		vo.setReadTimeout(1);
//		vo.setMaxMessageSize(400);
		vo.setCommPortId("testFakeCommPort");
		return vo;
	}
	
	//TODO RT full of existing points
		//reusable points
	

}
