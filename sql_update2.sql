ALTER TABLE files RENAME TO sqlitestudio_temp_table;
CREATE TABLE files (report_id INTEGER, name VARCHAR, datetime DATETIME, direction INTEGER, linked_id INTEGER, type_id INTEGER, uuid VARCHAR (36) PRIMARY KEY);
INSERT INTO files (report_id, name, datetime, direction, linked_id, type_id, uuid) SELECT report_id, name, datetime, direction, linked_id, type_id, uuid FROM sqlitestudio_temp_table;
DROP TABLE sqlitestudio_temp_table;
ALTER TABLE transport_files RENAME TO sqlitestudio_temp_table;
CREATE TABLE transport_files (parent_uuid VARCHAR (36), child_uuid VARCHAR (36));
INSERT INTO transport_files (parent_uuid, child_uuid) SELECT parent_uuid, child_uuid FROM sqlitestudio_temp_table;
DROP TABLE sqlitestudio_temp_table;