/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.math3.complex.Complex;
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
	private boolean returnFrequency;
	/**
	 * 
	 * @param vo
	 * @param from
	 * @param to
	 * @param returnFreqency - Return data as Period (s) or Frequency (Hz)
	 */
	public PointValueFftCalculator(DataPointVO vo, long from, long to, boolean returnFreqency){
		this.vo = vo;
		this.from = from;
		this.to = to;
		this.returnFrequency = returnFreqency;
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
		
		/**
		 * Depending on if fftData.length is even or odd we need to pull out 
		 * the values of interest.
		 * 
		 * fftData[0] is the steady state Real Value
		 * 
		 * if length is even
		 *  fftData[2*i] = Re[i], 0<=i<length/2
		 *  fftData[2*i+1] = Im[i], 0<i<length/2 
		 *  fftData[1] = Re[n/2]
		 * 
		 * if length is odd 
		 *  fftData[2*i] = Re[i], 0<=i<(length+1)/2 
		 *  fftData[2*i+1] = Im[i], 0<i<(length-1)/2
		 *  fftData[1] = Im[(length-1)/2]
		 * 
		 */
		//Output The Real Steady State Mangitude
		try {
			jgen.writeStartObject();
			jgen.writeNumberField("value", fftData[0]); //Amplitude
			double frequency = 0;
			jgen.writeNumberField("frequency", frequency);
			jgen.writeEndObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double realComponent, imaginaryComponent, frequency;
		
		if(fftData.length % 2 == 0){
			for(int i=2; i<fftData.length/2; i++){
				try {
					realComponent = fftData[i*2];
					imaginaryComponent = fftData[2*i+1];
					Complex c = new Complex(realComponent, imaginaryComponent);
					jgen.writeStartObject();
					jgen.writeNumberField("value", c.abs()); //Amplitude
					
					if(this.returnFrequency)
						frequency = (double)i * sampleRateHz / dataLength;
					else
						frequency = 1d/((double)i * sampleRateHz / dataLength);
					
					jgen.writeNumberField("frequency", frequency);
					jgen.writeEndObject();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			
			for(int i=2; i<(fftData.length-1)/2; i++){
				try {
					realComponent = fftData[i*2];
					imaginaryComponent = fftData[2*i+1];
					Complex c = new Complex(realComponent, imaginaryComponent);
					jgen.writeStartObject();
					jgen.writeNumberField("value", c.abs()); //Amplitude
					if(this.returnFrequency)
						frequency = (double)i * sampleRateHz / dataLength;
					else
						frequency = 1d/((double)i * sampleRateHz / dataLength);

					jgen.writeNumberField("frequency", frequency);
					jgen.writeEndObject();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Write the last value out as it isn't in order in the array
			try {
				realComponent = fftData[fftData.length/2];
				imaginaryComponent = fftData[1];
				Complex c = new Complex(realComponent, imaginaryComponent);
				jgen.writeStartObject();
				jgen.writeNumberField("value", c.abs()); //Amplitude

				if(this.returnFrequency)
					frequency = (double)(((fftData.length-1)/2)-1) * sampleRateHz / dataLength;
				else
					frequency = 1d/((double)(((fftData.length-1)/2)-1) * sampleRateHz / dataLength);

				jgen.writeNumberField("frequency", frequency);
				jgen.writeEndObject();
			} catch (Exception e) {
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
