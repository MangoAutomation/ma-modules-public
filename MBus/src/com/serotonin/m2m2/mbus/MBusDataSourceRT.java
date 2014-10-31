/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import java.io.IOException;

import net.sf.mbus4j.dataframes.datablocks.BigDecimalDataBlock;
import net.sf.mbus4j.dataframes.datablocks.ByteDataBlock;
import net.sf.mbus4j.dataframes.datablocks.IntegerDataBlock;
import net.sf.mbus4j.dataframes.datablocks.LongDataBlock;
import net.sf.mbus4j.dataframes.datablocks.RealDataBlock;
import net.sf.mbus4j.dataframes.datablocks.ShortDataBlock;
import net.sf.mbus4j.dataframes.datablocks.StringDataBlock;
import net.sf.mbus4j.master.MBusMaster;
import net.sf.mbus4j.master.ValueRequest;
import net.sf.mbus4j.master.ValueRequestPointLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.io.serial.SerialParameters;
import com.serotonin.io.serial.SerialPortProxy;
import com.serotonin.io.serial.SerialUtils;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;

/**
 * TODO datatype NUMERIC_INT is missing TODO Starttime for timpepoints ???
 */
public class MBusDataSourceRT extends PollingDataSource {
    private final static Log LOG = LogFactory.getLog(MBusDataSourceRT.class);
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 1;
    public static final int POINT_READ_EXCEPTION_EVENT = 2;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 3;
    public static final int POLL_ABORTED_EVENT = 4;
    
    private final MBusDataSourceVO vo;
    // private final long nextRescan = 0;
    private final MBusMaster master;

    public MBusDataSourceRT(MBusDataSourceVO vo) {
        super(vo);
        this.vo = vo;
        this.master = new MBusMaster();
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
    }

    @Override
    public void initialize() {
        LOG.info("INITIALIZE");
        openSerialPort();
        super.initialize();

    }

    @Override
    public void terminate() {
        LOG.info("TERMINATE");
        closePort();
        super.terminate();

    }

    @Override
    protected synchronized void doPoll(long time) {
        ValueRequest<DataPointRT> request = new ValueRequest<DataPointRT>();
        for (DataPointRT point : dataPoints) {
            final MBusPointLocatorRT locator = point.getPointLocator();
            request.add(locator.createValueRequestPointLocator(point));
        }

        if (openSerialPort()) {
            try {
                master.readValues(request);
                for (ValueRequestPointLocator<DataPointRT> vr : request) {
                    try {
                        if (vr.getDb() == null) {
                            // TODO handle null value properly
                        	
                        	DataPointRT dprt = vr.getReference();
                        	String msg = dprt.getVO().getName() + " " + dprt.getVO().getXid() + " ";
                        	if(	vr.getVif()!= null){
                        		msg = vr.getVif().toString();
                        	}
                            LOG.warn("Read null value for: " + msg);
                            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                    new TranslatableMessage("event.exception2", vo.getName(),
                                            "Dont know how to save value for point with xid : ", dprt.getVO().getXid()));
                        }
                        else if (vr.getDb() instanceof ByteDataBlock) {
                            vr.getReference().updatePointValue(
                                    new PointValueTime((double) ((ByteDataBlock) vr.getDb()).getValue(), time));
                        }
                        else if (vr.getDb() instanceof ShortDataBlock) {
                            vr.getReference().updatePointValue(
                                    new PointValueTime((double) ((ShortDataBlock) vr.getDb()).getValue(), time));
                        }
                        else if (vr.getDb() instanceof IntegerDataBlock) {
                            vr.getReference().updatePointValue(
                                    new PointValueTime((double) ((IntegerDataBlock) vr.getDb()).getValue(), time));
                        }
                        else if (vr.getDb() instanceof LongDataBlock) {
                            vr.getReference().updatePointValue(
                                    new PointValueTime(((LongDataBlock) vr.getDb()).getValue(), time));
                        }
                        else if (vr.getDb() instanceof RealDataBlock) {
                            vr.getReference().updatePointValue(
                                    new PointValueTime(((RealDataBlock) vr.getDb()).getValue(), time));
                        }
                        else if (vr.getDb() instanceof BigDecimalDataBlock) {
                            vr.getReference().updatePointValue(
                                    new PointValueTime(((BigDecimalDataBlock) vr.getDb()).getValue().doubleValue(),
                                            time));
                        }
                        else if (vr.getDb() instanceof StringDataBlock) {
                            vr.getReference().updatePointValue(
                                    new PointValueTime(((StringDataBlock) vr.getDb()).getValue(), time));
                        }
                        else {
                            LOG.fatal("Dont know how to save : " + vr.getReference());
                            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                    new TranslatableMessage("event.exception2", vo.getName(),
                                            "Dont know how to save : ", "Datapoint"));

                        }
                    }
                    catch (Exception ex) {
                        LOG.fatal("Error during saving: " + vr.getReference(), ex);
                    }

                }
                returnToNormal(POINT_READ_EXCEPTION_EVENT, time);
                returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, time);

            }
            catch (InterruptedException ex) {
                raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                        getSerialExceptionMessage(ex, vo.getCommPortId()));
                LOG.error("cant set value of", ex);
            }
            catch (IOException ex) {
                raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                        getSerialExceptionMessage(ex, vo.getCommPortId()));
                LOG.error("cant set value of", ex);
            }
            finally {
                closePort();
            }
        }
    }

    @Override
    public synchronized void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no op
    }

    private boolean openSerialPort() {
        try {
        	if(master.getConnection() != null){
	        	switch(master.getConnection().getConnState()){
	        		case OPEN:
	        			return true;
	        		case CLOSING:
	        		case OPENING:
	        			return false;
	        		default:
	        	}
        	}
            
            //Hack to allow reading from TCP
//            EthernetComBridge bridge = new EthernetComBridge("99.225.170.204", 10001, 1000);
//            Connection conn = new MBusSerialPortBridge(bridge, vo.getBaudRate(), 0);
            
            SerialParameters params = new SerialParameters();
            params.setCommPortId(vo.getCommPortId());
            params.setPortOwnerName("Mango MBus Serial Data Source");
            params.setBaudRate(vo.getBaudRate());
            params.setFlowControlIn(vo.getFlowControlIn());
            params.setFlowControlOut(vo.getFlowControlOut());
            params.setDataBits(vo.getDataBits());
            params.setStopBits(vo.getStopBits());
            params.setParity(vo.getParity());
            SerialPortProxy serialPort = SerialUtils.openSerialPort(params);
            MangoMBusSerialConnection conn = new MangoMBusSerialConnection(serialPort, vo.getResponseTimeoutOffset());
            
            master.setConnection(conn);
            master.open();
            
            //master.setStreams(sPort.getInputStream(), sPort.getOutputStream(), vo.getBaudRate());
            return true;
        }
        catch (Exception ex) {
            LOG.fatal("MBus Open serial port exception", ex);
            // Raise an event.
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                    getSerialExceptionMessage(ex, vo.getCommPortId()));
            return false;
        }
    }

    private void closePort() {
        try {
            master.close();
        }
        catch (IOException ex) {
            LOG.fatal("Close port", ex);
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage(
                    "event.exception2", vo.getName(), ex.getMessage(), "HALLO3"));
        }
    }
}
