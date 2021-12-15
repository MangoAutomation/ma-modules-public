/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.mailingList;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.infiniteautomation.mango.scheduling.util.DailySchedule;
import com.infiniteautomation.mango.scheduling.util.WeeklySchedule;
import com.serotonin.m2m2.vo.mailingList.MailingList;

/**
 * 
 * Since the model will convert the value to a VO and back
 * these tests assume that if we send in a WeeklySchedule, it 
 * will be converted to a list of inactive times and then back
 * to a weekly schedule that should be identical.
 * 
 * Schedules that cross midnight must start the next day 
 * at 00:00 
 * 
 * 
 * @author Terry Packer
 *
 */
public class MailingListModelTest {
    
    @Test
    public void testFirstFifteen() {
        MailingListModel model = new MailingListModel();
        
        WeeklySchedule inactiveSchedule = new WeeklySchedule(); 
        inactiveSchedule.addDay(createDailySchedule()); //Sunday
        inactiveSchedule.addDay(createDailySchedule("00:00", "00:15"));
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule()); //Saturday
        
        model.setInactiveSchedule(inactiveSchedule);

        MailingList list = model.toVO();
        
        MailingListModel result = new MailingListModel(list);
        assertSchedules(inactiveSchedule, result.getInactiveSchedule());
    }
    
    @Test
    public void testEmptySchedule() {
        MailingListModel model = new MailingListModel();
        
        WeeklySchedule inactiveSchedule = new WeeklySchedule(); 
        inactiveSchedule.addDay(createDailySchedule()); //Sunday
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule()); //Saturday
        
        model.setInactiveSchedule(inactiveSchedule);

        MailingList list = model.toVO();
        
        MailingListModel result = new MailingListModel(list);
        assertSchedules(inactiveSchedule, result.getInactiveSchedule());
    }
    
    @Test
    public void testFullSchedule() {
        MailingListModel model = new MailingListModel();
        
        WeeklySchedule inactiveSchedule = new WeeklySchedule(); 
        inactiveSchedule.addDay(createDailySchedule("00:00")); //Sunday
        inactiveSchedule.addDay(createDailySchedule("00:00"));
        inactiveSchedule.addDay(createDailySchedule("00:00"));
        inactiveSchedule.addDay(createDailySchedule("00:00"));
        inactiveSchedule.addDay(createDailySchedule("00:00"));
        inactiveSchedule.addDay(createDailySchedule("00:00"));
        inactiveSchedule.addDay(createDailySchedule("00:00"));
        
        model.setInactiveSchedule(inactiveSchedule);

        MailingList list = model.toVO();
        
        MailingListModel result = new MailingListModel(list);
        assertSchedules(inactiveSchedule, result.getInactiveSchedule());
    }
    
    @Test
    public void testScheduleThroughMidnight() {
        MailingListModel model = new MailingListModel();
        
        WeeklySchedule inactiveSchedule = new WeeklySchedule(); 
        inactiveSchedule.addDay(createDailySchedule("08:00", "10:00", "13:00")); //Sunday
        inactiveSchedule.addDay(createDailySchedule("00:00", "05:00", "13:00"));
        inactiveSchedule.addDay(createDailySchedule("00:00", "06:00", "07:00"));
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule("07:00", "14:00", "15:00"));
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule("08:00", "17:00")); //Saturday
        
        model.setInactiveSchedule(inactiveSchedule);

        MailingList list = model.toVO();
        
        MailingListModel result = new MailingListModel(list);
        assertSchedules(inactiveSchedule, result.getInactiveSchedule());
    }
    
    @Test
    public void testRandomSchedule() {
        MailingListModel model = new MailingListModel();
        
        WeeklySchedule inactiveSchedule = new WeeklySchedule(); 
        inactiveSchedule.addDay(createDailySchedule("08:00", "10:00", "13:00")); //Sunday
        inactiveSchedule.addDay(createDailySchedule("05:00", "13:00"));
        inactiveSchedule.addDay(createDailySchedule("00:00", "06:00", "07:00"));
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule("07:00", "14:00", "15:00"));
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule("08:00", "17:00")); //Saturday
        
        model.setInactiveSchedule(inactiveSchedule);

        MailingList list = model.toVO();
        
        MailingListModel result = new MailingListModel(list);
        assertSchedules(inactiveSchedule, result.getInactiveSchedule());
    }
    
    @Test
    public void testRandomSchedule2() {
        MailingListModel model = new MailingListModel();
        
        WeeklySchedule inactiveSchedule = new WeeklySchedule(); 
        inactiveSchedule.addDay(createDailySchedule("08:00", "10:00", "13:00")); //Sunday
        inactiveSchedule.addDay(createDailySchedule("09:00", "12:00", "15:00"));
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule("07:00", "14:00", "15:00"));
        inactiveSchedule.addDay(createDailySchedule());
        inactiveSchedule.addDay(createDailySchedule("08:00", "17:00")); //Saturday
        
        model.setInactiveSchedule(inactiveSchedule);

        MailingList list = model.toVO();
        
        //Compare the tree sets
        
        MailingListModel result = new MailingListModel(list);
        assertSchedules(inactiveSchedule, result.getInactiveSchedule());
    }

    /**
     * Test to create a schedule of all sizes and slide it through 
     * the week.  This is not a comprehensive test of all possible 
     * combinations of intervals.
     */
    @Test
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
                MailingList list = new MailingList();
                list.setInactiveIntervals(inactive);
                MailingListModel model = new MailingListModel(list);
                MailingList actual = model.toVO();
                assertIntervals(inactive, actual.getInactiveIntervals() == null ? new TreeSet<>() : actual.getInactiveIntervals());
            }
        }
    }  
    
    /**
     */
    private void assertIntervals(Set<Integer> expectedIntervals, Set<Integer> actualIntervals) {
        assertEquals(expectedIntervals.size(), actualIntervals.size());
        Iterator<Integer> expectedIt = expectedIntervals.iterator();
        Iterator<Integer> actualIt = expectedIntervals.iterator();
        while(expectedIt.hasNext()) {
            assertEquals(expectedIt.next(), actualIt.next());
        }
        
    }

    private void assertSchedules(WeeklySchedule expected, WeeklySchedule actual) {
        for(int i=0; i<expected.getDailySchedules().size(); i++) {
            DailySchedule expectedDay = expected.getDailySchedules().get(i);
            
            //Since the model does not pre-populate a daily schedule with an empty list
            if(expectedDay.getChanges().size() == 0)
                continue;
            DailySchedule actualDay = actual.getDailySchedules().get(i);
            assertEquals(expectedDay.getChanges().size(), actualDay.getChanges().size());
            for(int j=0; j<expectedDay.getChanges().size(); j++) {
                assertEquals(expectedDay.getChanges().get(j), actualDay.getChanges().get(j));
            }
        }
    }
    
    private DailySchedule createDailySchedule(String... changes) {
        DailySchedule day = new DailySchedule();
        for(String change : changes)
            day.addChange(change);
        return day;
    }

}
