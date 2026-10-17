# 数据库文件与使用边界

所有文件均不包含实际账号密码哈希、学生记录或住宿记录。保留上游结构的署名/许可证。

| 文件 | 用途 | 可对已有 smart_dorm 执行？ |
| --- | --- | --- |
| schema/2026-10-17-business-snapshot.sql | 从本机 MySQL 9.5.0 导出的四张业务表纯结构；保留现状，无数据、DROP、USE、自增计数 | 否：是快照，只适用于新空库，不是升级脚本 |
| init/001-system-schema.sql | 从上游脚本提取的16张底座表结构，无删除语句和账号 | 否：只用于新空库 |
| init/002-system-definitions.sql | 必要系统菜单、ROOT/ADMIN角色、部门、字典、配置定义，无用户/用户角色数据 | 否：使用固定定义 ID，仅新空库 |
| init/003-bootstrap-admin.sql | 可选：读取本地 SQL 会话变量初始化首个管理员，不保存默认密码或哈希 | 否：仅新空库，已有用户时不创建、不更新 |
| migrations/20261017_01_dorm_menu_permissions.sql | 只新增缺失的三个业务菜单和12项权限；复用已有组件菜单，不改已有授权 | 备份后人工执行；本轮仅在隔离验证库执行，未应用到 smart_dorm |
| verification/accommodation-schema-probe.sql | 临时表中的风险复现，只复制结构，数据为虚构值 | 可在本地开发库独立连接验证；不是迁移 |
| youlai-admin.sql | 已停用的旧入口说明 | 不导入。原破坏性脚本在本地备份及原分支历史中 |

新环境初始化：先通过本机 MySQL 客户端创建独立空库（推荐 `smart_dorm_dev`，utf8mb4），显式选择该库；文件均不硬编码 USE，防止切换错库。

```sql
-- 在 MySQL 客户端中，先检查当前库：
SELECT DATABASE();
SOURCE D:/JavaProjects/youlai-boot/sql/init/001-system-schema.sql;
SOURCE D:/JavaProjects/youlai-boot/sql/init/002-system-definitions.sql;
SOURCE D:/JavaProjects/youlai-boot/sql/schema/2026-10-17-business-snapshot.sql;
SOURCE D:/JavaProjects/youlai-boot/sql/migrations/20261017_01_dorm_menu_permissions.sql;
```

此时没有登录账号。使用 Spring Security 的 BCryptPasswordEncoder 在本地生成独立强密码的哈希，交互设置 `@bootstrap_username` 和 `@bootstrap_password_hash` 后 SOURCE `init/003-bootstrap-admin.sql`。不要把这些变量的真实赋值保存到脚本、终端历史或 Git。脚本只允许空用户表初始化，不重置已有账号；执行后清空会话变量。

ROOT 遵循既有超级管理员规则。其他角色必须通过角色管理显式分配菜单和权限；查询权限也必须分配。房间页还需要 `biz:building:list` 来加载楼宇。迁移不自动把权限授予 ADMIN/GUEST，也不写用户角色绑定，不擅自刷新现有 Redis 缓存。

菜单迁移只保证**串行重跑**不重复新增，不支持并行运行迁移；现有重复组件/权限不会自动清理，以最小 ID 复用菜单。新增权限是元数据准备，不等于授权已生效。

快照保留原有缺陷：住宿没有有效记录唯一约束，房间缺容量 CHECK，楼宇实体逻辑删除映射尚未补齐。不要误认为导出结构就实现了住宿事务。MySQL 9.5.0 已用于本轮验证，其他版本兼容性需另测。原 Docker Compose 自动挂载 SQL 的设置已移除。
