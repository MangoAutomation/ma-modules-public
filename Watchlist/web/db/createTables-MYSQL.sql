--
--    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
--    @author Matthew Lohbihler
--
CREATE TABLE watchLists (
  id int NOT NULL auto_increment,
  xid varchar(50) NOT NULL,
  userId int NOT NULL,
  name varchar(50),
  PRIMARY KEY (id)
) engine=InnoDB;
ALTER TABLE watchLists add constraint watchListsUn1 unique (xid);
ALTER TABLE watchLists add constraint watchListsFk1 foreign key (userId) references users(id) on delete cascade;

CREATE TABLE watchListPoints (
  watchListId int NOT NULL,
  dataPointId int NOT NULL,
  sortOrder int NOT NULL
) engine=InnoDB;
ALTER TABLE watchListPoints add constraint watchListPointsFk1 foreign key (watchListId) references watchLists(id) on delete cascade;
ALTER TABLE watchListPoints add constraint watchListPointsFk2 foreign key (dataPointId) references dataPoints(id) on delete cascade;

CREATE TABLE watchListUsers (
  watchListId int NOT NULL,
  userId int NOT NULL,
  accessType int NOT NULL,
  PRIMARY KEY (watchListId, userId)
) engine=InnoDB;
ALTER TABLE watchListUsers add constraint watchListUsersFk1 foreign key (watchListId) references watchLists(id) on delete cascade;
ALTER TABLE watchListUsers add constraint watchListUsersFk2 foreign key (userId) references users(id) on delete cascade;

CREATE TABLE selectedWatchList (
  userId int NOT NULL,
  watchListId int NOT NULL,
  PRIMARY KEY (userId)
) engine=InnoDB;
ALTER TABLE selectedWatchList add constraint selectedWatchListFk1 foreign key (userId) references users(id) on delete cascade;
ALTER TABLE selectedWatchList add constraint selectedWatchListFk2 foreign key (watchListId) references watchLists(id) on delete cascade;
