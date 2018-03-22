--
--    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
--    @author Matthew Lohbihler
--
CREATE TABLE watchLists (
  id int NOT NULL auto_increment,
  xid varchar(100) NOT NULL,
  userId int NOT NULL,
  name varchar(50),
  readPermission varchar(255),
  editPermission varchar(255),
  type varchar(20),
  data longtext,
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

CREATE TABLE selectedWatchList (
  userId int NOT NULL,
  watchListId int NOT NULL,
  PRIMARY KEY (userId)
) engine=InnoDB;
ALTER TABLE selectedWatchList add constraint selectedWatchListFk1 foreign key (userId) references users(id) on delete cascade;
ALTER TABLE selectedWatchList add constraint selectedWatchListFk2 foreign key (watchListId) references watchLists(id) on delete cascade;
