/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.mailingList;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.infiniteautomation.mango.scheduling.util.DailySchedule;
import com.infiniteautomation.mango.scheduling.util.WeeklySchedule;
import com.serotonin.m2m2.vo.mailingList.MailingList;

/**
 * Schedules that cross midnight must start the next day 
 * at 00:00 
 * 
 * 
 * @author Terry Packer
 *
 */
public class MailingListModelTest {
    
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
        
        //Compare the tree sets
        
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

    
    private void assertSchedules(WeeklySchedule expected, WeeklySchedule actual) {
        for(int i=0; i<expected.getDailySchedules().size(); i++) {
            DailySchedule expectedDay = expected.getDailySchedules().get(i);
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
