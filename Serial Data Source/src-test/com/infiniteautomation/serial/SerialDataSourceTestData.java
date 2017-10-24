/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.infiniteautomation.serial.vo.SerialPointLocatorVO;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

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
	public static DataPointRT getMatchAllPoint(DataSourceVO<?> ds) {
		DataPointVO vo = new DataPointVO();
		vo.setName("matchAll");
		vo.setXid("matchAll");
		vo.setId(currentId++);
		vo.setUnitString("");
		SerialPointLocatorVO plVo = new SerialPointLocatorVO();
		plVo.setDataTypeId(DataTypes.ALPHANUMERIC);
		plVo.setValueRegex(PATTERNS.get(vo.getName()));
		plVo.setValueIndex(2);
		plVo.setPointIdentifier("");
		vo.setPointLocator(plVo);
		return new DataPointRT(vo, plVo.createRuntime(), ds, null, null);
	}
	public static DataPointRT getNewlineTerminated(DataSourceVO<?> ds) {
		DataPointVO vo = new DataPointVO();
		vo.setName("newlineTerminated");
		vo.setXid("newlineTerminated");
		vo.setId(currentId++);
		SerialPointLocatorVO plVo = new SerialPointLocatorVO();
		plVo.setDataTypeId(DataTypes.ALPHANUMERIC);
		plVo.setValueRegex(PATTERNS.get(vo.getName()));
		plVo.setValueIndex(2);
		plVo.setPointIdentifier("");
		vo.setPointLocator(plVo);
		return new DataPointRT(vo, plVo.createRuntime(), ds, null, null);
	}
	public static DataPointRT getCustomPoint(String name, String xid, String valueRegex, int valueIndex, String pointIdentifier, DataSourceVO<?> ds) {
		DataPointVO vo = new DataPointVO();
		vo.setName(name);
		vo.setXid(xid);
		vo.setId(currentId++);
		SerialPointLocatorVO plVo = new SerialPointLocatorVO();
		plVo.setDataTypeId(DataTypes.ALPHANUMERIC);
		plVo.setValueRegex(valueRegex);
		plVo.setValueIndex(valueIndex);
		plVo.setPointIdentifier(pointIdentifier);
		vo.setPointLocator(plVo);
		return new DataPointRT(vo, plVo.createRuntime(), ds, null, null);
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
