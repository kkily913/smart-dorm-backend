-- Run only against a local development database that already has the biz_* tables.
-- Copies structure only, never reads or changes business rows.
-- Temporary tables disappear when this mysql session closes.
-- These probes reproduce missing database guards; they are NOT passing business tests.
CREATE TEMPORARY TABLE probe_accommodation LIKE biz_accommodation;
INSERT INTO probe_accommodation (student_id, room_id, bed_number, status, is_deleted)
VALUES (900001, 900001, 1, 1, 0),
       (900001, 900002, 1, 1, 0),
       (900002, 900001, 1, 1, 0);
SELECT 'duplicate_active_student' AS probe, COUNT(*) AS accepted_rows
FROM probe_accommodation WHERE student_id = 900001 AND status = 1 AND is_deleted = 0;
SELECT 'duplicate_active_bed' AS probe, COUNT(*) AS accepted_rows
FROM probe_accommodation WHERE room_id = 900001 AND bed_number = 1 AND status = 1 AND is_deleted = 0;

CREATE TEMPORARY TABLE probe_room LIKE biz_room;
INSERT INTO probe_room (building_id, room_number, capacity, occupied_count)
VALUES (900001, 'PROBE-ONLY', 4, 5);
SELECT 'capacity_exceeded' AS probe, COUNT(*) AS accepted_rows
FROM probe_room WHERE occupied_count > capacity;

CREATE TEMPORARY TABLE probe_student LIKE biz_student;
INSERT INTO probe_student (student_no, name, gender, is_deleted)
VALUES ('PROBE-ONLY', 'Synthetic test student', 1, 1);
-- The unique student_no index still covers the deleted row. IGNORE lets the next
-- SELECT report the collision without stopping the other diagnostics.
INSERT IGNORE INTO probe_student (student_no, name, gender, is_deleted)
VALUES ('PROBE-ONLY', 'Synthetic replacement', 1, 0);
SELECT 'reused_deleted_student_number' AS probe, COUNT(*) AS accepted_active_rows
FROM probe_student WHERE student_no = 'PROBE-ONLY' AND is_deleted = 0;
