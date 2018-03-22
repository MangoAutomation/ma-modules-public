--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table graphicalViews (
  id int not null identity,
  xid nvarchar(100) not null,
  name nvarchar(100) not null,
  background nvarchar(255),
  userId int not null,
  anonymousAccess int not null,
  readPermission nvarchar(255),
  setPermission nvarchar(255),
  editPermission nvarchar(255),
  data image not null,
  primary key (id)
);
alter table graphicalViews add constraint graphicalViewsUn1 unique (xid);
alter table graphicalViews add constraint graphicalViewsFk1 foreign key (userId) references users(id) on delete cascade;