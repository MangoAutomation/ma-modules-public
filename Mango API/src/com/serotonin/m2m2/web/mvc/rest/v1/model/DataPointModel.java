/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.TemplateDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.util.UnitUtil;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.PointLocatorVO;
import com.serotonin.m2m2.vo.template.BaseTemplateVO;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumn;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnGetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVColumnSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVEntity;
import com.serotonin.m2m2.web.mvc.rest.v1.mapping.SuperclassModelDeserializer;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.LoggingPropertiesModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.LoggingPropertiesModelFactory;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.PointLocatorModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.TimePeriodModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.chartRenderer.BaseChartRendererModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.chartRenderer.ChartRendererFactory;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.textRenderer.BaseTextRendererModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.textRenderer.TextRendererFactory;


/**
 * Rest Data Model
 * 
 * 
 * @author Terry Packer
 *
 */
@CSVEntity()
public class DataPointModel extends AbstractActionVoModel<DataPointVO>{
	
	@CSVColumn(header="templateXid", order = 7)
	private String templateXid; //Used for Model Creation/Saving of Data Points
	
	@JsonProperty
	private LoggingPropertiesModel loggingProperties;
	
	@JsonProperty 
	@JsonDeserialize(using = SuperclassModelDeserializer.class)
	private BaseTextRendererModel<?> textRenderer;
	
	@JsonProperty 
	@JsonDeserialize(using = SuperclassModelDeserializer.class)
	private BaseChartRendererModel<?> chartRenderer;
	
	public DataPointModel(){
		this(new DataPointVO());
		this.loggingProperties = LoggingPropertiesModelFactory.createModel(this.data);
	}
	/**
	 * @param vo
	 */
	public DataPointModel(DataPointVO vo) {
		super(vo);
		//We must set the local properties to ensure they are in the model since
		// this constructor is used in the Mango Rest Controller Code
		if(vo.getTemplateId() != null){
			BaseTemplateVO<?> template = TemplateDao.instance.get(vo.getTemplateId());
			if(template != null)
				this.templateXid = template.getXid();
			
		}
		this.loggingProperties = LoggingPropertiesModelFactory.createModel(vo);
		this.textRenderer = TextRendererFactory.createModel(vo);
		this.chartRenderer = ChartRendererFactory.createModel(vo);
	}
	
	@CSVColumnGetter(order=3, header="deviceName")
	@JsonGetter("deviceName")
	public String getDeviceName(){
		return this.data.getDeviceName();
	}
	
	@CSVColumnSetter(order=3, header="deviceName")
	@JsonSetter("deviceName")
	public void setDeviceName(String deviceName){
		this.data.setDeviceName(deviceName);
	}
	
	@CSVColumnGetter(order=4, header="dataSourceXid")
	@JsonGetter("dataSourceXid")
	public String getDataSourceXid(){
		return this.data.getDataSourceXid();
	}
	
	@CSVColumnSetter(order=4, header="dataSourceXid")
	@JsonSetter("dataSourceXid")
	public void setDataSourceXid(String xid){
		this.data.setDataSourceXid(xid);
	}
	
	@CSVColumnGetter(order=5, header="readPermission")
	@JsonGetter("readPermission")
	public String getReadPermission(){
		return this.data.getReadPermission();
	}
	
	@CSVColumnSetter(order=5, header="readPermission")
	@JsonSetter("readPermission")
	public void setReadPermission(String readPermission){
		this.data.setReadPermission(readPermission);
	}
			
	@CSVColumnGetter(order=6, header="setPermission")
	@JsonGetter("setPermission")
	public String getSetPermission(){
		return this.data.getSetPermission();
	}
	
	@CSVColumnSetter(order=6, header="readPermission")
	@JsonSetter("setPermission")
	public void setSetPermission(String setPermission){
		this.data.setSetPermission(setPermission);
	}
	
	@JsonGetter("pointFolderId")
	public int getPointFolder(){
		return this.data.getPointFolderId();
	}
	@JsonSetter("pointFolderId")
	public void setPointFolder(int id){
		this.data.setPointFolderId(id);
	}
	
	@JsonGetter("purgeOverride")
	public boolean isPurgeOverride(){
		return this.data.isPurgeOverride();
	}
	@JsonSetter("purgeOverride")
	public void setPurgeOverride(boolean purgeOverride){
		this.data.setPurgeOverride(purgeOverride);
	}
	
	@JsonGetter("purgePeriod")
	public TimePeriodModel getPurgePeriod(){
		return new TimePeriodModel(this.data.getPurgePeriod(), this.data.getPurgeType());
	}
	@JsonSetter("purgePeriod")
	public void setPurgePeriod(TimePeriodModel model){
		this.data.setPurgePeriod(model.getPeriods());
		this.data.setPurgeType(Common.TIME_PERIOD_CODES.getId(model.getPeriodType()));
	}
	
	public LoggingPropertiesModel getLoggingProperties(){
		return loggingProperties;
	}

	public void setLoggingProperties(LoggingPropertiesModel props){
		this.loggingProperties = props;
		LoggingPropertiesModelFactory.updateDataPoint(this.data, this.loggingProperties);
	}
	

	public String getTemplateXid() {
		return templateXid;
	}
	public void setTemplateXid(String templateXid){
		this.templateXid = templateXid;
	}
	
	@CSVColumnGetter(header="pointLocatorType", order=8)
	@JsonGetter("pointLocator")
	public PointLocatorModel<?> getPointLocator(){
		PointLocatorVO vo = this.data.getPointLocator();
		if(vo == null)
			return null;
		else
			return this.data.getPointLocator().asModel();
	}
	
	@CSVColumnSetter(header="pointLocatorType", order=8)
	@JsonSetter("pointLocator")
	public void setPointLocator(PointLocatorModel<?> pl){
		if(pl != null)
			this.data.setPointLocator((PointLocatorVO)pl.getData());
	}
	
	
	
	@JsonGetter("unit")
	public String getUnit(){
		return UnitUtil.formatLocal(this.data.getUnit());
	}
	@JsonSetter("unit")
	public void setUnit(String unit){
		this.data.setUnit(UnitUtil.parseLocal(unit));
	}
	
	@JsonGetter("useIntegralUnit")
	public boolean isUseIntegralUnit(){
		return this.data.isUseIntegralUnit();
	}
	@JsonSetter("useIntegralUnit")
	public void setUseIntegralUnit(boolean useIntegralUnit){
		this.data.setUseIntegralUnit(useIntegralUnit);
	}
	
	@JsonGetter("integralUnit")
	public String getIntegralUnit(){
		return UnitUtil.formatLocal(this.data.getIntegralUnit());
	}
	@JsonSetter("integralUnit")
	public void setIntegralUnit(String unit){
		this.data.setIntegralUnit(UnitUtil.parseLocal(unit));
	}

	@JsonGetter("useRenderedUnit")
	public boolean isUseRenderedUnit(){
		return this.data.isUseRenderedUnit();
	}
	@JsonSetter("useRenderedUnit")
	public void setUseRenderedUnit(boolean useRenderedUnit){
		this.data.setUseRenderedUnit(useRenderedUnit);
	}
	
	@JsonGetter("renderedUnit")
	public String getRenderedUnit(){
		return UnitUtil.formatLocal(this.data.getRenderedUnit());
	}
	@JsonSetter("renderedUnit")
	public void setRenderedUnit(String unit){
		this.data.setRenderedUnit(UnitUtil.parseLocal(unit));
	}

	@JsonGetter("chartColour")
	public String getChartColour(){
		return this.data.getChartColour();
	}
	@JsonSetter("chartColour")
	public void setChartColour(String colour){
		this.data.setChartColour(colour);
	}

	@JsonGetter("plotType")
	public String getPlotType(){
		return DataPointVO.PLOT_TYPE_CODES.getCode(this.data.getPlotType());
	}
	@JsonSetter("plotType")
	public void setPlotType(String plotType){
		this.data.setPlotType(DataPointVO.PLOT_TYPE_CODES.getId(plotType));
	}
	
	public BaseTextRendererModel<?> getTextRenderer(){
		return this.textRenderer;
	}
	
	public void setTextRenderer(BaseTextRendererModel<?> renderer){
		this.textRenderer = renderer;
		TextRendererFactory.updateDataPoint(this.data, renderer);
	}
	
	public BaseChartRendererModel<?> getChartRenderer(){
		return this.chartRenderer;
	}
	
	public void setChartRenderer(BaseChartRendererModel<?> renderer){
		this.chartRenderer = renderer;
		ChartRendererFactory.updateDataPoint(this.data, renderer);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.model.AbstractRestModel#validate(com.serotonin.m2m2.i18n.ProcessResult)
	 */
	//@Override
	public void validate(ProcessResult response) {
		this.data.validate(response);
	}
	
	
	/**
	 * Ensure all Complex properties are set in the Data Point prior to returning
	 */
	@Override
	public DataPointVO getData(){
		
		if(templateXid != null){
			BaseTemplateVO<?> template = TemplateDao.instance.getByXid(templateXid);
			if(template != null)
				this.data.setTemplateId(template.getId());
			
		}

		return this.data;
	}
}
