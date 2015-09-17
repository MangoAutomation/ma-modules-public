/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.Collections;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.dao.DaoRegistry;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.quantize2.FftGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.pair.LongPair;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;

/**
 * @author Terry Packer
 *
 */
public class PointValueFftCalculator implements QueryArrayStream<PointValueTimeModel>{

	private DataPointVO vo;
	private long from;
	private long to;
	
	public PointValueFftCalculator(DataPointVO vo, long from, long to){
		this.vo = vo;
		this.from = from;
		this.to = to;
	}

	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeStream#streamData(java.io.Writer)
	 */
	@Override
	public void streamData(JsonGenerator jgen) {
		
		this.setupDates();
		DateTime startTime = new DateTime(from);
		DateTime endTime = new DateTime(to);
		
		FftGenerator generator = this.calculate(startTime, endTime);
		double[] fftData = generator.getValues();
		double sampleRateHz = 1000d / generator.getAverageSamplePeriodMs();
		double dataLength = (double)fftData.length;
		
		for(int i=0; i<fftData.length; i++){
			try {
				jgen.writeStartObject();
				jgen.writeNumberField("value", fftData[i]); //Amplitude
				double frequency = (double)i * sampleRateHz / dataLength;
				jgen.writeNumberField("frequency", frequency);
				jgen.writeEndObject();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream#streamData(com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter)
	 */
	@Override
	public void streamData(CSVPojoWriter<PointValueTimeModel> writer)
			throws IOException {
		this.setupDates();
		
		
	}
	
	/**
	 * Generate FFT
	 * @return
	 */
	public FftGenerator calculate(DateTime from, DateTime to){
		
		long count = DaoRegistry.pointValueDao.dateRangeCount(vo.getId(), from.getMillis(), to.getMillis());
	
		final FftGenerator generator = new FftGenerator(count);
        //Make the call to get the data and quantize it
        DaoRegistry.pointValueDao.getPointValuesBetween(vo.getId(), from.getMillis(), to.getMillis(),
                new MappedRowCallback<PointValueTime>() {
                    @Override
                    public void row(PointValueTime pvt, int row) {
                        generator.data(pvt);
                    }
                });
        
        generator.done(getEndValue());
        return generator;
	}
	
	/**
	 * Setup the dates based on available data
	 */
	private void setupDates(){
        // Determine the start and end times.
        if (from == -1) {
            // Get the start and end from the point values table.
            LongPair lp = DaoRegistry.pointValueDao.getStartAndEndTime(Collections.singletonList(vo.getId()));
            from = lp.getL1();
            to = lp.getL2();
        }

	}
	
	/**
	 * Get the value at to (assume that setup dates has been called)
	 * @return
	 */
	private PointValueTime getEndValue(){
		PointValueTime endPvt = DaoRegistry.pointValueDao.getPointValueAt(vo.getId(), to);
        return endPvt;
	}

}
