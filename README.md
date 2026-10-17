# 智慧校舍管理系统 · 后端

用于 Java 后端求职学习的个人项目。基于 [有来开源组织 youlai-boot](https://gitee.com/youlaiorg/youlai-boot) 二次开发；保留原 Apache-2.0 [许可证](LICENSE) 和代码署名。前端为独立的 smart-dorm-frontend 仓库，不合并仓库、不移除上游归属。

## 实际进度

| 模块 | 状态 |
| --- | --- |
| 用户、角色、菜单、认证等管理底座 | 继承上游实现；完整登录联调仍受本地环境阻塞 |
| 楼宇、房间、学生 | 已有前后端 CRUD、数据表、菜单及12个接口操作权限；完整业务联调尚未验证 |
| 住宿 | 仅有数据库表结构；无入住/退宿服务、事务或并发控制实现 |
| Agent | 未实现，本轮不开发 |

现有业务风险、代码证据和后续优先级见 [项目进度](docs/project-status.md)。这不是生产就绪系统：验证码绕过、字段校验和楼宇逻辑删除等已知问题仍待处理，不能直接对公网开放。

## 技术与环境

Java 编译目标17，建议本机 JDK21；Spring Boot 4.0.5、Spring Security、MyBatis-Plus 3.5.15，Maven，MySQL、Redis。本机结构及脚本在 MySQL 9.5.0 验证，其他数据库版本需另测。Redis 是已有认证/权限基础设施依赖，关闭 Spring Cache 不会取消这个依赖。

## 启动步骤

1. 准备独立的本地 MySQL 数据库和 Redis。先阅读 [SQL 初始化顺序与限制](sql/README.md)，不要对已有 smart_dorm 执行空库初始化。
2. 新空库依次导入：`sql/init/001-system-schema.sql` → `002-system-definitions.sql` → `sql/schema/2026-10-17-business-snapshot.sql` → `sql/migrations/20261017_01_dorm_menu_permissions.sql`。账号不随仓库提供；按 SQL 文档使用可选的管理员初始化脚本，密码和 BCrypt 哈希仅在本机设置。
3. 通过环境变量配置以下项目；或复制 `config/application-local.example.yml` 为 `config/application-local.yml`。后者已忽略，仅在 dev profile 下加载，不会打进 JAR。
4. `mvn spring-boot:run`，默认 dev、端口8000；或先 `mvn -Dtest=BizControllerPermissionTest package` 再 `java -jar target/youlai-boot.jar`。
5. 启动独立前端，默认3000；其 `/dev-api` 代理指向本地8000。非 ROOT 角色需要显式分配 `biz:building|room|student:list|create|update|delete` 精确权限；房间页还依赖 building:list。

## 配置约定

| 环境变量 | 用途 | 默认/要求 |
| --- | --- | --- |
| DB_URL | JDBC 地址 | 本机3306/smart_dorm；新环境建议独立 smart_dorm_dev |
| DB_USERNAME / DB_PASSWORD | 数据库账号/密码 | 账号默认 smart_dorm；密码必须私下提供 |
| REDIS_HOST / REDIS_PORT / REDIS_DATABASE / REDIS_PASSWORD | Redis 连接 | 本机6379、库0；密码按本机设置 |
| JWT_SECRET | JWT 签名 | 必须提供独立高强度随机值，至少32字符；无共享默认值 |
| FILE_STORAGE_TYPE | 文件存储 | local；切换 minio 前配置对应凭据 |
| MINIO_ENDPOINT / MINIO_ACCESS_KEY / MINIO_SECRET_KEY | 可选 MinIO | 凭据为空，按需配置 |
| MAIL_* / OSS_ACCESS_KEY_* / SMS_ACCESS_KEY_* / WX_APP_ID / WX_APP_SECRET / XXL_JOB_ACCESS_TOKEN | 可选外部能力 | 默认不提供凭据；未联调不代表可用 |

Spring 标准 `SPRING_DATASOURCE_*`、`SPRING_DATA_REDIS_*` 变量也可直接覆盖对应属性。已有 `config/application-local.yml` 中的字面配置会覆盖主配置的 DB_* 占位项；切换环境时移开本地文件，或使用标准 Spring 属性环境变量显式覆盖。prod 不导入本地文件。`.env` 不会自动被 Spring Boot 加载，不要以为新建 .env 就完成了后端配置。

真实密码、Token、模型密钥只能放服务端环境变量或忽略的本地文件，不能放前端 VITE_*。本项目未实现模型调用，不需要模型密钥。

Docker 编排仅为可选工具，凭据已改为变量，`docker/.env.example` 是空值示例；不再自动导入旧 SQL，当前环境没有验证 Docker 启动。

## 验证

```sh
mvn -Dtest=BizControllerPermissionTest test
mvn -Dtest=BizControllerPermissionTest package
```

最近业务修复：72个方法授权用例通过（修改前48个拒绝场景失败）；前端类型检查、完整构建及 HTTP 预览通过。它们不等于数据库 CRUD 或生产 Token 链路通过。全量认证测试仍缺独立测试库，Redis 未启动，尚未完成。

数据库文件验证：`python scripts/verify_schema.py`，需要本机 mysql 客户端、MYSQL_USER/MYSQL_PWD 环境变量。只接受回环地址，新建随机验证库，检查20张表、3个业务菜单、12项权限、串行重跑、既有菜单保留和初始化保护，最后只删除自己创建的验证库。不会操作已有 smart_dorm。

## 版本与发布

原上游历史与原分支保留在本机；鉴于旧历史含凭据及演示数据，本次准备推送 `codex/github-save` 的清理后独立根快照，不推送原历史、旧标签或全部分支。`origin` 仍是原作者 Gitee 地址，禁止向其推送。个人 GitHub 仓库计划为私有 smart-dorm-backend；GitHub CLI 未找到，创建/推送尚未执行，不能把本 README 当作已经备份到云端的证明。

详见 [安全保存记录与恢复说明](docs/publication-status.md)。原许可证、署名和来源链接保留，不将上游能力宣称为全部独立开发。
