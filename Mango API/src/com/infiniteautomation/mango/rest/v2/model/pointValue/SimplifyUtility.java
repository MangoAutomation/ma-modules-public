/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.util.Arrays;
import java.util.List;

import com.goebl.simplify.NullValueException;
import com.goebl.simplify.Simplify;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.AbstractRollupValueTime;
import com.serotonin.log.LogStopWatch;

/**
 *
 * @author Terry Packer
 */
public class SimplifyUtility {

    
    /**
     * Simplify according to our requirements
     * 
     * TODO This currently only works for Numeric Points
     * 
     * @param list
     * @return
     */
    public static List<DataPointVOPointValueTimeBookend> simplify(
            Double simplifyTolerance,
            Integer simplifyTarget,
            boolean simplifyHighQuality,
            List<DataPointVOPointValueTimeBookend> list) {
        LogStopWatch logStopWatch = new LogStopWatch();
        if(simplifyTolerance != null) {
            //TODO improve Simplify code to return a list
            Simplify<DataPointVOPointValueTimeBookend> simplify = new Simplify<DataPointVOPointValueTimeBookend>(new DataPointVOPointValueTimeBookend[0], SimplifyPointValueExtractor.extractor);
            DataPointVOPointValueTimeBookend[] simplified = simplify.simplify(list.toArray(new DataPointVOPointValueTimeBookend[list.size()]), simplifyTolerance, simplifyHighQuality);
            logStopWatch.stop("Finished Simplify, tolerance: " + simplifyTolerance);
            return Arrays.asList(simplified);
        }else {
            if(list.size() < simplifyTarget)
                return list;
            
            //Compute target bounds as 10% of target
            int lowerTarget = simplifyTarget - (int)(simplifyTarget * 0.1);
            int upperTarget = simplifyTarget + (int)(simplifyTarget * 0.1);
            
            //Compute tolerance bounds and initial tolerance
            Double max = Double.MIN_VALUE;
            Double min = Double.MAX_VALUE;
            for(DataPointVOPointValueTimeBookend value : list) {
                if(value.getPvt().getDoubleValue() > max)
                    max = value.getPvt().getDoubleValue();
                if(value.getPvt().getDoubleValue() < min)
                    min = value.getPvt().getDoubleValue();
            }
            double difference = max - min;
            double tolerance = difference / 20d;
            double topBound = difference;
            double bottomBound = 0;
            
            //Determine max iterations we can allow
            int maxIterations = 100;
            int iteration = 1;
            
            Simplify<DataPointVOPointValueTimeBookend> simplify = new Simplify<DataPointVOPointValueTimeBookend>(new DataPointVOPointValueTimeBookend[0], SimplifyPointValueExtractor.extractor);
            DataPointVOPointValueTimeBookend[] simplified = simplify.simplify(list.toArray(new DataPointVOPointValueTimeBookend[list.size()]), tolerance, simplifyHighQuality);
            DataPointVOPointValueTimeBookend[] best = simplified;
            while(simplified.length < lowerTarget || simplified.length > upperTarget) {
                
                if (simplified.length > simplifyTarget) {
                    bottomBound = tolerance;
                } else {
                    topBound = tolerance;
                }
                
                //Adjust tolerance
                tolerance = bottomBound + (topBound - bottomBound) / 2.0d;
                simplify = new Simplify<DataPointVOPointValueTimeBookend>(new DataPointVOPointValueTimeBookend[0], SimplifyPointValueExtractor.extractor);
                simplified = simplify.simplify(list.toArray(new DataPointVOPointValueTimeBookend[list.size()]), tolerance, simplifyHighQuality);
                
                //Keep our best effort
                if(Math.abs(simplifyTarget - simplified.length) < Math.abs(simplifyTarget - best.length))
                    best = simplified;

                if(iteration > maxIterations) {
                    simplified = best;
                    break;
                }

                iteration++;
            }
            
            logStopWatch.stop("Finished Simplify, target: " + simplifyTarget + " actual " + simplified.length);
            return Arrays.asList(simplified);
        }
    }
    
    /**
     * Simplify according to our requirements
     * 
     * @param list
     * @return
     */
    public static List<AbstractRollupValueTime> simplifyRollup(
            Double simplifyTolerance,
            Integer simplifyTarget,
            boolean simplifyHighQuality,
            List<AbstractRollupValueTime> list) {
        LogStopWatch logStopWatch = new LogStopWatch();
        if(simplifyTolerance != null) {
            //TODO improve Simplify code to return a list
            Simplify<AbstractRollupValueTime> simplify = new Simplify<AbstractRollupValueTime>(new AbstractRollupValueTime[0]);
            AbstractRollupValueTime[] simplified = simplify.simplify(list.toArray(new AbstractRollupValueTime[list.size()]), simplifyTolerance, simplifyHighQuality);
            logStopWatch.stop("Finished Simplify, tolerance: " + simplifyTolerance);
            return Arrays.asList(simplified);
        }else {
            if(list.size() < simplifyTarget)
                return list;
            
            //Compute target bounds as 10% of target
            int lowerTarget = simplifyTarget - (int)(simplifyTarget * 0.1);
            int upperTarget = simplifyTarget + (int)(simplifyTarget * 0.1);
            
            //Compute tolerance bounds and initial tolerance
            Double max = Double.MIN_VALUE;
            Double min = Double.MAX_VALUE;
            for(AbstractRollupValueTime value : list) {
                try {
                    if(value.getY() > max)
                        max = value.getY();
                }catch(NullValueException e) { }
                try {
                    if(value.getY() < min)
                        min = value.getY();
                }catch(NullValueException e) { }
            }
            double difference = max - min;
            double tolerance = difference / 20d;
            double topBound = difference;
            double bottomBound = 0;
            
            //Determine max iterations we can allow
            int maxIterations = 100;
            int iteration = 1;
            
            Simplify<AbstractRollupValueTime> simplify = new Simplify<AbstractRollupValueTime>(new AbstractRollupValueTime[0]);
            AbstractRollupValueTime[] simplified = simplify.simplify(list.toArray(new AbstractRollupValueTime[list.size()]), tolerance, simplifyHighQuality);
            AbstractRollupValueTime[] best = simplified;
            while(simplified.length < lowerTarget || simplified.length > upperTarget) {
                
                if (simplified.length > simplifyTarget) {
                    bottomBound = tolerance;
                } else {
                    topBound = tolerance;
                }
                
                //Adjust tolerance
                tolerance = bottomBound + (topBound - bottomBound) / 2.0d;
                simplify = new Simplify<AbstractRollupValueTime>(new AbstractRollupValueTime[0]);
                simplified = simplify.simplify(list.toArray(new AbstractRollupValueTime[list.size()]), tolerance, simplifyHighQuality);
                
                //Keep our best effort
                if(Math.abs(simplifyTarget - simplified.length) < Math.abs(simplifyTarget - best.length))
                    best = simplified;

                if(iteration > maxIterations) {
                    simplified = best;
                    break;
                }

                iteration++;
            }
            
            logStopWatch.stop("Finished Simplify, target: " + simplifyTarget + " actual " + simplified.length);
            return Arrays.asList(simplified);
        }
    }
    
}
