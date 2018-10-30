/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * @author Terry Packer
 *
 */
public class InactiveIntervalConversionTest {

    /**
     * Test to create a schedule of all sizes and slide it through
     * the week.  This is not a comprehensive test of all possible
     * combinations of intervals.
     */
    @Test
    public void testAllIntervals() {
        for(int length=0; length<672; length++) {
            for(int startPos=671; startPos>=0; startPos--) {
                TreeSet<Integer> inactive = new TreeSet<>();
                for(int k=0; k<length; k++) {
                    int actualLength;
                    if(startPos + k > 671) {
                        actualLength = 671-startPos;
                    }else {
                        actualLength = k;
                    }
                    inactive.add(startPos + actualLength);
                }
                //Convert to Weekly schedule
                List<Set<String>> schedule = getInactiveIntervalsAsWeeklySchedule(inactive);
                Set<Integer> actual = weeklyScheduleToInactiveIntervals(schedule);
                assertIntervals(inactive, actual == null ? new TreeSet<>() : actual);
            }
        }
    }
    

    /**
     * Very long running test so it doesn't run automatically
     */
    public void testGappedIntervals() {
        int maxBlockLength = 671;
        int maxGapLength = 671;
        for(int length=0; length<=maxGapLength; length++) {
            for(int startPos=671; startPos>=0; startPos--) {
                TreeSet<Integer> inactive = new TreeSet<>();
                for(int gapStart=0; gapStart < 672; gapStart++) {
                    for(int gapLength = 0; gapLength <= maxBlockLength; gapLength++) {
                        for(int k=0; k<length; k++) {
                            int actualLength;
                            if(startPos + k > 671) {
                                actualLength = 671-startPos;
                            }else {
                                actualLength = k;
                            }
                            int interval = startPos + actualLength;
                            if(interval >= gapStart && interval <= gapStart + gapLength)
                                continue;
                            inactive.add(interval);
                        }
                        //Convert to Weekly schedule
                        List<Set<String>> schedule = getInactiveIntervalsAsWeeklySchedule(inactive);
                        Set<Integer> actual = weeklyScheduleToInactiveIntervals(schedule);
                        assertIntervals(inactive, actual == null ? new TreeSet<>() : actual);
                    }
                }
            }
        }
    }
    
    public List<Set<String>> getInactiveIntervalsAsWeeklySchedule(Set<Integer> inactiveIntervals) {
        List<Set<String>> weeklySchedule = new ArrayList<Set<String>>(7);
        for(int k = 0; k < 7; k+=1) {
            weeklySchedule.add(k, new TreeSet<String>());
        }
        
        Integer[] inactive = new Integer[inactiveIntervals.size()];
        inactiveIntervals.toArray(inactive);
        Arrays.sort(inactive);
        
        int last = -2;
        for(Integer i : inactive) {
            if(i == null)
                continue;
            
            int dayIndex = i.intValue() / (4*24);
            int lastDayIndex;
            if(last != -2)
                lastDayIndex = last / (4*24);
            else
                lastDayIndex = dayIndex;
            
            if(last == i.intValue() - 1 && dayIndex == lastDayIndex) { //Still inactive
                last = i.intValue();
                continue;
            } else if (dayIndex != lastDayIndex) {
                if((last+1) % (4*24) != 0) {
                    int minute15 = (last+1) % (4*24); //At the end of the 15 minute period
                    int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
                    weeklySchedule.get(lastDayIndex).add(String.valueOf(millisecondsInPeriod));
                }
                last = -2;
            }
            
            if(last != -2) {
                int minute15 = (last+1) % (4*24); //At the end of the 15 minute period
                int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
                weeklySchedule.get(lastDayIndex).add(String.valueOf(millisecondsInPeriod));
            }
            
            last = i.intValue();
            int minute15 = i.intValue() % (4*24);
            int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
            weeklySchedule.get(dayIndex).add(String.valueOf(millisecondsInPeriod));
        }
        
        if(last != -2 && last % (4*24) != 95) {
            int dayIndex = (last+1) / (4*24);
            int minute15 = (last+1) % (4*24);
            int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
            weeklySchedule.get(dayIndex).add(String.valueOf(millisecondsInPeriod));
        }
        
        return weeklySchedule;
    }
    
    public Set<Integer> weeklyScheduleToInactiveIntervals(List<Set<String>> weeklySchedule) {
        Set<Integer> inactiveIntervals = new TreeSet<>();
        for(int k = 0; k < weeklySchedule.size(); k+=1) {
            int baseInterval = k * 96 ; //milliseconds per day
            boolean deactivated = false;
            int lastInterval = -2;
            Set<String> wsTimes = weeklySchedule.get(k);
            Integer[] times = new Integer[wsTimes.size()];
            int index = 0;
            for(String time : wsTimes)
                times[index++] = Integer.parseInt(time);
            
            Arrays.sort(times);
            for(Integer i : times) {
                int thisInterval = i / 900000; //millis in 15m
                if(deactivated) {
                    while(lastInterval < thisInterval) {
                        inactiveIntervals.add(baseInterval+lastInterval++);
                    }
                    deactivated = false;
                } else {
                    lastInterval = thisInterval;
                    deactivated = true;
                }
            }
            
            if(deactivated) {
                while(lastInterval < 96) {
                    inactiveIntervals.add(baseInterval+lastInterval++);
                }
            }
        }
        return inactiveIntervals;
    }
    
    /**
     * @param inactive
     * @param inactiveIntervals
     */
    private void assertIntervals(Set<Integer> expectedIntervals, Set<Integer> actualIntervals) {
        assertEquals(expectedIntervals.size(), actualIntervals.size());
        Iterator<Integer> expectedIt = expectedIntervals.iterator();
        Iterator<Integer> actualIt = expectedIntervals.iterator();
        while(expectedIt.hasNext()) {
            assertEquals(expectedIt.next(), actualIt.next());
        }
        
    }
}
