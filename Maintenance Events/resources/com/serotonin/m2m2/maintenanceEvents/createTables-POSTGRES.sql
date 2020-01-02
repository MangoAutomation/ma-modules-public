--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table maintenanceEvents (
  id int not null auto_increment,
  xid varchar(100) not null,
  dataSourceId int,
  alias varchar(255),
  alarmLevel int not null,
  scheduleType int not null,
  disabled char(1) not null,
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
  togglePermission varchar(255),
  primary key (id)
);
alter table maintenanceEvents add constraint maintenanceEventsUn1 unique (xid);
alter table maintenanceEvents add constraint maintenanceEventsFk1 foreign key (dataSourceId) references dataSources(id) on delete cascade;

CREATE TABLE maintenanceEventDataPoints (
  maintenanceEventId int NOT NULL,
  dataPointId int NOT NULL
) ;
ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk1 foreign key (maintenanceEventId) references maintenanceEvents(id) on delete cascade;
ALTER TABLE maintenanceEventDataPoints add constraint maintenanceEventDataPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;
