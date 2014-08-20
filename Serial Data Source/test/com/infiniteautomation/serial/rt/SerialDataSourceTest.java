/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial.rt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Lists;
import com.infiniteautomation.serial.SerialDataSourceTestData;
import com.infiniteautomation.serial.SerialTestCase;
import com.infiniteautomation.serial.TestSerialPortInputStream;
import com.infiniteautomation.serial.TestSerialPortOutputStream;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.serotonin.io.serial.SerialParameters;
import com.serotonin.io.serial.SerialPortException;
import com.serotonin.io.serial.SerialPortProxy;
import com.serotonin.io.serial.SerialPortProxyEvent;
import com.serotonin.io.serial.SerialUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.MangoTestInstance;
import com.serotonin.m2m2.db.H2Proxy;
import com.serotonin.m2m2.rt.EventManager;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.util.properties.ReloadingProperties;

/**
 * @author Terry Packer
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SerialUtils.class, Common.class})
public class SerialDataSourceTest {
	
	private static Map<String, DataPointRT> registeredPoints = new HashMap<String, DataPointRT>();
	private static Map<String, SerialTestCase> testCases = new HashMap<String, SerialTestCase>();
	static {
		testCases.put("Hello World!;", new SerialTestCase("matchAll", "terminator", ";", 1, new String[]{"Hello World!;"}));
		testCases.put("8812;abcf;", new SerialTestCase("matchAll", "terminator", ";", 2, new String[]{"8812;","abcf;"}));
		testCases.put("", new SerialTestCase("matchAll", "terminator", ";", 0, new String[]{}));
		testCases.put("testStr\n\nabs", new SerialTestCase("newlineTerminated", "terminator", "\n", 2, new String[]{"testStr", ""}));
		testCases.put("ok;", new SerialTestCase("matchAll", "terminator", ";", 1, new String[]{"ok;"}));
//Commented out as requiring more mocks for timers		testCases.put("Hello World!;", new SerialTestCase("timeout", ";", 1, new String[]{"Hello World!;"}));
	};

	@Mock
	private SerialUtils serialUtils;
	
	@Mock
	private SerialPortProxy serialPort;

//	@Mock
	TestSerialPortInputStream inputStream;
	
//	@Mock
	TestSerialPortOutputStream outputStream;
	
	@Mock
	RuntimeManager runtimeManager;
	
	@Mock
	EventManager eventManager;
	
   @Before
    public void setup() {
    	MockitoAnnotations.initMocks(this);
    	
		//Make sure that Common and other classes are properly loaded
		mockMangoInternals();
    }
	
	
	@Test
	public void testReadPointValue(){
		
		SerialDataSourceVO vo = SerialDataSourceTestData.getStandardDataSourceVO();
		SerialDataSourceRT rt = (SerialDataSourceRT) vo.createDataSourceRT();
		DataPointRT dprt = SerialDataSourceTestData.getMatchAllPoint();
		registeredPoints.put("matchAll", dprt);
		rt.addDataPoint(dprt);
		dprt = SerialDataSourceTestData.getNewlineTerminated();
		registeredPoints.put("newlineTerminated", dprt);
		rt.addDataPoint(dprt);
		rt.forcePointReload();
		
		inputStream = new TestSerialPortInputStream();
		
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
		when(serialPort.getInputStream()).thenReturn(inputStream);
		when(serialPort.getOutputStream()).thenReturn(outputStream);
		
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
		
		for(String s : testCases.keySet()) {
			SerialTestCase stc = testCases.get(s);
			if(stc.getCondition().equals("terminator")) {
				vo.setMessageTerminator(stc.getTerminator());
				vo.setUseTerminator(true);
			}
			else if(stc.getCondition().equals("timeout")) {
				vo.setUseTerminator(false);
			}
			//load a test input
			inputStream.pushToMockStream(s);
			
			//Create an event to force the Data Source to read the port
			SerialPortProxyEvent evt = new SerialPortProxyEvent(System.currentTimeMillis());
			rt.serialEvent(evt);
			
			//test the return value(s), reverse list because Mango stores latest value at [0]
			dprt = registeredPoints.get(stc.getTargetPoint());
			if(dprt == null)
				continue;
			List<PointValueTime> pvts = Lists.reverse(dprt.getLatestPointValues(stc.getNewValueCount()));
			
			for(int k = 0; k < stc.getResults().length; k+=1) {
				try {
					assertEquals(stc.getResult(k), pvts.get(k).getStringValue());
				} catch(Exception e) { //catching out of bounds exceptions
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		}
	}
	
	private void mockMangoInternals() {
		PowerMockito.mockStatic(Common.class);
		when(Common.getLocale()).thenReturn(new Locale("en"));
		
    	Common.envProps = new ReloadingProperties("test-env");
        Common.MA_HOME =  System.getProperty("ma.home"); 
    	
        Common.runtimeManager = this.runtimeManager;
        Common.eventManager = this.eventManager;
        
        //Start the Database so we can use Daos (Base Dao requires this)
    	H2Proxy proxy = new H2Proxy();
        Common.databaseProxy = proxy;
        proxy.initialize(null);
	}
	
	
}
