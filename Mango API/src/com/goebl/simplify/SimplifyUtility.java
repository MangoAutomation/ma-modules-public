/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.goebl.simplify;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import com.infiniteautomation.mango.rest.pointextractor.IdentityPointExtractor;
import com.serotonin.log.LogStopWatch;

/**
 *
 * @author Terry Packer
 */
public class SimplifyUtility {

    public static <T extends Point> List<T> simplify(
            Double simplifyTolerance,
            Integer simplifyTarget,
            boolean simplifyHighQuality,
            boolean prePostProcess,
            List<T> list) {

        return simplify(simplifyTolerance, simplifyTarget, simplifyHighQuality, prePostProcess, list,
                IdentityPointExtractor.INSTANCE, Comparator.comparingDouble(Point::getX));
    }

    /**
     * Simplify according to the requirements.  Either reduce the list to with 10% of
     *  desired target via Newton's Method or use a tolerance in one go. Optionally pre/post
     *  process the data to remove !Point.isProcessable() values and add them back in.
     *
     */
    public static <T> List<T> simplify(
            Double simplifyTolerance,
            Integer simplifyTarget,
            boolean simplifyHighQuality,
            boolean prePostProcess,
            List<T> list,
            PointExtractor<? super T> extractor,
            Comparator<? super T> comparator) {
        LogStopWatch logStopWatch = new LogStopWatch();

        //PreProcess by removing all invalid values (to add back in at the end)
        List<T> unprocessable = new ArrayList<>();
        if(prePostProcess) {
            ListIterator<T> it = list.listIterator();
            while(it.hasNext()) {
                T value = it.next();
                boolean processable;
                try {
                    double x = extractor.getX(value);
                    double y = extractor.getY(value);
                    processable = !Double.isNaN(x) && !Double.isNaN(y) &&
                            !Double.isInfinite(x) && !Double.isInfinite(y);
                } catch (Exception e) {
                    processable = false;
                }

                if(!processable) {
                    unprocessable.add(value);
                    it.remove();
                }
            }
        }
        List<T> simplified;
        Simplify<T> simplify = new Simplify<>(extractor);
        if(simplifyTolerance != null) {
            simplified = simplify.simplify(list, simplifyTolerance, simplifyHighQuality);
        }else {
            if(list.size() > simplifyTarget) {
                //Compute target bounds as 10% of target
                int lowerTarget = simplifyTarget - (int)(simplifyTarget * 0.1);
                int upperTarget = simplifyTarget + (int)(simplifyTarget * 0.1);

                //Compute tolerance bounds and initial tolerance
                double max = Double.MIN_VALUE;
                double min = Double.MAX_VALUE;
                for(T value : list) {
                    double y = extractor.getY(value);
                    max = Math.max(max, y);
                    min = Math.min(min, y);
                }
                double difference = max - min;
                double tolerance = difference / 20d;
                double topBound = difference;
                double bottomBound = 0;

                //Determine max iterations we can allow
                int maxIterations = 100;
                int iteration = 1;

                simplified = simplify.simplify(list, tolerance, simplifyHighQuality);
                List<T> best = simplified;
                while(simplified.size() < lowerTarget || simplified.size() > upperTarget) {

                    if (simplified.size() > simplifyTarget) {
                        bottomBound = tolerance;
                    } else {
                        topBound = tolerance;
                    }

                    //Adjust tolerance
                    tolerance = bottomBound + (topBound - bottomBound) / 2.0d;
                    simplified = simplify.simplify(list, tolerance, simplifyHighQuality);

                    //Keep our best effort
                    if(Math.abs(simplifyTarget - simplified.size()) < Math.abs(simplifyTarget - best.size()))
                        best = simplified;

                    if(iteration > maxIterations) {
                        simplified = best;
                        break;
                    }

                    iteration++;
                }
            }else {
                simplified = list;
            }
            if(prePostProcess) {
                //Trim out the unprocessable data if it makes our target list size too big
                int toTrim = (unprocessable.size() + simplified.size()) - simplifyTarget;
                if(toTrim > 0) {
                    int toTrimIndex;
                    if(toTrim > unprocessable.size())
                        toTrimIndex = unprocessable.size();
                    else
                        toTrimIndex = unprocessable.size() - toTrim;
                    unprocessable = unprocessable.subList(0, toTrimIndex);
                }
            }
        }

        //Post Process, add back in values
        if(prePostProcess) {
            simplified.addAll(unprocessable);
            simplified.sort(comparator);
        }

        if(simplifyTolerance != null)
            logStopWatch.stop(() -> "Finished Simplify, tolerance: " + simplifyTolerance);
        else {
            int size = simplified.size();
            logStopWatch.stop(() -> "Finished Simplify, target: " + simplifyTarget + " actual " + size);
        }
        return simplified;
    }

}
