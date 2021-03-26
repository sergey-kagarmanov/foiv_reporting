ALTER TABLE files RENAME TO sqlitestudio_temp_table;
CREATE TABLE files (id INTEGER PRIMARY KEY AUTOINCREMENT, report_id INTEGER, name VARCHAR, datetime DATETIME, direction INTEGER, linked_id INTEGER, type_id INTEGER, uuid VARCHAR (36), linked_uuid VARCHAR (36));
INSERT INTO files (id, report_id, name, datetime, direction, linked_id, type_id) SELECT id, report_id, name, datetime, direction, linked_id, type_id FROM sqlitestudio_temp_table;
DROP TABLE sqlitestudio_temp_table;
ALTER TABLE transport_files RENAME TO sqlitestudio_temp_table;
CREATE TABLE transport_files (id INTEGER PRIMARY KEY AUTOINCREMENT, parent_id INTEGER, child_id INTEGER, parent_uuid VARCHAR (36), child_uuid VARCHAR (36));
INSERT INTO transport_files (id, parent_id, child_id) SELECT id, parent_id, child_id FROM sqlitestudio_temp_table;
DROP TABLE sqlitestudio_temp_table;
