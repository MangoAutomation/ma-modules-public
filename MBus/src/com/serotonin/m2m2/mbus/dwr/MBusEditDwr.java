/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus.dwr;

import java.util.HashMap;
import java.util.Map;

import net.sf.mbus4j.dataframes.MBusResponseFramesContainer;
import net.sf.mbus4j.dataframes.UserDataResponse;
import net.sf.mbus4j.dataframes.datablocks.DataBlock;

import com.infiniteautomation.mango.spring.dao.DataPointDao;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.mbus.MBusDataSourceVO;
import com.serotonin.m2m2.mbus.MBusPointLocatorVO;
import com.serotonin.m2m2.mbus.MBusSearchByAddressing;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;
import java.io.IOException;
import java.util.List;
import net.sf.mbus4j.Connection;
import net.sf.mbus4j.MBusAddressing;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MBusEditDwr extends DataSourceEditDwr {

    private final static Log LOG = LogFactory.getLog(DataSourceEditDwr.class);

    //
    //
    // MBus stuff
    //
    @DwrPermission(user = true)
    public ProcessResult saveMBusDataSource(String name, String xid,
            Connection connection, int updatePeriodType, int updatePeriods, boolean quantize) {
        MBusDataSourceVO ds = (MBusDataSourceVO) Common.getUser()
                .getEditDataSource();
        ds.setXid(xid);
        ds.setName(name);
        ds.setConnection(connection);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setUpdatePeriods(updatePeriods);
        ds.setQuantize(quantize);
        return tryDataSourceSave(ds);
    }

    @DwrPermission(user = true)
    public ProcessResult saveMBusPointLocator(int id, String xid,
            String name, MBusPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator, null);
    }

    /* 	public Connection getMBusConn() {
		          TcpIpConnection conn = new TcpIpConnection();
		conn.setHost("localhost");
		conn.setPort(2000);
		return conn;
	}
     */
    @DwrPermission(user = true)
    public ProcessResult searchMBus(int dataSourceId, Connection conn, MBusSearchByAddressing addressing) {
        LOG.fatal("searchMBus");
        ProcessResult result = new ProcessResult();
        User user = Common.getUser();

        Permissions.ensureDataSourcePermission(user);
        if (!Common.runtimeManager.isDataSourceRunning(dataSourceId)) {
            MBusDiscovery discovery = new MBusDiscovery(getTranslations(), conn, addressing);
            discovery.start();
            user.setTestingUtility(discovery);
            result.addData("sourceRunning", false);
            LOG.fatal("searchMBus not running");
        } else {
            result.addData("sourceRunning", true);
            LOG.fatal("searchMBus running");
        }
        return result;
    }

    @DwrPermission(user = true)
    public Map<String, Object> mBusSearchUpdate() {
        Map<String, Object> result = new HashMap<>();
        MBusDiscovery test = Common.getUser().getTestingUtility(MBusDiscovery.class);
        if (test == null) {
            return null;
        }

        test.addUpdateInfo(result);
        return result;
    }

    @DwrPermission(user = true)
    public Map<String, Object> getMBusResponseFrames(int deviceIndex) {
        Map<String, Object> result = new HashMap<>();
        MBusDiscovery test = Common.getUser().getTestingUtility(MBusDiscovery.class);
        if (test == null) {
            return null;
        }

        test.getDeviceDetails(deviceIndex, result);
        return result;
    }

    @DwrPermission(user = true)
    public Map<String, Object> changeMBusAddress(int deviceIndex, String newAddress) {
        Map<String, Object> result = new HashMap<>();
        result.put("deviceIndex", deviceIndex);
        MBusDiscovery test = Common.getUser().getTestingUtility(MBusDiscovery.class);
        if (test == null) {
            return null;
        }
        try {
            final byte oldAddress = test.getDevice(deviceIndex).getAddress();
            byte address;
            if (newAddress.startsWith("0x")) {
                address = (byte) Short.parseShort(newAddress.substring(2), 16);
            } else {
                address = (byte) Short.parseShort(newAddress);
            }
            if (test.changeAddress(deviceIndex, address, result)) {
                //if address was changed, then change existing datapoints enabled disabled
                final DataSourceVO<?> ds = Common.getUser().getEditDataSource();
                List<DataPointVO> dpVos = DataPointDao.instance.getDataPoints(ds.getId(), null);
                for (DataPointVO dpVo : dpVos) {
                    final MBusPointLocatorVO pl = dpVo.getPointLocator();
                    if (pl.getAddress() == oldAddress) {
                        pl.setAddress(address);
                        Common.runtimeManager.saveDataPoint(dpVo);
                    }
                }
            }
            result.put("points", getPoints());
        } catch (IOException | InterruptedException e) {
            return null;
        }

        return result;
    }

    @DwrPermission(user = true)
    public DataPointVO addMBusPoint(int deviceIndex,
            int rsIndex, int dbIndex) {
        DataPointVO dp = getPoint(Common.NEW_ID, null);
        MBusPointLocatorVO locator = (MBusPointLocatorVO) dp.getPointLocator();

        MBusDiscovery test = Common.getUser().getTestingUtility(
                MBusDiscovery.class);
        if (test == null) {
            return null;
        }

        MBusResponseFramesContainer dev = test.getDevice(deviceIndex);
        if (dev.getResponseFrameContainer(rsIndex).getResponseFrame() instanceof UserDataResponse) {
            UserDataResponse udr = (UserDataResponse) dev
                    .getResponseFrameContainer(rsIndex).getResponseFrame();
            DataBlock db = udr.getDataBlock(dbIndex);

            dp.setName(db.getParamDescr());

            locator.setDbIndex(dbIndex);
            locator.setAddressing(MBusAddressing.PRIMARY);
            locator.setAddress(dev.getAddress());
            locator.setMedium(dev.getMedium());
            locator.setManufacturer(dev.getManufacturer());
            locator.setVersion(dev.getVersion());
            locator.setIdentNumber(dev.getIdentNumber());
            locator.setResponseFrame(dev.getResponseFrameContainer(rsIndex)
                    .getName());
            locator.setSubUnit(db.getSubUnit());
            locator.setDifCode(db.getDataFieldCode().getLabel());
            locator.setFunctionField(db.getFunctionField().getLabel());
            locator.setStorageNumber(db.getStorageNumber());
            locator.setTariff(db.getTariff());
            locator.setSiPrefix(db.getSiPrefix().getLabel());
            locator.setEffectiveSiPrefix(locator.getSiPrefix());
            locator.setUnitOfMeasurement(db.getUnitOfMeasurement() != null ? db.getUnitOfMeasurement().getLabel(): null);
            locator.setVifType(db.getVif().getVifType().getLabel());
            locator.setVifLabel(db.getVif().getLabel());
            locator.setExponent(db.getExponent());
            if (db.getVifes() != null) {
                final String[] vifeLabels = new String[db.getVifes().length];
                final String[] vifeTypes = new String[db.getVifes().length];
                for (int i = 0; i < vifeLabels.length; i++) {
                    vifeTypes[i] = db.getVifes()[i].getVifeType().getLabel();
                    vifeLabels[i] = db.getVifes()[i].getLabel();
                }
                locator.setVifeTypes(vifeTypes);
                locator.setVifeLabels(vifeLabels);
            } else {
                locator.setVifeLabels(null);
            }
        }
        return dp;
    }
}
