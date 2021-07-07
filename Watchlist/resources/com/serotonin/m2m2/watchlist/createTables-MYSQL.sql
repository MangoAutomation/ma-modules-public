--
--    Copyright (C) 2021 Radix IoT LLC. All rights reserved.
--    @author Matthew Lohbihler
--
CREATE TABLE watchLists (
  id int NOT NULL auto_increment,
  xid varchar(100) NOT NULL,
  name varchar(255),
  type varchar(20),
  data longtext,
  readPermissionId INT NOT NULL,
  editPermissionId INT NOT NULL,
  PRIMARY KEY (id)
) engine=InnoDB;
ALTER TABLE watchLists add constraint watchListsUn1 unique (xid);
ALTER TABLE watchLists ADD CONSTRAINT watchListsFk2 FOREIGN KEY (readPermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;
ALTER TABLE watchLists ADD CONSTRAINT watchListsFk3 FOREIGN KEY (editPermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;

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
