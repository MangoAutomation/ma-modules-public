--
--    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table graphicalViews (
  id int not null identity,
  xid nvarchar(50) not null,
  name nvarchar(100) not null,
  background nvarchar(255),
  userId int not null,
  anonymousAccess int not null,
  data image not null,
  primary key (id)
);
alter table graphicalViews add constraint graphicalViewsUn1 unique (xid);
alter table graphicalViews add constraint graphicalViewsFk1 foreign key (userId) references users(id) on delete cascade;

create table graphicalViewUsers (
  graphicalViewId int not null,
  userId int not null,
  accessType int not null,
  primary key (graphicalViewId, userId)
);
alter table graphicalViewUsers add constraint graphicalViewUsersFk1 foreign key (graphicalViewId) references graphicalViews(id);
alter table graphicalViewUsers add constraint graphicalViewUsersFk2 foreign key (userId) references users(id) on delete cascade;
