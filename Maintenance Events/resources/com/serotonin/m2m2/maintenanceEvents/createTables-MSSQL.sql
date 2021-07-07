--
--    Copyright (C) 2021 Radix IoT LLC. All rights reserved.
--    @author Matthew Lohbihler
--
create table maintenanceEvents (
  id int not null identity,
  xid nvarchar(100) not null,
  alias nvarchar(255),
  alarmLevel int not null,
  scheduleType int not null,
  disabled char(1) not null,
  activeYear int,
  activeMonth int,
  activeDay int,
  activeHour int,
  activeMinute int,
  activeSecond int,
  activeCron nvarchar(25),
  inactiveYear int,
  inactiveMonth int,
  inactiveDay int,
  inactiveHour int,
  inactiveMinute int,
  inactiveSecond int,
  inactiveCron nvarchar(25),
  timeoutPeriods int,
  timeoutPeriodType int,
  togglePermissionId INT NOT NULL,
  primary key (id)
);
alter table maintenanceEvents add constraint maintenanceEventsUn1 unique (xid);
ALTER TABLE maintenanceEvents ADD CONSTRAINT maintenanceEventsFk1 FOREIGN KEY (togglePermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;

CREATE TABLE maintenanceEventDataPoints (
  maintenanceEventId int NOT NULL,
  dataPointId int NOT NULL
) ;
ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;
ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;

CREATE TABLE maintenanceEventDataSources (
  maintenanceEventId int NOT NULL,
  dataSourceId int NOT NULL
) ;
ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;
ALTER TABLE maintenanceEventDataSources add constraint maintenanceEventDataSourcesFk2 foreign key (dataSourceId) references dataSources(id) on delete cascade;

