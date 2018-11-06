/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.dwr;

import java.util.ArrayList;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.util.IntMessagePair;
import com.serotonin.m2m2.view.chart.ImageChartRenderer;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.virtual.vo.VirtualPointLocatorVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.BasicDataSourceVO;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;
import com.serotonin.m2m2.web.dwr.DataSourceEditDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

public class VirtualEditDwr extends DataSourceEditDwr {
    @DwrPermission(user = true)
    public ProcessResult saveVirtualDataSource(BasicDataSourceVO basic, int updatePeriods, int updatePeriodType, boolean polling) {
        VirtualDataSourceVO ds = (VirtualDataSourceVO) Common.getHttpUser().getEditDataSource();

        setBasicProps(ds, basic);
        ds.setUpdatePeriods(updatePeriods);
        ds.setUpdatePeriodType(updatePeriodType);
        ds.setPolling(polling);

        return tryDataSourceSave(ds);
    }

    @DwrPermission(user = true)
    public IntMessagePair[] getChangeTypes(int dataTypeId) {
        return ChangeTypeVO.getChangeTypes(dataTypeId);
    }

    @DwrPermission(user = true)
    public ProcessResult saveVirtualPointLocator(int id, String xid, String name, VirtualPointLocatorVO locator) {
        return validatePoint(id, xid, name, locator);
    }
    
    /**
     * Test Method for debugging system.
     */
    @DwrPermission(admin = true)
    public void createTestSource(){
		VirtualDataSourceVO ds = new VirtualDataSourceVO();
		
		DataSourceDefinition def = ModuleRegistry.getDataSourceDefinition("VIRTUAL");
        ds = (VirtualDataSourceVO) def.baseCreateDataSourceVO();
        ds.setId(Common.NEW_ID);
        ds.setXid(DataSourceDao.getInstance().generateUniqueXid());
		ds.setName("Test Virtual");
		ds.setEnabled(true);
		ds.setUpdatePeriods(5);
		ds.setUpdatePeriodType(TimePeriods.SECONDS);
		ds.setPolling(true);
		
		ProcessResult response = new ProcessResult();
		ds.validate(response);
		if(!response.getHasMessages())
			Common.runtimeManager.saveDataSource(ds);
		else
			throw new RuntimeException("Invalid data!");
		
		
		DataPointDao dpDao = DataPointDao.getInstance();
		//Create Test Points
		for(int i=0; i<10; i++){
			VirtualPointLocatorVO pointLocator = ds.createPointLocator();
			//Create a Random Points
			pointLocator.setDataTypeId(DataTypes.NUMERIC);
			pointLocator.setChangeTypeId(ChangeTypeVO.Types.RANDOM_ANALOG);
			pointLocator.getRandomAnalogChange().setMin(0);
			pointLocator.getRandomAnalogChange().setMax(100);
			pointLocator.getRandomAnalogChange().setStartValue("1");
			pointLocator.setSettable(true);
			
			
			DataPointVO dp = new DataPointVO();
			dp.setXid(dpDao.generateUniqueXid());
			dp.setName("Virtual Random " + i);
            dp.setDataSourceId(ds.getId());
            dp.setDataSourceTypeName(ds.getDefinition().getDataSourceTypeName());
            dp.setDeviceName(ds.getName());
            dp.setEventDetectors(new ArrayList<AbstractPointEventDetectorVO<?>>(0));
            dp.defaultTextRenderer();
            //Setup the Chart Renderer
            ImageChartRenderer chartRenderer = new ImageChartRenderer(TimePeriods.DAYS,5);
            dp.setChartRenderer(chartRenderer);
			
            dp.setPointLocator(pointLocator);
			dp.setEnabled(true);
			dp.setDefaultCacheSize(0);			
			
			dp.validate(response);
			if(!response.getHasMessages())
				Common.runtimeManager.saveDataPoint(dp);
			else
				throw new RuntimeException("Invalid data!");
			
		}
    }
    
}
