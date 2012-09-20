--
--    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table pointLinks (
  id int not null identity,
  xid nvarchar(50) not null,
  sourcePointId int not null,
  targetPointId int not null,
  script ntext,
  eventType int not null,
  writeAnnotation char(1) not null,
  disabled char(1) not null,
  primary key (id)
);
alter table pointLinks add constraint pointLinksUn1 unique (xid);
