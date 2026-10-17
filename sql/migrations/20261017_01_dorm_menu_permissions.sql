-- Add-only menu/permission metadata; run once, serially, against a backed-up database.
-- Can be rerun serially. Does not create business tables or alter existing menus/roles.
-- Does NOT grant permissions to any account/role. Use the role management UI explicitly.
START TRANSACTION;
INSERT INTO sys_menu (parent_id,tree_path,name,type,route_name,route_path,component,visible,sort,create_time)
SELECT 0,'0','校舍管理','C','DormManagement','/dorm','Layout',1,20,NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_path='/dorm' AND type='C')
AND (SELECT COUNT(DISTINCT component) FROM sys_menu
     WHERE component IN ('system/building/index','system/room/index','system/student/index')) < 3;
SET @dorm_parent = (SELECT MIN(id) FROM sys_menu WHERE route_path='/dorm' AND type='C');

INSERT INTO sys_menu (parent_id,tree_path,name,type,route_name,route_path,component,visible,sort,create_time)
SELECT @dorm_parent,CONCAT('0,',@dorm_parent),'楼宇管理','M','DormBuilding','building','system/building/index',1,1,NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE component='system/building/index');
SET @module_menu = (SELECT MIN(id) FROM sys_menu WHERE component='system/building/index');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'楼宇管理-查询','B','biz:building:list',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:building:list');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'楼宇管理-新增','B','biz:building:create',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:building:create');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'楼宇管理-修改','B','biz:building:update',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:building:update');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'楼宇管理-删除','B','biz:building:delete',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:building:delete');

INSERT INTO sys_menu (parent_id,tree_path,name,type,route_name,route_path,component,visible,sort,create_time)
SELECT @dorm_parent,CONCAT('0,',@dorm_parent),'房间管理','M','DormRoom','room','system/room/index',1,2,NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE component='system/room/index');
SET @module_menu = (SELECT MIN(id) FROM sys_menu WHERE component='system/room/index');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'房间管理-查询','B','biz:room:list',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:room:list');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'房间管理-新增','B','biz:room:create',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:room:create');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'房间管理-修改','B','biz:room:update',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:room:update');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'房间管理-删除','B','biz:room:delete',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:room:delete');

INSERT INTO sys_menu (parent_id,tree_path,name,type,route_name,route_path,component,visible,sort,create_time)
SELECT @dorm_parent,CONCAT('0,',@dorm_parent),'学生管理','M','DormStudent','student','system/student/index',1,3,NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE component='system/student/index');
SET @module_menu = (SELECT MIN(id) FROM sys_menu WHERE component='system/student/index');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'学生管理-查询','B','biz:student:list',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:student:list');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'学生管理-新增','B','biz:student:create',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:student:create');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'学生管理-修改','B','biz:student:update',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:student:update');
INSERT INTO sys_menu (parent_id,tree_path,name,type,perm,visible,sort,create_time)
SELECT m.id,CONCAT(m.tree_path,',',m.id),'学生管理-删除','B','biz:student:delete',1,0,NOW()
FROM sys_menu m WHERE m.id=@module_menu
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perm='biz:student:delete');
COMMIT;
