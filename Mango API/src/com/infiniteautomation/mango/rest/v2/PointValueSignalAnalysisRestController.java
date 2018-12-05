/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.math3.complex.Complex;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeCacheControl;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.quantize2.FftGenerator;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 *
 * @author Terry Packer
 */
@Api(value="Point Value Signal Analysis", description="Signal processing tools for point data")
@RestController
@RequestMapping("/point-value-analysis")
public class PointValueSignalAnalysisRestController {

    //TODO Cross Ambiguity Function for multiple points
    //TODO Cross Correlation of 2 points
    //TODO Highpass filter
    //TODO Lowpass filter
    //TODO Bandpass filter
    
    @ApiOperation(
            value = "Perform the FFT on a data point's values for the given time range", 
            notes = "From time inclusive, To time exclusive. Numeric,Multistate,Binary types supported.",
            response = FftValue.class, 
            responseContainer = "Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/fft/{xid}")
    public ResponseEntity<List<FftValue>> FFT(
            HttpServletRequest request,
            @ApiParam(value = "Point xid", required = true, allowMultiple = false) 
            @PathVariable String xid,
    
            @ApiParam(value = "From time", required = false, allowMultiple = false)
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime from,

            @ApiParam(value = "To time", required = false, allowMultiple = false) 
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime to,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,
            
            @ApiParam(value = "Point poll period ms, if not supplied will be autodected", required = false, allowMultiple = false) 
            @RequestParam(value = "pollPeriod", required = false) 
            Long pollPeriod,
            
            @ApiParam(value = "Limit (not including bookend values)", required = false, allowMultiple = false) 
            @RequestParam(value = "limit", required = false) 
            Integer limit,
            
            @AuthenticationPrincipal User user
            ) {
        
        DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
        if (vo == null) {
            throw new NotFoundRestException();
        }else {
            if(!Permissions.hasDataPointReadPermission(user, vo))
                throw new AccessDeniedException();
        }
        
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(
                from, to, null, timezone, RollupEnum.NONE, null, limit, 
                true, false, true, PointValueTimeCacheControl.NONE, null, null, false, null);
        FftGenerator generator = buildFFT(vo, info);
        
        double sampleRateHz;
        if(pollPeriod != null)
           sampleRateHz = 1000d / pollPeriod;
        else
           sampleRateHz = 1000d / generator.getAverageSamplePeriodMs();
        
        if(generator.getValues().length == 0)
            return ResponseEntity.ok(new ArrayList<>());
        else
            return ResponseEntity.ok(generate(generator, sampleRateHz, true));
    }
    
    @ApiOperation(
            value = "Perform the IFFT on a data point's values for the given time range", 
            notes = "From time inclusive, To time exclusive. Numeric,Multistate,Binary types supported.",
            response = FftValue.class, 
            responseContainer = "Array"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/ifft/{xid}")
    public ResponseEntity<List<FftValue>> inverseFFT(
            HttpServletRequest request,
            @ApiParam(value = "Point xid", required = true, allowMultiple = false) 
            @PathVariable String xid,
    
            @ApiParam(value = "From time", required = false, allowMultiple = false)
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime from,

            @ApiParam(value = "To time", required = false, allowMultiple = false) 
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME) 
            ZonedDateTime to,

            @ApiParam(value = "Time zone", required = false, allowMultiple = false) 
            @RequestParam(value = "timezone", required = false) 
            String timezone,
            
            @ApiParam(value = "Point poll period ms, if not supplied will be autodected", required = false, allowMultiple = false) 
            @RequestParam(value = "pollPeriod", required = false) 
            Long pollPeriod,
            
            @ApiParam(value = "Limit (not including bookend values)", required = false, allowMultiple = false) 
            @RequestParam(value = "limit", required = false) 
            Integer limit,
            
            @AuthenticationPrincipal User user
            ) {
        
        DataPointVO vo = DataPointDao.getInstance().getByXid(xid);
        if (vo == null) {
            throw new NotFoundRestException();
        }else {
            if(!Permissions.hasDataPointReadPermission(user, vo))
                throw new AccessDeniedException();
        }
        
        ZonedDateTimeRangeQueryInfo info = new ZonedDateTimeRangeQueryInfo(
                from, to, null, timezone, RollupEnum.NONE, null, limit, 
                true, false, true, PointValueTimeCacheControl.NONE, null, null, false, null);
        FftGenerator generator = buildFFT(vo, info);
        
        double sampleRateHz;
        if(pollPeriod != null)
           sampleRateHz = 1000d / pollPeriod;
        else
           sampleRateHz = 1000d / generator.getAverageSamplePeriodMs();
        
        if(generator.getValues().length == 0)
            return ResponseEntity.ok(new ArrayList<>());
        else
            return ResponseEntity.ok(generate(generator, sampleRateHz, false));
    }
    
    /**
     * Fill the FFT generator with data
     * @param vo
     * @param info
     * @param pollPeriod
     * @return
     */
    protected FftGenerator buildFFT(DataPointVO vo, ZonedDateTimeRangeQueryInfo info){
        PointValueDao pvd = Common.databaseProxy.newPointValueDao();
        List<PointValueTime> data = new ArrayList<>();
        //Make the call to get the data and quantize it
        pvd.getPointValuesBetween(vo.getId(), info.getFromMillis(), info.getToMillis(),
                new MappedRowCallback<PointValueTime>() {
                    @Override
                    public void row(PointValueTime pvt, int row) {
                        data.add(pvt);
                    }
                });
        FftGenerator generator = new FftGenerator(data.size());
        for(PointValueTime pvt : data)
            generator.data(pvt);
        return generator;
    }
    /**
     * 
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
     * @param generator
     * @param sampleRateHz
     * @param fft
     * @return
     */
    protected List<FftValue> generate(FftGenerator generator, double sampleRateHz, boolean fft) {
        if(fft)
            generator.fft();
        else
            generator.ifft();
        
        double[] fftData = generator.getValues();
        double dataLength = (double)fftData.length;
        List<FftValue> values = new ArrayList<>(fftData.length);
        
        //Output steady state magnitude
        FftValue ss = new FftValue();
        ss.setValue(fftData[0]);
        ss.setFrequency(0);
        ss.setPeriod(0);
        values.add(ss);
        double realComponent, imaginaryComponent;
        
        if(fftData.length % 2 == 0){
            for(int i=2; i<fftData.length/2; i++){
                realComponent = fftData[i*2];
                imaginaryComponent = fftData[2*i+1];
                Complex c = new Complex(realComponent, imaginaryComponent);
                FftValue value = new FftValue();
                value.setValue(c.abs());
                value.setFrequency((double)i * sampleRateHz / dataLength);
                value.setPeriod(1d/((double)i * sampleRateHz / dataLength));
                values.add(value);
            }
        }else{
            for(int i=2; i<(fftData.length-1)/2; i++){
                    realComponent = fftData[i*2];
                    imaginaryComponent = fftData[2*i+1];
                    Complex c = new Complex(realComponent, imaginaryComponent);
                    FftValue value = new FftValue();
                    value.setValue(c.abs());
                    value.setFrequency((double)i * sampleRateHz / dataLength);
                    value.setPeriod(1d/((double)i * sampleRateHz / dataLength));
                    values.add(value);
            }
            //Write the last value out as it isn't in order in the array

                realComponent = fftData[fftData.length/2];
                imaginaryComponent = fftData[1];
                Complex c = new Complex(realComponent, imaginaryComponent);
                FftValue value = new FftValue();
                value.setValue(c.abs());
                value.setFrequency((double)(((fftData.length-1)/2)-1) * sampleRateHz / dataLength);
                value.setPeriod(1d/((double)(((fftData.length-1)/2)-1) * sampleRateHz / dataLength));
                values.add(value);
        }
        return values;
    }
    
    public class FftValue {
        private double frequency;
        private double period;
        private double value;
        public double getFrequency() {
            return frequency;
        }
        public void setFrequency(double frequency) {
            this.frequency = frequency;
        }
        public double getPeriod() {
            return period;
        }
        public void setPeriod(double period) {
            this.period = period;
        }
        public double getValue() {
            return value;
        }
        public void setValue(double value) {
            this.value = value;
        }
    }
}
