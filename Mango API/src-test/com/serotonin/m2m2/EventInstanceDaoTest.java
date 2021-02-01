/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;

import com.infiniteautomation.mango.spring.service.EventInstanceService;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.event.EventInstanceVO;

import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;

/**
 *
 * @author Terry Packer
 */
public class EventInstanceDaoTest extends MangoTestBase {

    /**
     * Ensure the extra fields property doesn't cause problems with JOOQ
     */
    @Test
    public void testJooq() {

        SystemEventType type = new SystemEventType(SystemEventType.TYPE_SYSTEM_STARTUP);
        long timestamp = Common.timer.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Common.eventManager.raiseEvent(type, timestamp, false, AlarmLevels.CRITICAL, new TranslatableMessage("common.default", "testing"), null);
            timestamp++;
        }


        ASTNode rql = new RQLParser().parse("lt(activeTs, " + timestamp + ")&limit(100");
        MutableInt count = new MutableInt();
        Common.getBean(EventInstanceService.class).customizedQuery(rql, (EventInstanceVO item) -> {
            count.increment();
        });

        assertEquals(100, (int) count.getValue());

    }

    @Override
    protected MockMangoLifecycle getLifecycle() {
        MockMangoLifecycle lifecycle = super.getLifecycle();
        lifecycle.setEventManager(new MockEventManager(true));
        return lifecycle;
    }

}
