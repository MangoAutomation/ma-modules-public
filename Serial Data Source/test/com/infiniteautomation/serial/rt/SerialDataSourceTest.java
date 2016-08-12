/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial.rt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Lists;
import com.infiniteautomation.mango.io.serial.SerialPortException;
import com.infiniteautomation.mango.io.serial.SerialPortProxy;
import com.infiniteautomation.mango.io.serial.SerialPortProxyEvent;
import com.infiniteautomation.serial.SerialDataSourceTestCase;
import com.infiniteautomation.serial.SerialDataSourceTestData;
import com.infiniteautomation.serial.TestSerialPortInputStream;
import com.infiniteautomation.serial.TestSerialPortOutputStream;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.serotonin.m2m2.Common;
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
@PrepareForTest({Common.class})
public class SerialDataSourceTest {
	
	private static Map<String, DataPointRT> registeredPoints = new HashMap<String, DataPointRT>();
	private static Map<String, SerialDataSourceTestCase> testCases = new HashMap<String, SerialDataSourceTestCase>();

	
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
		SerialDataSourceVO vo = SerialDataSourceTestData.getStandardDataSourceVO();
		testCases.put("Hello World!;", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(vo), "terminator", ";", 1, new String[]{"Hello World!;"}));
		testCases.put("8812;abcf;", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(vo), "terminator", ";", 2, new String[]{"8812;","abcf;"}));
		testCases.put("", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(vo), "terminator", ";", 0, new String[]{}));
		testCases.put("testStr\n\nabs", new SerialDataSourceTestCase(SerialDataSourceTestData.getNewlineTerminated(vo), "terminator", "\n", 2, new String[]{"testStr", ""}));
		testCases.put("ok;", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(vo), "terminator", ";", 1, new String[]{"ok;"}));
				
		//Commented out as requiring more mocks for timers		testCases.put("Hello World!;", new SerialTestCase("timeout", ";", 1, new String[]{"Hello World!;"}));
    }
	
	
    /**
     * TODO Break this into pieces to be re-usable
     */
	@Test
	public void testCustomPoint(){
		String message = "000005,Fri 11 January 2013 15:18:55,\u0002Q,244,000.03,1017.3,049.2,+021.4,+10.3,+040.50,+000.06,+000.04,0000.000,+11.6,00,\u000277\r\n";
				
		String[] expected = {"244","000.03","+000.06"};
		
		//Setup Data Source
		String dataSourceMessageRegex = "()[\\d\\D\\s\\S\\w\\W]*";	
		int dataSourcePointIdentifierIndex = 1;
		boolean dataSourceUseTerminator = true;
		String dataSourceMessageTerminator = "\n";
		
		
		SerialDataSourceVO vo = SerialDataSourceTestData.getStandardDataSourceVO();
		vo.setMessageRegex(dataSourceMessageRegex);
		vo.setPointIdentifierIndex(dataSourcePointIdentifierIndex);
		vo.setUseTerminator(dataSourceUseTerminator);
		vo.setMessageTerminator(dataSourceMessageTerminator);
		SerialDataSourceRT rt = (SerialDataSourceRT) vo.createDataSourceRT();
		
		//Setup Data Point RTs
		//String pointIdentifier = "";
		String windDirectionRegex = "[\\d]*,[\\w\\s:]*,[\u0002\\w]*,([\\w]*),[\\d\\D\\s\\S\\w\\W]*"; //"([\\d\\D\\s\\S\\w\\W]*),[\\d\\D\\s\\S\\w\\W]*";
		String windSpeedRegex = "[\\d]*,[\\w\\s:]*,[\u0002\\w]*,[\\w]*,([\\d\\.]*),[\\d\\D\\s\\S\\w\\W]*"; //"([\\d\\D\\s\\S\\w\\W]*),[\\d\\D\\s\\S\\w\\W]*";
		String ip1Regex = "[\\d]*,[\\w\\s:]*,[\u0002\\w]*,[\\w]*,[\\d\\.]*,[\\d\\.]*,[\\d\\.]*,[\\d\\.+-]*,[\\d\\.+-]*,[\\d\\.+-]*,([\\d\\.+-]*),[\\d\\D\\s\\S\\w\\W]*"; //"([\\d\\D\\s\\S\\w\\W]*),[\\d\\D\\s\\S\\w\\W]*";

		
		
		DataPointRT windDirection = SerialDataSourceTestData.getCustomPoint(
				"windDirection",
				"windDirection",
				windDirectionRegex,
				1, 
				"", vo);
		rt.addDataPoint(windDirection);
		
		DataPointRT windSpeed = SerialDataSourceTestData.getCustomPoint(
				"windSpeed",
				"windSpeed",
				windSpeedRegex,
				1, 
				"", vo);
		rt.addDataPoint(windSpeed);
		
		DataPointRT ip1 = SerialDataSourceTestData.getCustomPoint(
				"ip1",
				"ip1",
				ip1Regex,
				1, 
				"", vo);
		rt.addDataPoint(ip1);
		
		inputStream = new TestSerialPortInputStream();
		
		//Mock up the serial port
        
        //Mock the is port owned call
		when(Common.serialPortManager.portOwned(vo.getCommPortId())).thenReturn(false);
		when(serialPort.getInputStream()).thenReturn(inputStream);
		when(serialPort.getOutputStream()).thenReturn(outputStream);
		
		//Mock the Get Port
		try {
			when(Common.serialPortManager.open(
					"test", 
					vo.getCommPortId(),
					vo.getBaudRate(),
					vo.getFlowControlIn(),
					vo.getFlowControlOut(),
					vo.getDataBits(),
					vo.getStopBits(),
					vo.getParity()
					)).thenReturn(this.serialPort);
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
		

		vo.setMessageTerminator("\n");
		vo.setUseTerminator(true);
		//load a test input
		inputStream.pushToMockStream(message);
		
		//Create an event to force the Data Source to read the port
		SerialPortProxyEvent evt = new SerialPortProxyEvent(Common.backgroundProcessing.currentTimeMillis());
		rt.serialEvent(evt);
		
		//test the return value(s), reverse list because Mango stores latest value at [0]
		List<PointValueTime> windSpeedValues = Lists.reverse(windSpeed.getLatestPointValues(1));
		List<PointValueTime> windDirectionValues = Lists.reverse(windDirection.getLatestPointValues(1));
		List<PointValueTime> ip1Values = Lists.reverse(ip1.getLatestPointValues(1));
		
		List<PointValueTime> received = new ArrayList<PointValueTime>();
		received.addAll(windSpeedValues);
		received.addAll(windDirectionValues);
		received.addAll(ip1Values);
		
		
		boolean found;
		for(int k = 0; k < expected.length; k+=1) {
			found = false;
			for(int i = 0; i < received.size(); i+=1) {
				if(expected[k].equals(received.get(i).getStringValue()))
					found = true;
			}
			if(!found)
				fail("No value match found for: '" + expected[k] + "' point value");
		}
		
	}
	
	
	public void testReadPointValue(){
		
		SerialDataSourceVO vo = SerialDataSourceTestData.getStandardDataSourceVO();
		SerialDataSourceRT rt = (SerialDataSourceRT) vo.createDataSourceRT();
		DataPointRT dprt = SerialDataSourceTestData.getMatchAllPoint(vo);
		registeredPoints.put("matchAll", dprt);
		rt.addDataPoint(dprt);
		dprt = SerialDataSourceTestData.getNewlineTerminated(vo);
		registeredPoints.put("newlineTerminated", dprt);
		rt.addDataPoint(dprt);
		
		inputStream = new TestSerialPortInputStream();
		
        //Mock the is port owned call
		when(Common.serialPortManager.portOwned(vo.getCommPortId())).thenReturn(false);
		when(serialPort.getInputStream()).thenReturn(inputStream);
		when(serialPort.getOutputStream()).thenReturn(outputStream);
		
		//Mock the Get Port
		try {
			when(Common.serialPortManager.open(
					"test", 
					vo.getCommPortId(),
					vo.getBaudRate(),
					vo.getFlowControlIn(),
					vo.getFlowControlOut(),
					vo.getDataBits(),
					vo.getStopBits(),
					vo.getParity()
					)).thenReturn(this.serialPort);
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
			SerialDataSourceTestCase stc = testCases.get(s);
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
			SerialPortProxyEvent evt = new SerialPortProxyEvent(Common.backgroundProcessing.currentTimeMillis());
			rt.serialEvent(evt);
			
			//test the return value(s), reverse list because Mango stores latest value at [0]
			dprt = registeredPoints.get(stc.getTargetPoint());
			if(dprt == null)
				continue;
			List<PointValueTime> pvts = Lists.reverse(dprt.getLatestPointValues(stc.getNewValueCount()));
			
			boolean found;
			for(int k = 0; k < stc.getResults().length; k+=1) {
				found = false;
				for(int i = 0; i < pvts.size(); i+=1) {
					if(stc.getResult(k).equals(pvts.get(i).getStringValue()))
						found = true;
				}
				if(!found)
					fail("No value match found for: '" + stc.getResult(k) + "' point value");
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
