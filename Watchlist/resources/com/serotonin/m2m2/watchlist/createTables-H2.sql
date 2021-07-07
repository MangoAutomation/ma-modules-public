--
--    Copyright (C) 2021 Radix IoT LLC. All rights reserved.
--    @author Matthew Lohbihler
--
CREATE TABLE watchLists
(
    id               INT          NOT NULL AUTO_INCREMENT,
    xid              VARCHAR(100) NOT NULL,
    name             VARCHAR(255),
    type             VARCHAR(20),
    data             LONGTEXT,
    readPermissionId INT          NOT NULL,
    editPermissionId INT          NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE watchLists
    ADD CONSTRAINT watchListsUn1 UNIQUE (xid);
ALTER TABLE watchLists
    ADD CONSTRAINT watchListsFk2 FOREIGN KEY (readPermissionId) REFERENCES permissions (id) ON DELETE RESTRICT;
ALTER TABLE watchLists
    ADD CONSTRAINT watchListsFk3 FOREIGN KEY (editPermissionId) REFERENCES permissions (id) ON DELETE RESTRICT;

CREATE TABLE watchListPoints
(
    watchListId INT NOT NULL,
    dataPointId INT NOT NULL,
    sortOrder   INT NOT NULL
);
ALTER TABLE watchListPoints
    ADD CONSTRAINT watchListPointsFk1 FOREIGN KEY (watchListId) REFERENCES watchLists (id) ON DELETE CASCADE;
ALTER TABLE watchListPoints
    ADD CONSTRAINT watchListPointsFk2 FOREIGN KEY (dataPointId) REFERENCES dataPoints (id) ON DELETE CASCADE;

CREATE TABLE selectedWatchList
(
    userId      INT NOT NULL,
    watchListId INT NOT NULL,
    PRIMARY KEY (userId)
);
ALTER TABLE selectedWatchList
    ADD CONSTRAINT selectedWatchListFk1 FOREIGN KEY (userId) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE selectedWatchList
    ADD CONSTRAINT selectedWatchListFk2 FOREIGN KEY (watchListId) REFERENCES watchLists (id) ON DELETE CASCADE;
