--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
CREATE TABLE "maintenanceEvents"
(
    "id"                 INT          NOT NULL AUTO_INCREMENT,
    "xid"                VARCHAR(100) NOT NULL,
    "alias"              VARCHAR(255),
    "alarmLevel"         INT          NOT NULL,
    "scheduleType"       INT          NOT NULL,
    "disabled"           CHAR(1)      NOT NULL,
    "activeYear"         INT,
    "activeMonth"        INT,
    "activeDay"          INT,
    "activeHour"         INT,
    "activeMinute"       INT,
    "activeSecond"       INT,
    "activeCron"         VARCHAR(25),
    "inactiveYear"       INT,
    "inactiveMonth"      INT,
    "inactiveDay"        INT,
    "inactiveHour"       INT,
    "inactiveMinute"     INT,
    "inactiveSecond"     INT,
    "inactiveCron"       VARCHAR(25),
    "timeoutPeriods"     INT,
    "timeoutPeriodType"  INT,
    "togglePermissionId" INT          NOT NULL,
    PRIMARY KEY ("id")
);
ALTER TABLE "maintenanceEvents"
    ADD CONSTRAINT "maintenanceEventsUn1" UNIQUE ("xid");
-- [jooq ignore start]
ALTER TABLE "maintenanceEvents"
    ADD CONSTRAINT "maintenanceEventsFk1" FOREIGN KEY ("togglePermissionId") REFERENCES "permissions" ("id") ON DELETE RESTRICT;
-- [jooq ignore stop]

CREATE TABLE "maintenanceEventDataPoints"
(
    "maintenanceEventId" INT NOT NULL,
    "dataPointId"        INT NOT NULL
);
ALTER TABLE "maintenanceEventDataPoints"
    ADD CONSTRAINT "maintenanceEventDataPointsFk1" FOREIGN KEY ("maintenanceEventId") REFERENCES "maintenanceEvents" ("id") ON DELETE CASCADE;
-- [jooq ignore start]
ALTER TABLE "maintenanceEventDataPoints"
    ADD CONSTRAINT "maintenanceEventDataPointsFk2" FOREIGN KEY ("dataPointId") REFERENCES "dataPoints" ("id") ON DELETE CASCADE;
-- [jooq ignore stop]

CREATE TABLE "maintenanceEventDataSources"
(
    "maintenanceEventId" INT NOT NULL,
    "dataSourceId"       INT NOT NULL
);
ALTER TABLE "maintenanceEventDataSources"
    ADD CONSTRAINT "maintenanceEventDataSourcesFk1" FOREIGN KEY ("maintenanceEventId") REFERENCES "maintenanceEvents" ("id") ON DELETE CASCADE;
-- [jooq ignore start]
ALTER TABLE "maintenanceEventDataSources"
    ADD CONSTRAINT "maintenanceEventDataSourcesFk2" FOREIGN KEY ("dataSourceId") REFERENCES "dataSources" ("id") ON DELETE CASCADE;
-- [jooq ignore stop]

