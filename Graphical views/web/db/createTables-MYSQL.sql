--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table graphicalViews (
  id int not null auto_increment,
  xid varchar(100) not null,
  name varchar(100) not null,
  background varchar(255),
  userId int not null,
  anonymousAccess int not null,
  readPermission varchar(255),
  setPermission varchar(255),
  editPermission varchar(255),
  data longblob not null,
  primary key (id)
) engine=InnoDB;
alter table graphicalViews add constraint graphicalViewsUn1 unique (xid);
alter table graphicalViews add constraint graphicalViewsFk1 foreign key (userId) references users(id) on delete cascade;