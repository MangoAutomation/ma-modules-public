--
--    Copyright (C) 2021 Radix IoT LLC. All rights reserved.
--    @author Matthew Lohbihler
--
create table watchLists (
  id int not null identity,
  xid nvarchar(255) not null,
  name nvarchar(50),
  type nvarchar(20),
  data ntext,
  readPermissionId INT NOT NULL,
  editPermissionId INT NOT NULL,
  primary key (id)
);
alter table watchLists add constraint watchListsUn1 unique (xid);
ALTER TABLE watchLists ADD CONSTRAINT watchListsFk2 FOREIGN KEY (readPermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;
ALTER TABLE watchLists ADD CONSTRAINT watchListsFk3 FOREIGN KEY (editPermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;

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
