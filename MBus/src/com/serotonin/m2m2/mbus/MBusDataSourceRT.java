/*
 *   Mango - Open Source M2M - http://mango.serotoninsoftware.com
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;
import java.io.IOException;
import java.util.Arrays;

import net.sf.mbus4j.dataframes.datablocks.BigDecimalDataBlock;
import net.sf.mbus4j.dataframes.datablocks.IntegerDataBlock;
import net.sf.mbus4j.dataframes.datablocks.LongDataBlock;
import net.sf.mbus4j.dataframes.datablocks.RealDataBlock;
import net.sf.mbus4j.dataframes.datablocks.ShortDataBlock;
import net.sf.mbus4j.dataframes.datablocks.StringDataBlock;
import net.sf.mbus4j.master.MBusMaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.sf.mbus4j.MBusUtils;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.dataframes.UserDataResponse;
import net.sf.mbus4j.dataframes.datablocks.BcdValue;
import net.sf.mbus4j.dataframes.datablocks.ByteDataBlock;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;

/**
 * TODO datatype NUMERIC_INT is missing TODO Starttime for timpepoints ???
 */
public class MBusDataSourceRT extends PollingDataSource<MBusDataSourceVO> {

    private final static Log LOG = LogFactory.getLog(MBusDataSourceRT.class);
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 1;
    public static final int POINT_READ_EXCEPTION_EVENT = 2;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 3;
    private final MBusDataSourceVO vo;
    private final MBusMaster master = new MBusMaster();

    public MBusDataSourceRT(MBusDataSourceVO vo) {
        super(vo);
        this.vo = vo;
        LOG.fatal("UpdatePeriodType " + vo.getUpdatePeriodType());
        LOG.fatal("UpdatePeriods " + vo.getUpdatePeriods());
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), vo.isQuantize());
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void terminate() {
        super.terminate();
    }

    @Override
    protected synchronized void doPoll(long time) {
        boolean pointError = false;

        final List<UserDataResponse> udrs = new LinkedList<>();

        if (openConnection()) {
            try {
                for (DataPointRT point : dataPoints) {
                    final MBusPointLocatorRT locatorRT = point.getPointLocator();
                    final MBusPointLocatorVO locatorVo = locatorRT.getVo();

                    UserDataResponse udr = getUdr(udrs, locatorVo);
                    if (udr == null) {
                        if (locatorVo.isPrimaryAddressing()) {
                            udr = master.readResponse(locatorVo.getAddress());
                        } else {
                            udr = master.readResponseBySecondary(MBusUtils.int2Bcd(locatorVo.getIdentNumber()), locatorVo.getManufacturer(), locatorVo.getVersion(), locatorVo.getMedium());
                        }
                        if (udr == null) {
                            //insert empty UDR to prevent rurter tries in this polling round
                            locatorRT.needCheckDifAndVif = true;
                            udr = new UserDataResponse();
                            udr.setIdentNumber(locatorVo.getIdentNumber());
                            udr.setMedium(locatorVo.getMedium());
                            udr.setManufacturer(locatorVo.getManufacturer());
                            udr.setVersion(locatorVo.getVersion());
                            udrs.add(udr);
                            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                    new TranslatableMessage("event.exception2", locatorVo.getDeviceName(), "Can't read device"));
                            pointError = true;
                        } else {
                            udrs.add(udr);
                        }
                    }
                    try {
                        if ((locatorVo.getDbIndex() == -1) || locatorRT.needCheckDifAndVif) {
                            int[] idx = findDataBlocks(udr, locatorVo);
                            switch (idx.length) {
                                case 0:
                                    LOG.fatal("DataBlock not found: " + locatorVo.toString());
                                    LOG.fatal(udr.toString());
                                    raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                            new TranslatableMessage("event.exception2", locatorVo.toString() + "Can't find datablock"));
                                    pointError = true;
                                    return;
                                case 1:
                                    if (locatorVo.getDbIndex() == idx[0]) {
                                        locatorRT.needCheckDifAndVif = false;
                                    } else if (locatorVo.getDbIndex() == -1) {
                                        LOG.info("Set DB Index: " + locatorVo.toString());
                                        locatorVo.setDbIndex(idx[0]);
                                        locatorRT.needCheckDifAndVif = false;
                                    } else {
                                        LOG.fatal("Index changed of datablock: " + locatorVo.toString());
                                        LOG.fatal(udr.toString());
                                        raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                                new TranslatableMessage("event.exception2", locatorVo.toString() + "Index changed of datablock Please maually correct the dbIndex"));
                                        return;
                                    }
                                    break;
                                default:
                                    if (locatorVo.getDbIndex() == -1) {
                                        LOG.fatal("too many dataBlocks found: " + locatorVo.toString());
                                        LOG.fatal(udr.toString());
                                        raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                                new TranslatableMessage("event.exception2", locatorVo.toString() + "Found more then one datablock! Please specify dbIndex manually!"));
                                        return;
                                    } else {
                                        for (int i : idx) {
                                            if (i == locatorVo.getDbIndex()) {
                                                locatorRT.needCheckDifAndVif = false;
                                                break;
                                            }
                                        }
                                        if (locatorRT.needCheckDifAndVif) {
                                            LOG.fatal("Matching dataBlock not found: " + locatorVo.toString());
                                            LOG.fatal(udr.toString());
                                            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                                    new TranslatableMessage("event.exception2", locatorVo.toString() + "Can't find matching datablock! Please specify dbIndex manually!"));
                                            pointError = true;
                                            return;
                                        }
                                    }
                            }
                        }
                        final DataBlock db = udr.getDataBlock(locatorVo.getDbIndex());
                        pointError = setValue(db, pointError, time, point, locatorRT, locatorVo);

                    } catch (IndexOutOfBoundsException ex) {
                        // Handle if datablock is not there...
                        raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                                new TranslatableMessage("event.exception2", point.getVO().getExtendedName(),
                                        "No Data"));
                        pointError = true;
                    }
                }

                if (!pointError) {
                    returnToNormal(POINT_READ_EXCEPTION_EVENT, time);
                }

                returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, time);
            } catch (InterruptedException ex) {
                LOG.error("doPoll() interrupted", ex);
                raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage(
                        "event.exception2", ex.getMessage(), "doPoll() Interrupted"));
            } catch (IOException ex) {
                LOG.error("doPoll() IO Ex", ex);
                raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage(
                        "event.exception2", ex.getMessage(), "doPoll() IO Ex"));
            } finally {
                closeConnection();
            }
        }
    }

    protected boolean setValue(DataBlock db, boolean pointError, long time, DataPointRT point, final MBusPointLocatorRT locatorRT, final MBusPointLocatorVO locatorVo) {
        try {
            if ((db instanceof BcdValue) && ((BcdValue) db).isBcdError()) {
                pointError = true;
                LOG.fatal("BCD Error : " + ((BcdValue) db).getBcdError());
                raiseEvent(POINT_READ_EXCEPTION_EVENT, time, true,
                        new TranslatableMessage("event.exception2", point.getVO().getExtendedName(),
                                "BCD error value: " + ((BcdValue) db).getBcdError()));
                pointError = true;
            } else if (db instanceof ByteDataBlock) {
                point.updatePointValue(
                        new PointValueTime(locatorRT.calcCorrectedValue(((ByteDataBlock) db).getValue(), db.getCorrectionExponent(locatorVo.effectiveSiPrefix()), db.getCorrectionConstant()), time));
            } else if (db instanceof ShortDataBlock) {
                point.updatePointValue(
                        new PointValueTime(locatorRT.calcCorrectedValue(((ShortDataBlock) db).getValue(), db.getCorrectionExponent(locatorVo.effectiveSiPrefix()), db.getCorrectionConstant()), time));
            } else if (db instanceof IntegerDataBlock) {
                point.updatePointValue(
                        new PointValueTime(locatorRT.calcCorrectedValue(((IntegerDataBlock) db).getValue(), db.getCorrectionExponent(locatorVo.effectiveSiPrefix()), db.getCorrectionConstant()), time));
            } else if (db instanceof LongDataBlock) {
                point.updatePointValue(
                        new PointValueTime(locatorRT.calcCorrectedValue(((LongDataBlock) db).getValue(), db.getCorrectionExponent(locatorVo.effectiveSiPrefix()), db.getCorrectionConstant()), time));
            } else if (db instanceof RealDataBlock) {
                point.updatePointValue(
                        new PointValueTime(locatorRT.calcCorrectedValue(((RealDataBlock) db).getValue(), db.getCorrectionExponent(locatorVo.effectiveSiPrefix()), db.getCorrectionConstant()), time));
            } else if (db instanceof BigDecimalDataBlock) {
                point.updatePointValue(
                        new PointValueTime(locatorRT.calcCorrectedValue(((BigDecimalDataBlock) db).getValue().doubleValue(), db.getCorrectionExponent(locatorVo.effectiveSiPrefix()), db.getCorrectionConstant()), time));
            } else if (db instanceof StringDataBlock) {
                point.updatePointValue(
                        new PointValueTime(((StringDataBlock) db).getValue(), time));
            } else {
                LOG.fatal("Dont know how to save: " + point.getVO().getExtendedName());
                raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                        new TranslatableMessage("event.exception2", point.getVO().getExtendedName(),
                                "Dont know how to save"));
                pointError = true;

            }
        } catch (Exception ex) {
            LOG.fatal("Error during saving: " + vo.getName(), ex);
            raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true,
                    new TranslatableMessage("event.exception2", point.getVO().getExtendedName(),
                            "Ex: " + ex));
            pointError = true;

        }
        return pointError;
    }

    @Override
    public void setPointValueImpl(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no op
    }

    private boolean openConnection() {
        try {
            LOG.debug("MBus Try open serial port");
            if (vo.getConnection() instanceof SerialPortConnection) {
                //replace with buggy jssc
                SerialPortConnection spc = (SerialPortConnection) vo.getConnection();
                master.setConnection(new MangoMBusSerialConnection("MBus " + this.vo.getXid(), spc.getPortName(), spc.getBitPerSecond(), 1000));
            } else {
                master.setConnection(vo.getConnection());
            }
            master.open();
            return true;
        } catch (IOException ex) {
            LOG.fatal("MBus Open serial port exception", ex);
            master.setConnection(null);
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage(
                    "event.exception2", "openConnection() Failed", ex.getMessage()));
            return false;
        }
    }

    private void closeConnection() {
        try {
            master.close();
        } catch (IOException ex) {
            LOG.fatal("Close port", ex);
            raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage(
                    "event.exception2", "closeConnection() Failed", ex.getMessage()));
        } finally {
            master.setConnection(null);
        }
    }

    private UserDataResponse getUdr(List<UserDataResponse> udrs, MBusPointLocatorVO locatorVo) {
        for (UserDataResponse udr : udrs) {
            if ((udr.getIdentNumber() == locatorVo.getIdentNumber())
                    && Objects.equals(udr.getManufacturer(), locatorVo.getManufacturer())
                    && Objects.equals(udr.getMedium(), locatorVo.getMedium())
                    && (udr.getVersion() == locatorVo.getVersion())) {
                return udr;
            }
        }
        return null;
    }

    private int[] findDataBlocks(UserDataResponse userDataResponse, MBusPointLocatorVO locatorVo) {
        int[] result = new int[0];
        for (int i = 0; i < userDataResponse.getDataBlockCount(); i++) {
            DataBlock db = userDataResponse.getDataBlock(i);
            if (Objects.equals(db.getDataFieldCode(), locatorVo.difCode())
                    && Objects.equals(db.getUnitOfMeasurement(), locatorVo.unitOfMeasurement())
                    && Objects.equals(db.getFunctionField(), locatorVo.functionField())
                    && (db.getStorageNumber() == locatorVo.getStorageNumber())
                    && (db.getSubUnit() == locatorVo.getSubUnit())
                    && (db.getTariff() == locatorVo.getTariff())) {
                result = Arrays.copyOf(result, result.length + 1);
                result[result.length - 1] = i;
            }
        }
        return result;
    }

}
