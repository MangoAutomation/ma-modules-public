/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.serotonin.m2m2.maintenanceEvents;

import java.util.List;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.db.AbstractTableDefinition;

/**
 *
 * @author Terry Packer
 */
@Component
public class MaintenanceEventsTableDefinition extends AbstractTableDefinition {

    Field<Integer> ALARM_LEVEL = DSL.field(DSL.name("alarmLevel"), SQLDataType.INTEGER.nullable(false));
    Field<Integer> SCHEDULE_TYPE = DSL.field(DSL.name("scheduleType"), SQLDataType.INTEGER.nullable(false));
    Field<String> DISABLED = DSL.field(DSL.name("disabled"), SQLDataType.CHAR(1).nullable(false));
    Field<Integer> ACTIVE_YEAR = DSL.field(DSL.name("activeYear"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> ACTIVE_MONTH = DSL.field(DSL.name("activeMonth"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> ACTIVE_DAY = DSL.field(DSL.name("activeDay"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> ACTIVE_HOUR = DSL.field(DSL.name("activeHour"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> ACTIVE_MINUTE = DSL.field(DSL.name("activeMinute"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> ACTIVE_SECOND = DSL.field(DSL.name("activeSecond"), SQLDataType.INTEGER.nullable(true));
    Field<String> ACTIVE_CRON = DSL.field(DSL.name("activeCron"), SQLDataType.VARCHAR(25).nullable(true));
    Field<Integer> INACTIVE_YEAR = DSL.field(DSL.name("inactiveYear"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> INACTIVE_MONTH = DSL.field(DSL.name("inactiveMonth"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> INACTIVE_DAY = DSL.field(DSL.name("inactiveDay"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> INACTIVE_HOUR = DSL.field(DSL.name("inactiveHour"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> INACTIVE_MINUTE = DSL.field(DSL.name("inactiveMinute"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> INACTIVE_SECOND = DSL.field(DSL.name("inactiveSecond"), SQLDataType.INTEGER.nullable(true));
    Field<String> INACTIVE_CRON = DSL.field(DSL.name("inactiveCron"), SQLDataType.VARCHAR(25).nullable(true));
    Field<Integer> TIMEOUT_PERIODS = DSL.field(DSL.name("timeoutPeriods"), SQLDataType.INTEGER.nullable(true));
    Field<Integer> TIMEOUT_PERIOD_TYPE = DSL.field(DSL.name("timeoutPeriodType"), SQLDataType.INTEGER.nullable(true));


    public MaintenanceEventsTableDefinition() {
        super(DSL.table(SchemaDefinition.TABLE_NAME),  DSL.name("me"));
    }

    @Override
    protected void addFields(List<Field<?>> fields) {
        super.addFields(fields);
        fields.add(ALARM_LEVEL);
        fields.add(SCHEDULE_TYPE);
        fields.add(DISABLED);
        fields.add(ACTIVE_YEAR);
        fields.add(ACTIVE_MONTH);
        fields.add(ACTIVE_DAY);
        fields.add(ACTIVE_HOUR);
        fields.add(ACTIVE_MINUTE);
        fields.add(ACTIVE_SECOND);
        fields.add(ACTIVE_CRON);
        fields.add(INACTIVE_YEAR);
        fields.add(INACTIVE_MONTH);
        fields.add(INACTIVE_DAY);
        fields.add(INACTIVE_HOUR);
        fields.add(INACTIVE_MINUTE);
        fields.add(INACTIVE_SECOND);
        fields.add(INACTIVE_CRON);
        fields.add(TIMEOUT_PERIODS);
        fields.add(TIMEOUT_PERIOD_TYPE);
    }

    @Override
    protected Name getNameFieldName() {
        return DSL.name("alias");
    }

}
