--
--    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table graphicalViews (
  id int not null auto_increment,
  xid varchar(50) not null,
  name varchar(100) not null,
  background varchar(255),
  userId int not null,
  anonymousAccess int not null,
  data longblob not null,
  primary key (id)
) engine=InnoDB;
alter table graphicalViews add constraint graphicalViewsUn1 unique (xid);
alter table graphicalViews add constraint graphicalViewsFk1 foreign key (userId) references users(id) on delete cascade;

create table graphicalViewUsers (
  graphicalViewId int not null,
  userId int not null,
  accessType int not null,
  primary key (graphicalViewId, userId)
) engine=InnoDB;
alter table graphicalViewUsers add constraint graphicalViewUsersFk1 foreign key (graphicalViewId) references graphicalViews(id);
alter table graphicalViewUsers add constraint graphicalViewUsersFk2 foreign key (userId) references users(id) on delete cascade;
