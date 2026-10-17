-- OPTIONAL: new empty local database only. Never stores a default password/hash.
-- In your LOCAL interactive SQL session, set @bootstrap_username and
-- @bootstrap_password_hash (a locally generated BCrypt hash, not plaintext).
-- Do not save those SET statements in Git or shell history.
START TRANSACTION;
INSERT INTO sys_user (username,nickname,password,status,is_deleted,create_time)
SELECT @bootstrap_username,'Local administrator',@bootstrap_password_hash,1,0,NOW()
WHERE @bootstrap_username IS NOT NULL AND CHAR_LENGTH(@bootstrap_username) BETWEEN 1 AND 64
AND @bootstrap_password_hash IS NOT NULL AND CHAR_LENGTH(@bootstrap_password_hash)=60
AND LEFT(@bootstrap_password_hash,4) IN ('$2a$','$2b$','$2y$')
AND NOT EXISTS (SELECT 1 FROM sys_user)
AND EXISTS (SELECT 1 FROM sys_role WHERE code='ROOT' AND is_deleted=0);
SET @bootstrap_user_id = IF(ROW_COUNT()=1,LAST_INSERT_ID(),NULL);
INSERT INTO sys_user_role (user_id,role_id)
SELECT @bootstrap_user_id,id FROM sys_role
WHERE @bootstrap_user_id IS NOT NULL AND code='ROOT' AND is_deleted=0;
COMMIT;
SELECT IF(@bootstrap_user_id IS NULL,
  'No account created: database is not empty or required session variables/ROOT role are missing.',
  'Local administrator created. Clear the session variables and close this connection.') AS bootstrap_result;
SET @bootstrap_username=NULL, @bootstrap_password_hash=NULL, @bootstrap_user_id=NULL;
