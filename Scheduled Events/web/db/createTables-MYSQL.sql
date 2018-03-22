--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table scheduledEvents (
  id int not null auto_increment,
  xid varchar(100) not null,
  alias varchar(255),
  alarmLevel int not null,
  scheduleType int not null,
  returnToNormal char(1) not null,
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
  primary key (id)
) engine=InnoDB;
alter table scheduledEvents add constraint scheduledEventsUn1 unique (xid);
