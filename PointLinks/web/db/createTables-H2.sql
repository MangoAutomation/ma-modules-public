--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table pointLinks (
  id int not null auto_increment,
  xid varchar(100) not null,
  sourcePointId int not null,
  targetPointId int not null,
  script longtext,
  eventType int not null,
  writeAnnotation char(1) not null,
  disabled char(1) not null,
  logLevel int not null,
  logSize double not null,
  logCount int not null,
  scriptDataSourcePermission varchar(255) not null,
  scriptDataPointSetPermission varchar(255) not null,
  scriptDataPointReadPermission varchar(255) not null,
  primary key (id)
);
alter table pointLinks add constraint pointLinksUn1 unique (xid);
