--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table pointLinks (
  id int not null auto_increment,
  xid varchar(50) not null,
  sourcePointId int not null,
  targetPointId int not null,
  script longtext,
  eventType int not null,
  writeAnnotation char(1) not null,
  disabled char(1) not null,
  primary key (id)
) engine=InnoDB;
alter table pointLinks add constraint pointLinksUn1 unique (xid);
