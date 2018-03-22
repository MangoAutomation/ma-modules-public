--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
create table watchLists (
  id int not null identity,
  xid nvarchar(100) not null,
  userId int not null,
  name nvarchar(50),
  readPermission nvarchar(255),
  editPermission nvarchar(255),
  type nvarchar(20),
  data ntext,
  primary key (id)
);
alter table watchLists add constraint watchListsUn1 unique (xid);
alter table watchLists add constraint watchListsFk1 foreign key (userId) references users(id) on delete cascade;

create table watchListPoints (
  watchListId int not null,
  dataPointId int not null,
  sortOrder int not null
);
alter table watchListPoints add constraint watchListPointsFk1 foreign key (watchListId) references watchLists(id) on delete cascade;
alter table watchListPoints add constraint watchListPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;

create table selectedWatchList (
  userId int not null,
  watchListId int not null,
  primary key (userId)
);
alter table selectedWatchList add constraint selectedWatchListFk1 foreign key (userId) references users(id) on delete cascade;
alter table selectedWatchList add constraint selectedWatchListFk2 foreign key (watchListId) references watchLists(id) on delete cascade;
