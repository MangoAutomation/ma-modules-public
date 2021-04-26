/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.serial.rt;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.infiniteautomation.mango.io.serial.DataBits;
import com.infiniteautomation.mango.io.serial.FlowControl;
import com.infiniteautomation.mango.io.serial.Parity;
import com.infiniteautomation.mango.io.serial.SerialPortException;
import com.infiniteautomation.mango.io.serial.SerialPortProxy;
import com.infiniteautomation.mango.io.serial.SerialPortProxyEvent;
import com.infiniteautomation.mango.io.serial.StopBits;
import com.infiniteautomation.serial.SerialDataSourceTestCase;
import com.infiniteautomation.serial.SerialDataSourceTestData;
import com.infiniteautomation.serial.TestSerialPortInputStream;
import com.infiniteautomation.serial.TestSerialPortOutputStream;
import com.infiniteautomation.serial.TestSerialPortProxy;
import com.infiniteautomation.serial.vo.SerialDataSourceVO;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.MangoTestBase;
import com.serotonin.m2m2.MockSerialPortManager;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;

/**
 * @author Terry Packer
 *
 */
public class SerialDataSourceTest extends MangoTestBase {

    protected static Map<String, SerialDataSourceTestCase> testCases = new HashMap<String, SerialDataSourceTestCase>();
    protected TestSerialPortProxy proxy;

    protected SerialDataSourceVO vo;
    protected SerialDataSourceRT ds;
    protected SerialDataSourceRT rt;
    protected long time; //Time during simulation



    @Before
    @Override
    public void before() {
        super.before();
        this.proxy = new TestSerialPortProxy(new TestSerialPortInputStream(), new TestSerialPortOutputStream());
        Common.serialPortManager = new SerialDataSourceSerialPortManager(proxy);

        vo = SerialDataSourceTestData.getStandardDataSourceVO();
        ds = vo.createDataSourceRT();
        rt = vo.createDataSourceRT();
        rt.initialize(false);

        testCases.put("Hello World!;", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(ds), "terminator", ";", 1, new String[]{"Hello World!;"}));
        testCases.put("8812;abcf;", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(ds), "terminator", ";", 2, new String[]{"8812;","abcf;"}));
        testCases.put("", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(ds), "terminator", ";", 0, new String[]{}));
        testCases.put("testStr\n\nabs", new SerialDataSourceTestCase(SerialDataSourceTestData.getNewlineTerminated(ds), "terminator", "\n", 2, new String[]{"testStr", ""}));
        testCases.put("ok;", new SerialDataSourceTestCase(SerialDataSourceTestData.getMatchAllPoint(ds), "terminator", ";", 1, new String[]{"ok;"}));

        //Clean out the timer's tasks
        time = System.currentTimeMillis() - 1000000;
        timer.setStartTime(time);

        //TODO requiring more mocks for timers		testCases.put("Hello World!;", new SerialTestCase("timeout", ";", 1, new String[]{"Hello World!;"}));
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
        SerialDataSourceRT rt = vo.createDataSourceRT();
        rt.initialize(false);

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
                "", ds);
        rt.addDataPoint(windDirection);

        DataPointRT windSpeed = SerialDataSourceTestData.getCustomPoint(
                "windSpeed",
                "windSpeed",
                windSpeedRegex,
                1,
                "", ds);
        rt.addDataPoint(windSpeed);

        DataPointRT ip1 = SerialDataSourceTestData.getCustomPoint(
                "ip1",
                "ip1",
                ip1Regex,
                1,
                "", ds);
        rt.addDataPoint(ip1);

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
        proxy.getTestInputStream().pushToMockStream(message);

        //Create an event to force the Data Source to read the port
        SerialPortProxyEvent evt = new SerialPortProxyEvent(System.currentTimeMillis());
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

    @Test
    public void testReadPointValue(){

        //Connect
        try {
            assertTrue(rt.connect());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        for(String s : testCases.keySet()) {
            SerialDataSourceTestCase stc = testCases.get(s);

            rt.addDataPoint(stc.getTargetPoint());

            if(stc.getCondition().equals("terminator")) {
                vo.setMessageTerminator(stc.getTerminator());
                vo.setUseTerminator(true);
            }
            else if(stc.getCondition().equals("timeout")) {
                vo.setUseTerminator(false);
            }
            //load a test input
            proxy.getTestInputStream().pushToMockStream(s);

            //Create an event to force the Data Source to read the port
            SerialPortProxyEvent evt = new SerialPortProxyEvent(timer.currentTimeMillis());
            rt.serialEvent(evt);

            //Fast Forward to fire any events
            time = time + 5;
            timer.fastForwardTo(time);

            List<PointValueTime> pvts = Lists.reverse(stc.getTargetPoint().getLatestPointValues(stc.getNewValueCount()));

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

            //Remove the point for the next test
            rt.removeDataPoint(stc.getTargetPoint());
        }
    }


    class SerialDataSourceSerialPortManager extends MockSerialPortManager {

        protected SerialPortProxy proxy;

        public SerialDataSourceSerialPortManager(SerialPortProxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public SerialPortProxy open(String ownerName, String commPortId, int baudRate,
                FlowControl flowControlIn, FlowControl flowControlOut, DataBits dataBits,
                StopBits stopBits, Parity parity) throws SerialPortException {
            return this.proxy;
        }
    }
}
