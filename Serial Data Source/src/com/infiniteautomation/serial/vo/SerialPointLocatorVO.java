package com.infiniteautomation.serial.vo;

import java.util.List;

import com.infiniteautomation.serial.rt.SerialPointLocatorRT;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;

public class SerialPointLocatorVO extends AbstractPointLocatorVO{
    
	@Override
	public int getDataTypeId() {
		return DataTypes.ALPHANUMERIC; //Always for returned string from terminal
	}

	@Override
	public TranslatableMessage getConfigurationDescription() {
		//TODO add the properties to this
		return new TranslatableMessage("serial.point.configuration");
	}

	@Override
	public boolean isSettable() {
		return true;
	}

	@Override
	public PointLocatorRT createRuntime() {
		return new SerialPointLocatorRT(this);
	}

	@Override
	public void validate(ProcessResult response) {
		//TODO add validation
		
	}

	@Override
	public void addProperties(List<TranslatableMessage> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPropertyChanges(List<TranslatableMessage> list, Object o) {
		// TODO Auto-generated method stub
		
	}

}
