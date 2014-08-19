/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial.rt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.infiniteautomation.serial.SerialDataSourceTestData;
import com.infiniteautomation.serial.TestSerialPortInputStream;
import com.infiniteautomation.serial.TestSerialPortOutputStream;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.serotonin.io.serial.SerialParameters;
import com.serotonin.io.serial.SerialPortException;
import com.serotonin.io.serial.SerialPortProxy;
import com.serotonin.io.serial.SerialPortProxyEvent;
import com.serotonin.io.serial.SerialUtils;

/**
 * @author Terry Packer
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SerialUtils.class})
public class SerialDataSourceTest {

	@Mock
	private SerialUtils serialUtils;
	
	@Mock
	private SerialPortProxy serialPort;

	@Mock
	TestSerialPortInputStream inputStream;
	
	@Mock
	TestSerialPortOutputStream outputStream;
	
   @Before
    public void setup() {
    	MockitoAnnotations.initMocks(this);
    }
	
	
	@Test
	public void testReadPointValue(){
		
		SerialDataSourceVO vo = SerialDataSourceTestData.getStandardDataSourceVO();
		SerialDataSourceRT rt = (SerialDataSourceRT) vo.createDataSourceRT();

		
		//Mock up the serial port
		SerialParameters params = new SerialParameters();
		params.setCommPortId(vo.getCommPortId());
        params.setPortOwnerName("Mango Serial Data Source");
        params.setBaudRate(vo.getBaudRate());
        params.setFlowControlIn(vo.getFlowControlIn());
        params.setFlowControlOut(vo.getFlowControlOut());
        params.setDataBits(vo.getDataBits());
        params.setStopBits(vo.getStopBits());
        params.setParity(vo.getParity());
        params.setRecieveTimeout(vo.getReadTimeout());
		
        //User Power Mock to mock static classes
        PowerMockito.mockStatic(SerialUtils.class);
        
        //Mock the is port owned call
		when(SerialUtils.portOwned(vo.getCommPortId())).thenReturn(false);
		
		//Mock the Get Port
		try {
			when(SerialUtils.openSerialPort(params)).thenReturn(this.serialPort);
		} catch (SerialPortException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
		
		//Connect
		try {
			assertTrue(rt.connect());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		//Create an event to force the Data Source to read the port
		SerialPortProxyEvent evt = new SerialPortProxyEvent(System.currentTimeMillis());
		rt.serialEvent(evt);
		
	}
	
	
	
	
}
