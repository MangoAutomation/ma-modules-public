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

import com.infiniteautomation.mango.scheduling.util.DailySchedule;
import com.infiniteautomation.mango.scheduling.util.ScheduleUtils;
import com.infiniteautomation.mango.scheduling.util.TimeValue;
import com.infiniteautomation.mango.scheduling.util.WeeklySchedule;

/**
 * @author Terry Packer
 *
 */
public class InactiveIintervalWithScheduleConversion {

    /**
     * Test to create a schedule of all sizes and slide it through
     * the week.  This is not a comprehensive test of all possible
     * combinations of intervals.
     */

    public void testSomeIntervals() {
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
                if(startPos == 575 && length == 2)
                    System.out.println("found it");
                System.out.println("Testing from " + startPos + " to " + length);
                //Convert to Weekly schedule
                WeeklySchedule schedule = getInactiveIntervalsAsWeeklySchedule(inactive);
                Set<Integer> actual = weeklyScheduleToInactiveIntervals(schedule);
                assertIntervals(inactive, actual == null ? new TreeSet<>() : actual);
            }
        }
    }
    
    @Test
    public void testGappedIntervals() {
        for(int length=0; length<672; length++) {
            for(int startPos=671; startPos>=0; startPos--) {
                TreeSet<Integer> inactive = new TreeSet<>();
                for(int gapStart=0; gapStart < 672; gapStart++) {
                    for(int gapLength = 0; gapLength < 672; gapLength++) {
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
                        if(startPos == 575 && length == 10 && gapLength == 2)
                            System.out.println("found it");
                        //System.out.println("Testing " + startPos + " to " + length + " with gap size " + gapLength + " starting at " + gapStart);
                        //Convert to Weekly schedule
                        WeeklySchedule schedule = getInactiveIntervalsAsWeeklySchedule(inactive);
                        Set<Integer> actual = weeklyScheduleToInactiveIntervals(schedule);
                        assertIntervals(inactive, actual == null ? new TreeSet<>() : actual);
                    }
                }
            }
        }
    }
    
    public WeeklySchedule getInactiveIntervalsAsWeeklySchedule(Set<Integer> inactiveIntervals) {
        if(inactiveIntervals == null)
            return null;
        
        WeeklySchedule weeklySchedule = new WeeklySchedule();
        for(int k = 0; k < 7; k+=1) {
            weeklySchedule.addDay(new DailySchedule());
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
                int minute15 = (last+1) % (4*24); //At the end of the 15 minute period
                int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
                int hr = (minute15 * 15) / 60;
                int min = (minute15 * 15) % 60;
                weeklySchedule.getDailySchedules().get(dayIndex).addChange(String.format("%02d:%02d", hr, min));
                last = -2;
            }
            
            if(last != -2) {
                int minute15 = (last+1) % (4*24); //At the end of the 15 minute period
                int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
                int hr = (minute15 * 15) / 60;
                int min = (minute15 * 15) % 60;
                weeklySchedule.getDailySchedules().get(lastDayIndex).addChange(String.format("%02d:%02d", hr, min));
            }
            
            last = i.intValue();
            int minute15 = i.intValue() % (4*24);
            int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
            int hr = (minute15 * 15) / 60;
            int min = (minute15 * 15) % 60;
            weeklySchedule.getDailySchedules().get(dayIndex).addChange(String.format("%02d:%02d", hr, min));
        }
        
        if(last != -2 && last % (4*24) != 95) {
            int dayIndex = (last+1) / (4*24);
            int minute15 = (last+1) % (4*24);
            int millisecondsInPeriod = minute15 * 15 * 60 * 1000;
            int hr = (minute15 * 15) / 60;
            int min = (minute15 * 15) % 60;
            weeklySchedule.getDailySchedules().get(dayIndex).addChange(String.format("%02d:%02d", hr, min));
        }
        
        //Re-Order putting Sunday first
        //Sunday is last in the list, place it first
        DailySchedule sunday = weeklySchedule.getDailySchedules().remove(6);
        weeklySchedule.getDailySchedules().add(0, sunday);
        
        return weeklySchedule;
    }
    
    public Set<Integer> weeklyScheduleToInactiveIntervals(WeeklySchedule weeklySchedule) {
        if(weeklySchedule == null)
            return null;
        
        //Modify a copy of the weekly schedule to put Monday first
        //TODO assert we have 7 days in the schedule
        List<DailySchedule> copy = new ArrayList<>(weeklySchedule.getDailySchedules());
        DailySchedule sunday = copy.remove(0);
        copy.add(copy.size(), sunday);
        
        Set<Integer> inactiveIntervals = new TreeSet<>();
        for(int k = 0; k < copy.size(); k+=1) {
            int baseInterval = k * 96 ; //milliseconds per day
            boolean deactivated = false;
            int lastInterval = -2;
            List<String> wsTimes = copy.get(k).getChanges();
            Integer[] times = new Integer[wsTimes.size()];
            int index = 0;
            for(String time : wsTimes) {
                TimeValue value = ScheduleUtils.parseTimeValue(time);
                //Compute offset from midnight
                times[index++] = (value.getHour() * 60 * 60 * 1000) + (value.getMinute() * 60 * 1000);
            }
            
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
