ALTER TABLE files RENAME TO sqlitestudio_temp_table;
CREATE TABLE files (report_id INTEGER, name VARCHAR, datetime DATETIME, direction INTEGER, linked_uuid VARCHAR (36), type_id INTEGER, uuid VARCHAR (36) PRIMARY KEY);
INSERT INTO files (report_id, name, datetime, direction, linked_uuid, type_id, uuid) SELECT report_id, name, datetime, direction, linked_uuid, type_id, uuid FROM sqlitestudio_temp_table;
DROP TABLE sqlitestudio_temp_table;
ALTER TABLE transport_files RENAME TO sqlitestudio_temp_table;
CREATE TABLE transport_files (parent_uuid VARCHAR (36), child_uuid VARCHAR (36));
INSERT INTO transport_files (parent_uuid, child_uuid) SELECT parent_uuid, child_uuid FROM sqlitestudio_temp_table;
DROP TABLE sqlitestudio_temp_table;
ALTER TABLE file_attributes RENAME TO sqlitestudio_temp_table;
CREATE TABLE file_attributes (id INTEGER PRIMARY KEY AUTOINCREMENT, attribute_id INTEGER, value VARCHAR, file_uuid VARCHAR (36));
INSERT INTO file_attributes (id, attribute_id, value, file_uuid) SELECT id, attribute_id, value, file_uuid FROM sqlitestudio_temp_table;
DROP TABLE sqlitestudio_temp_table;