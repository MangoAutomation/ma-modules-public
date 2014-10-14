/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StartsAndRuntimeList;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

/**
 * @author Terry Packer
 *
 */
public class StatisticsCalculator implements MappedRowCallback<PointValueTime>{

	private StatisticsGenerator stats;
	private int dataTypeId;
	
	/**
	 * @param jgen
	 * @param dataTypeId
	 */
	public StatisticsCalculator(int dataTypeId, long from, long to) {
		this.dataTypeId = dataTypeId;
		
		switch(dataTypeId){
			case DataTypes.BINARY:
			case DataTypes.MULTISTATE:
				this.stats = new StartsAndRuntimeList(from, to, null);
			break;
			case DataTypes.ALPHANUMERIC:
				this.stats = new ValueChangeCounter(from, to, null);
			break;
			case DataTypes.NUMERIC:
				this.stats = new AnalogStatistics(from, to, null);
			break;
			default:
				throw new ShouldNeverHappenException("Invalid Data Type: "+ dataTypeId);
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(PointValueTime pvt, int index) {
		this.stats.addValueTime(pvt);
	}

	/**
	 * Signal we are finished
	 */
	public void done(JsonGenerator jgen){
		this.stats.done(null);
		
	       switch(this.dataTypeId){
				case DataTypes.BINARY:
				case DataTypes.MULTISTATE:
	                // Runtime stats
	                StartsAndRuntimeList stats = (StartsAndRuntimeList)this.stats;
	                StartsAndRuntimeListModel model = new StartsAndRuntimeListModel();
//	                if(startVT == null)
//		            	startVT = values.get(0);
//	                model.setStartPoint(new PointValueTimeModel(startVT));
//	                if(endVT == null)
//		            	endVT = values.get(values.size()-1);
//	                model.setEndPoint(new PointValueTimeModel(endVT));
//	                List<StartsAndRuntimeModel> srtModels = new ArrayList<StartsAndRuntimeModel>(stats.getData().size());
//	                for(StartsAndRuntime srt : stats.getData()){
//	                	srtModels.add(new StartsAndRuntimeModel(srt));
//	                }
//	               model.setStartsAndRuntime(srtModels);
//	               model.setHasData(true);
	               //TODO Write out Model
	               return;
				case DataTypes.NUMERIC:
	                AnalogStatistics analogStats = (AnalogStatistics)this.stats;
	                AnalogStatisticsModel analogModel = new AnalogStatisticsModel();
//		            if(startVT == null)
//		            	startVT = values.get(0);
//	                analogModel.setStartPoint(new PointValueTimeModel(startVT));
//	                if(endVT == null)
//		            	endVT = values.get(values.size()-1);
//	                analogModel.setEndPoint(new PointValueTimeModel(endVT));
//	                PointValueTimeModel minimum = new PointValueTimeModel(
//	                		new PointValueTime(
//	                				new NumericValue(analogStats.getMinimumValue()),
//	                				analogStats.getMinimumTime()));
//	                analogModel.setMinimum(minimum);
//	                PointValueTimeModel maximum = new PointValueTimeModel(
//	                		new PointValueTime(
//	                				new NumericValue(analogStats.getMaximumValue()),
//	                				analogStats.getMaximumTime()));
//	                analogModel.setMaximum(maximum);
//	                PointValueTimeModel first = new PointValueTimeModel(
//	                		new PointValueTime(
//	                				new NumericValue(analogStats.getFirstValue()),
//	                				analogStats.getFirstTime()));
//	                analogModel.setFirst(first);
//	                PointValueTimeModel last = new PointValueTimeModel(
//	                		new PointValueTime(
//	                				new NumericValue(analogStats.getLastValue()),
//	                				analogStats.getLastTime()));
//	                analogModel.setLast(last);
//	                analogModel.setAverage(analogStats.getAverage());
//	                analogModel.setSum(analogStats.getSum());
//	                analogModel.setCount(analogStats.getCount());
//	                analogModel.setIntegral(analogStats.getIntegral());
//	                analogModel.setHasData(true);
	                return;
				case DataTypes.ALPHANUMERIC:
	                ValueChangeCounter vcStats = (ValueChangeCounter)this.stats;
	                ValueChangeStatisticsModel vcModel = new ValueChangeStatisticsModel();
//	                if(startVT == null)
//		            	startVT = values.get(0);
//	                vcModel.setStartPoint(new PointValueTimeModel(startVT));
//	                if(endVT == null)
//		            	endVT = values.get(values.size()-1);
//	                vcModel.setEndPoint(new PointValueTimeModel(endVT));
//	                vcModel.setChanges(vcStats.getChanges());
//	                vcModel.setHasData(true);
	                return;
				default:
					return;
			}
		
	}
	
}
