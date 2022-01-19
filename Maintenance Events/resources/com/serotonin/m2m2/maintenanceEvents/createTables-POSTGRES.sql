--
--    Copyright (C) 2021 Radix IoT LLC. All rights reserved.
--    @author Matthew Lohbihler
--



CREATE TABLE maintenanceEvents (
    id serial,
    xid varchar(100) NOT NULL,
    alias varchar(255),
    alarmLevel int NOT NULL,
    scheduleType int NOT NULL,
    disabled char(1) NOT NULL,
    activeYear int,
    activeMonth int,
    activeDay int,
    activeHour int,
    activeMinute int,
    activeSecond int,
    activeCron varchar(25),
    inactiveYear int,
    inactiveMonth int,
    inactiveDay int,
    inactiveHour int,
    inactiveMinute int,
    inactiveSecond int,
    inactiveCron varchar(25),
    timeoutPeriods int,
    timeoutPeriodType int,
    togglePermissionId int NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE maintenanceEvents ADD CONSTRAINT maintenanceEventsUn1 UNIQUE (xid);
ALTER TABLE maintenanceEvents ADD CONSTRAINT maintenanceEventsFk1 FOREIGN KEY (togglePermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;

CREATE TABLE maintenanceEventDataPoints (
    maintenanceEventId int NOT NULL,
    dataPointId int NOT NULL
);
ALTER TABLE maintenanceEventDataPoints ADD CONSTRAINT maintenanceEventDataPointsFk1 FOREIGN KEY (maintenanceEventId) REFERENCES maintenanceEvents(id) ON DELETE CASCADE;
ALTER TABLE maintenanceEventDataPoints ADD CONSTRAINT maintenanceEventDataPointsFk2 FOREIGN KEY (dataPointId) REFERENCES dataPoints(id) ON DELETE CASCADE;

CREATE TABLE maintenanceEventDataSources (
    maintenanceEventId int NOT NULL,
    dataSourceId int NOT NULL
);
ALTER TABLE maintenanceEventDataSources ADD CONSTRAINT maintenanceEventDataSourcesFk1 FOREIGN KEY (maintenanceEventId) REFERENCES maintenanceEvents(id) ON DELETE CASCADE;
ALTER TABLE maintenanceEventDataSources ADD CONSTRAINT maintenanceEventDataSourcesFk2 FOREIGN KEY (dataSourceId) REFERENCES dataSources(id) ON DELETE CASCADE;
