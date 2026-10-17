# 智慧校舍管理系统：开发现场与验证记录

更新日期：2026-10-18（Asia/Hong_Kong）。下文第1—10节保留2026-10-17开发现场基线；第11节记录后续安全保存整理，涉及配置和SQL的最新说明以第11节为准。结论以工作区、数据库元数据和实际命令为依据，README 的模板功能宣传不作为业务完成证明。

## 1. 接手结论

项目处于“管理底座 + 楼宇/房间/学生 CRUD 初版 + 本机住宿表设计”的阶段，尚未形成入住、床位分配、退宿闭环。

- 后端：`D:/JavaProjects/youlai-boot`；前端：`D:/JavaProjects/vue3-element-admin`，是两个独立 Git 仓库。
- 未发现适用的 AGENTS.md。阅读了两个 README、pom.xml、package.json、配置、Docker 文档和源码后开展检查。
- 后端原有未提交内容：SQL、验证码过滤器、dev/prod 配置、三个 Biz 模块的控制器/实体/Mapper/Service（多项为 AM，即暂存后继续修改）。前端原有 `.env.development` 修改和六个未跟踪的业务 API/页面目录。
- 本轮保留这些内容及暂存区；没有提交、推送、回滚、导入初始化 SQL或修改实际业务记录。
- 后端最近提交：`5085d4c7` 日志采集/落库解耦、`155fc69a` 启动配置修复、`afc17807` 自动填充调整。前端 `384696af`/`5671190b`/`8904af43` 集中在样式预构建。最近提交主要是底座维护，校舍功能进度保存在未提交文件和本机数据库中。

## 2. 技术栈与环境

| 层次 | 实际依据与版本 | 当前情况 |
| --- | --- | --- |
| Java 后端 | pom.xml：项目 4.7.0，Java 编译目标 17，Spring Boot 4.0.5，Spring Security，MyBatis-Plus 3.5.15，Druid 1.2.24，MySQL 驱动 9.1.0 | 单模块 Maven 项目，业务在 system 包 |
| JSON/API | Jackson 3 的 tools.jackson 配置，Knife4j 4.5.0、springdoc 2.8.9 | 本轮未验证文档端点运行兼容性 |
| 缓存/认证 | spring-data-redis、Redisson 4.1.0；默认 dev 配置 | 已有底座依赖，非本轮新增。启动需要 Redis；`spring.cache.enabled=false` 不等于不需要 Redis |
| 前端 | package.json 4.8.4；本机安装 Vue 3.5.41、Vite 8.2.2、TypeScript 6.0.3、Element Plus 2.14.5、Pinia 4.0.3、Vue Router 5.2.0 | package.json 声明范围与 README 徽章不同，以上为读取 node_modules 的实际版本 |
| Node/包管理 | 本机 Node 24.11.1、npm 11.6.2；项目要求 pnpm，含 pnpm-lock.yaml | 本轮复用已安装依赖，以 npm run 执行脚本，没有重新安装或升级依赖 |
| 数据库 | 本机 MySQL **9.5.0**，开发库 `smart_dorm`，通过 SELECT VERSION() 和 information_schema 核实 | 3306 可达；不等于独立测试环境已准备完成 |
| 工具链 | PATH 中 java 是 Java 8，mvn 缺失；找到 `D:/program/Java` 的 JDK 21.0.5 和 IDEA 自带 Maven | 用当前进程 JAVA_HOME 运行，未改系统全局环境 |
| 其他 | MinIO/本地文件、短信、邮件、微信、XXL Job、SSE 等底座实现存在 | 本轮未联调这些外部能力；XXL Job 配置关闭 |

数据库读取只涉及列、索引、约束、菜单和角色元数据，没有读取学生个人记录。配置中存在明文凭据/样例密钥，本文件不复制其内容。

## 3. 模块状态

下表“部分完成”优先表达整个业务模块的状态；通过编译或某个权限测试不等于模块整体已运行。

| 模块 | 已有实现 | 缺失环节 | 证据位置 | 验证状态 | 下一步 |
| --- | --- | --- | --- | --- | --- |
| 认证/RBAC 管理底座 | 登录、Token、验证码、用户、角色、菜单、部门、字典、配置及权限组件 | 本机 Redis 未启动；现有认证测试依赖 dev 配置；验证码校验被注释 | auth/；config/SecurityConfig.java；framework/security/service/PermissionService.java；AuthControllerTest.java | 静态实现存在，编译/打包通过；登录未运行验证 | 建立隔离测试配置，恢复安全登录测试 |
| 楼宇 | CRUD 后端/页面，数据库表，动态菜单；本轮接口权限 | 实体缺 isDeleted；删楼宇不检查房间；表结构/菜单未入库脚本；字段校验缺失 | BizBuildingController、BizBuildingServiceImpl、BizBuilding；前端 views/system/building/index.vue | **部分完成**；权限测试通过，数据库 CRUD 未联调 | 优先统一逻辑删除并保护关联关系 |
| 房间 | 按楼宇/房号分页，CRUD、房型/状态、人数编辑、表与菜单 | 无楼宇有效性/楼层/容量校验；occupiedCount 可手改；只取前 100 个楼宇；无房号唯一约束 | BizRoomController、BizRoomServiceImpl、BizRoom；前端 views/system/room/index.vue | **部分完成**；类型检查/构建、权限测试通过；容量缺口临时表复现 | 入住人数改由住宿流程维护，定义房间约束 |
| 学生 | 学号/姓名分页、CRUD、逻辑删除、表与菜单 | 实体直接入参/出参，无校验/敏感字段裁剪；学号删除后重建规则未明确 | BizStudentController；BizStudent；前端 views/system/student/index.vue | **部分完成**；权限测试通过；唯一键与软删除行为已在临时表核实 | DTO/VO、校验、敏感字段权限和学号生命周期 |
| 住宿/入住/退宿 | 本机存在 biz_accommodation 表及普通 student_id/room_id 索引 | 仓库未发现对应实体/接口/页面/服务/事务/测试，表结构未版本化 | information_schema；sql/verification/accommodation-schema-probe.sql | **只有数据库骨架**；缺失约束已复现；无业务调用链可运行 | 明确有效住宿状态、床位编号与退宿规则后设计事务/约束 |
| 独立床位管理 | 未发现独立床位表；住宿表有 bed_number | 无床位生命周期、唯一分配入口 | 本机 biz_accommodation 元数据；仓库检索 | **仓库中未发现独立实现** | 决定独立床位实体或房间内编号方案 |
| AI Agent | 未发现模型 SDK、模型调用、工具执行、超时/权限流程 | 无真实业务能力 | pom.xml、前后端关键词/文件扫描 | **仓库中未发现**；UserAgent 是浏览器识别，Mockito agent 是 JVM 测试机制，均非业务 Agent | 当前不优先引入 |
| 通知/文件/SSE/代码生成/日志 | 继承底座的控制器、服务、Mapper/XML/前端 | 未进行端到端验证 | system/controller；support/；codegen/；mapper/system | 静态检查存在实现，**尚未运行验证** | 只按校舍业务实际需求选择联调 |

主要关系：楼宇 `managerId` 只是字段，尚无宿管业务角色/归属校验；房间 `buildingId` 指向楼宇；住宿表 `student_id`、`room_id` 关联学生/房间。本机业务表没有外键约束。学生实体没有系统用户绑定字段，因此不能宣称“学生自助入住”。

本机角色：ROOT、ADMIN、GUEST、DEPT_MANAGER、DEPT_MEMBER、EMPLOYEE、CUSTOM_USER，均来自底座；未发现专门的宿管/学生角色。测试里的 DORM_OPERATOR 是虚构测试身份，不是已经上线的角色。

## 4. 前后端、SQL 与菜单核对

| 对照项 | 核实结果 |
| --- | --- |
| 楼宇 API | `/api/v1/system/buildings/page` GET；根路径 POST；`/{id}` PUT；`/{ids}` DELETE。前端调用吻合，单个 id 能对应数组路径参数 |
| 房间 API | `/api/v1/system/rooms/page` GET；根路径 POST；`/{id}` PUT；`/{ids}` DELETE。前端调用吻合 |
| 学生 API | `/api/v1/system/students` GET/POST，`/{id}` PUT/DELETE。分页不是 `/page`，前端也使用根路径，未发现路径错配 |
| 分页响应 | 楼宇/房间 `data.records,total`；学生 `PageResult.data.list,total`；前端分别读取，虽然不统一，但当前对应 |
| ID/状态 | 后端 Long 通过 JacksonConfig 转字符串；前端使用 string。楼宇 genderLimit=0/1/2、status=0/1；房型 1/2/3 和学生性别/状态前后端一致。接口允许任意数值，只有页面选项约束，不代表后端强制校验 |
| 逻辑删除 | 房间/学生有 @TableLogic isDeleted；**本机楼宇表也有 is_deleted，但 BizBuilding 没有该字段**，默认 BaseMapper 删除是物理删除、查询不自动排除已删除楼宇 |
| 时间字段 | 三张 CRUD 表本机有 CURRENT_TIMESTAMP 默认值和 ON UPDATE。实体没有自动填充注解；当前依靠数据库默认行为，不能说 Java 自动填充已覆盖这些实体 |
| 数据库版本化 | `sql/youlai-admin.sql` 只有底座表，`USE smart_dorm` 且有 DROP TABLE；不包含四张 biz 表，也不包含三个业务菜单。**不能拿该脚本覆盖现有库** |
| 本机菜单 | 楼宇/房间/学生分别为 `system/building/index`、`system/room/index`、`system/student/index`，route_path 为 building/room/student；perm 为空；查询未发现 biz:* 权限节点 |
| Mock | 前端存在底座 mock 目录，但 dev 的 VITE_MOCK_DEV_SERVER=false；业务页调用真实 API，未发现校舍模拟数据。页面不等于成功联调 |

本机结构摘要（只读核实）：

- biz_building：id、name、code、gender_limit、floor_count、manager_id、status、remark、create_time、update_time、is_deleted；仅主键索引。
- biz_room：id、building_id、room_number、floor_num、capacity、occupied_count、room_type、status、create_time、update_time、is_deleted；主键与普通 idx_building_id。
- biz_student：id、student_no、name、gender、college、major、phone、id_card、status、create_time、update_time、is_deleted；主键与唯一 uk_student_no（不排除软删除行）。
- biz_accommodation：id、student_id、room_id、bed_number、check_in_date、check_out_date、status、create_time、update_time、is_deleted；主键与普通 idx_student_id/idx_room_id，无有效住宿唯一约束、CHECK 或外键。本轮探测采用 status=1/is_deleted=0 作为候选有效记录；正式状态转换仍需在业务实现时确认。

## 5. 问题清单与优先级

P0 表示阻塞运行/交付，P1 表示权限或数据正确性高风险，P2 表示后续维护/体验问题。类别区分已确认缺陷、待验证风险和可选改进。

| 类别/问题 | 触发场景 | 代码或数据证据 | 实际影响 | 建议方案 | 优先级 | 验证方法 |
| --- | --- | --- | --- | --- | --- | --- |
| 已确认：前端构建失败（本轮修复） | 构建房间页的两个楼宇下拉框 | room/index.vue el-option 的 value 接收可选 BuildingItem.id | vue-tsc 两处 TS2322，构建无法完成 | 过滤没有有效字符串主键的选项，收窄局部类型 | P0 | npm run build、type-check |
| 已确认：新环境无法还原业务库 | 按仓库 SQL 初始化 | sql/youlai-admin.sql 无 biz 表/菜单；本机存在四表和三菜单 | 换电脑不能还原业务，脚本还会重建底座表 | 导出仅结构快照，设计增量迁移与独立测试库 | P0 | 空测试库迁移、字段/索引/菜单核对；本轮未执行迁移 |
| 已确认：缺 Redis 阻止后端启动 | 本地启动完整 Spring 上下文 | baseline-startup-confirmed.log，Redisson 连接 127.0.0.1:6379 失败 | 无法验证登录及业务 HTTP 闭环 | 准备专用本地 Redis，与现有业务库隔离测试 | P0 环境 | 启动成功日志、健康端点、认证测试 |
| 已确认：业务权限缺失（本轮修复） | 普通已登录用户直接调用业务接口，不依赖页面菜单 | SecurityConfig.anyRequest().authenticated()；三个 Biz 控制器原来没有 PreAuthorize | 原调用路径可读学生完整信息、修改/删除业务数据 | 复用 ss.hasPerm，对 12 个入口按模块/动作校验，前端同步按钮权限 | P1 | 修改前 72 用例中 48 失败，修改后 72 全通过；拒绝时业务 Service 零调用 |
| 已确认：楼宇删除语义不符 | 删除楼宇，或查询已有软删除标记行 | BizBuilding 缺 isDeleted；deleteByIds→removeByIds；本机表有 is_deleted | 物理删除、逻辑删除过滤缺失；房间可能悬空 | 增补逻辑删除映射、关联保护，并测试删除/恢复规则 | P1 | MyBatis 实际 SQL 与隔离库关联记录；本轮仅映射链确认，未对真实记录执行删除 |
| 已确认：业务字段可直接写入且缺校验 | 请求伪造 id/isDeleted/时间，或负容量、超额人数、无效楼宇 | 三控制器 @RequestBody 实体；RoomService.add/update 直存 | 非法业务状态可能持久化，房间可超额；本机表无 CHECK/外键 | 使用明确 Form/VO、Bean Validation、服务层关联校验，入住人数由业务维护 | P1 | 临时表已接受 capacity=4/occupied_count=5；完整 HTTP/持久化测试待补 |
| 已确认：住宿表没有唯一分配兜底 | 插入同学生两条有效住宿、同房同床两条有效住宿 | 本机约束仅主键；临时表探测 accepted_rows 均为 2 | 将来直接写入会出现冲突；不能声称已有入住服务存在并发 bug，因为服务不存在 | 明确状态后采用有效记录唯一键、事务及房间/床位锁；冲突转换业务错误 | P1（实现住宿前） | 当前顺序重复插入已复现；并发事务、重复请求、失败回滚待有业务实现后测试 |
| 已确认行为、规则待定：学号软删后不可重建 | 删除学生后按同学号新建 | uk_student_no 只覆盖 student_no，不排除删除行 | 临时表中新有效记录插入被唯一键拒绝；可能应恢复旧记录而非新建 | 确定学号永久唯一还是允许重建；不要直接追加 is_deleted 联合唯一键而忽略多次删除 | P1 | 本轮 INSERT IGNORE 后有效行数为 0；下一轮验证产品规则 |
| 已确认：验证码被绕过 | 账号密码登录 | CaptchaValidationFilter.doFilterInternal 内 validate 被注释；这是接手前已有修改 | 图形验证码失去效力 | 保留现场；后续恢复校验，测试通过测试配置/替身获得验证码，而非生产代码注释 | P1 | 缺失/错误/过期/正确验证码及请求体可重复读取测试 |
| 已确认：底座存在固定验证值 | 短信/邮件验证调用 | AuthService、UserServiceImpl 的 TODO 与固定验证码逻辑 | 对外启用这些入口时不能作为安全验证 | 按需禁用演示入口或接入真实随机码/发送服务并测试 | P1（启用前） | 验证固定值拒绝、失效/次数限制；本轮未发送短信/邮件 |
| 待验证：分页可绕过限制 | pageSize=-1 或极大值 | 三控制器参数无范围校验；MybatisConfig 分页插件无 maxLimit | MyBatis-Plus 特定负值路径可不分页；目前无端到端负载测量 | 入参范围 1..合理上限、插件兜底、稳定排序 | P1/P2 | 隔离库实际 SQL、异常响应、分页边界测试 |
| 已确认：页面流程边角缺口 | 楼宇超过 100 条、筛选时停留后页、编辑后再新增 | room.loadBuildings 固定 100；building/room.handleQuery 不重置页码；building 有两个操作列，handleAdd 未清 managerId | 楼宇选项不全、筛选空页、潜在表单残留 | 选项检索/分页、区分查询与翻页、表单完整重置 | P2 | UI 场景回归；本轮未扩大范围修复 |
| 待验证：测试/依赖迁移残留 | 恢复完整应用上下文后 | AuthControllerTest 注入 com.fasterxml ObjectMapper，而应用配置 tools.jackson；Spring Boot 4 与 springdoc 2.8.9 组合 | 可能出现 Bean/接口文档兼容问题；当前先被环境阻塞，不能断言已失败 | 准备环境后逐项运行确认，不批量升级 | P2 | 认证测试、/v3/api-docs 与 /doc.html |
| 可选：统一分页响应、改善表单交互 | 后续新增模块 | records 与 list 两种格式；无提交中保护 | 增加理解/维护成本，但当前并非接口不匹配 | 在稳定契约前提下按需统一 | P2 | 契约测试、双击提交测试 |

未发现有证据的 N+1 核心业务查询；现有分页多为单表 wrapper。没有进行性能优化或压测，不声明 QPS、延迟提升或高并发能力。

## 6. 本轮实际修改

### 6.1 修复房间页构建障碍

前端 `src/views/system/room/index.vue` 新增局部 BuildingOption 类型，并在 loadBuildings 中过滤非字符串/空 id。避免 undefined 传给 el-option，没有改成虚构的空 ID，也没有用非空断言掩盖风险。保持通用 BuildingItem 可选字段契约。

验证：原 npm run build 在两个 el-option 报 TS2322；最终 vue-tsc + Vite 完整构建通过；预览构建产物返回 HTTP 200 并包含 Vue 挂载元素。没有浏览器点击或后端联调验证。中途尝试把公共 BuildingItem.id 改为必填导致 Element Plus DefaultRow 类型错误，该尝试已撤销，不是最终方案。

### 6.2 补齐业务接口授权

- `BizBuildingController`、`BizRoomController`、`BizStudentController` 各四个入口增加 `@PreAuthorize("@ss.hasPerm('biz:模块:动作')")`。
- 模块：building/room/student；动作：list/create/update/delete。查询也校验，避免仅保护写接口而暴露学生数据。
- 前端三个页面新增/编辑/删除按钮接入现有 v-has-perm。按钮隐藏是交互辅助，服务端方法拦截才是安全边界。
- `src/test/java/com/youlai/boot/system/controller/BizControllerPermissionTest.java`：真实 Spring @EnableMethodSecurity 代理、真实 PermissionService 与 SecurityContext；仅权限存储端口和业务 Service 为 Mockito 替身，不启动 dev 配置、不访问数据库或 Redis。
- 12 个入口 × 6 种场景 = 72：已登录无权限、精确授权、其他模块权限、同模块其他动作权限、ROOT、匿名方法调用。拒绝时验证三个业务服务都没有被调用；允许时验证只触发一次服务调用。
- 修改前 48 个拒绝用例失败、24 个允许用例通过；修改后 72/72 通过。48 包含 12 个匿名方法调用用例，这是方法层纵深防御验证；原 HTTP 过滤器本来就要求认证，**不等于原来匿名用户能直接通过 HTTP 访问**。

**本机授权配置尚需执行**：现有菜单没有 biz 权限节点，本轮不擅自改变角色授权。ROOT 沿用既有超级管理员规则；其他角色即使能看到业务菜单，也需要显式授予相应权限，否则接口返回权限不足。这是修复后的预期收紧。

使用已有菜单/角色管理功能，在三个业务菜单下配置按钮权限节点：

| 菜单组件 | 需要配置的权限 |
| --- | --- |
| system/building/index | biz:building:list、biz:building:create、biz:building:update、biz:building:delete |
| system/room/index | biz:room:list、biz:room:create、biz:room:update、biz:room:delete |
| system/student/index | biz:student:list、biz:student:create、biz:student:update、biz:student:delete |

按最小权限分配角色，不默认给 ADMIN/GUEST 全部权限。房间页面加载楼宇下拉还依赖 biz:building:list。授权变更通过已有角色管理服务完成，以同步权限缓存；重新登录/刷新权限后联调。前端现有指令按精确权限检查，建议配置上述精确值，不依赖后端支持但前端不展开的通配符。

局限：测试没有覆盖 Token HTTP 过滤链、真实权限缓存、数据库变更、字段脱敏或宿管楼宇数据范围。拥有 student:list 的角色仍可收到实体中的证件字段；下一轮应设计 VO 与字段最小化。

## 7. 实际检查记录

日志都在后端 target/ 下（被 Git 忽略；日志可能包含本机路径，不作为可发布文档）。以下摘要可在清理 target 后保留。

| 检查/命令 | 结果 | 说明/证据 |
| --- | --- | --- |
| 两仓库 git status --short、git log | 完成 | 接手前已记录未提交内容，无自动提交 |
| PATH java -version / mvn -version | 初始不可直接构建 | 默认 Java 8，mvn 未在 PATH；改用本机 JDK21/IDEA Maven |
| Maven 离线 package，指定 D:/maven-repo | 失败 | 缓存为旧项目，缺 Boot4 父 POM；不是源码错误 |
| 默认 ~/.m2 离线 -DskipTests package | 主源码与测试源码编译通过，打包阶段失败 | 缺 Surefire 插件依赖；baseline-build.log |
| 联网 -B -DskipTests package | **通过（修改前后端基线）** | 补齐既有插件依赖，未改 pom；baseline-build-online.log |
| 新增权限测试，修改控制器前 | **预期失败：72/48 failures/0 errors** | permission-before.log，缺拦截确实可复现 |
| 修改后 -Dtest=BizControllerPermissionTest test | **72 通过** | permission-after-confirmed.log；沙箱内曾出现 Mockito attach 管道权限限制，沙箱外复核通过 |
| 修改后 -Dtest=BizControllerPermissionTest package | **通过：72 测试通过，JAR 打包成功** | verified-package.log；仅指定权限测试，不冒充全量测试 |
| 原 AuthControllerTest + 全量 mvn test（本机隔离参数） | **未完成，主动中止** | full-test-local.log：验证库 dorm_verification 不存在，Druid 重试；6 个认证测试未验证，不报告通过 |
| java -jar 基线 jar，本机隔离参数 | **启动失败** | baseline-startup-confirmed.log：Redis 127.0.0.1:6379 Connection refused；未启动生产配置/服务 |
| 本机 TCP / SELECT VERSION / information_schema | **通过** | MySQL 3306 可达、9.5.0、四业务表；Redis 6379/后端8000不可达。最初沙箱内 TCP 结果不可靠，已在获准环境复核 |
| 菜单元数据查询 | 第二次成功 | 第一次误用 path 列报 Unknown column；改为实际 route_path 后确认三菜单。均只读 |
| 临时表 schema probe | **成功复现风险** | 同学生有效记录=2，同床有效记录=2，超容量行=1，软删学号重建有效行=0；无真实业务行参与，不是业务测试通过 |
| 前端 npm run build（修改前） | **失败** | room/index.vue 两处 TS2322 |
| 前端 npm run build（修复后） | **通过** | frontend-build-after.log；Vite 构建成功，不把构建耗时当作性能成果 |
| 前端 Prettier 三个修改页面 | **通过** | 初次检查存在格式警告，局部格式化后通过 |
| 前端 npm run type-check（最终格式化后） | **通过** | vue-tsc --noEmit，无错误 |
| Vite preview 本机 HTTP smoke | **通过** | HTTP 200、包含 Vue app 挂载元素，服务随后关闭；未进行浏览器交互 |
| git diff --check | 原有问题保留 | BizBuilding.java 原有 EOF 空行警告；本轮未修改该实体 |

## 8. 下次启动与验证

先准备独立测试库/Redis，勿直接执行包含 DROP TABLE 的原初始化脚本覆盖 smart_dorm。当前本机业务库是恢复线索，应先备份仅结构，核实规则再迁移。没有 Docker CLI 可用的证据，不能假设 docker compose 已可运行。

PowerShell 后端工具链：

```powershell
Set-Location D:\JavaProjects\youlai-boot
$env:JAVA_HOME = 'D:\program\Java'
$maven = 'D:\IntelliJ IDEA 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd'
& $maven -B -Dtest=BizControllerPermissionTest test
& $maven -B -Dtest=BizControllerPermissionTest package
```

Maven 使用默认用户 .m2 缓存；首次缺依赖需联网。权限单测不需要服务，适合首先验证工具链。Java 进程在受限沙箱中可能需要允许本地通信（Mockito attach/Surefire 管道/Netty），不要把权限限制误判成业务故障。

运行完整后端前通过环境变量或不入库的本地配置提供：SPRING_DATASOURCE_URL、SPRING_DATASOURCE_USERNAME、SPRING_DATASOURCE_PASSWORD、SPRING_DATA_REDIS_HOST、SPRING_DATA_REDIS_PORT、必要的 Redis 密码；文件存储可设 FILE_STORAGE_TYPE=local。密码在本机交互输入/密钥管理中配置，不写入命令示例或进度文档。默认 profile=dev、端口8000，prod 未验证。

```powershell
# 完成上述本地测试库/Redis 配置后再运行
& $maven spring-boot:run
# 或
& "$env:JAVA_HOME\bin\java.exe" -jar target\youlai-boot.jar
```

失败基线的隔离启动方式（只用于确认缺服务，不是正常启动指令）：

```powershell
& "$env:JAVA_HOME\bin\java.exe" -jar target\youlai-boot.jar --server.port=0 `
  '--spring.datasource.url=jdbc:mysql://127.0.0.1:3306/dorm_verification?connectTimeout=1500&socketTimeout=1500' `
  --spring.datasource.username=verification_only --spring.datasource.password=verification_only `
  --spring.data.redis.host=127.0.0.1 --spring.data.redis.port=6379 `
  --spring.data.redis.connect-timeout=1s --spring.data.redis.timeout=1s --file-storage.type=local
```

verification_only 是无效测试占位值，不是实际凭据。执行 Maven 的 Windows .cmd 时避免在 -D URL 中使用未妥善传递的 `&`，建议通过进程环境变量传完整 URL。全量测试依赖的测试库尚未建立，本轮中止了重复失败的连接等待，没有去建库或改真实数据。

前端：

```powershell
Set-Location D:\JavaProjects\vue3-element-admin
# 已有 node_modules 时可直接执行；首次安装遵循仓库 pnpm-lock.yaml
pnpm install --frozen-lockfile
pnpm run type-check
pnpm run build
pnpm run dev
```

已有依赖环境也可用 npm run 执行相同脚本（本轮如此）。dev 默认端口3000，API 前缀 `/dev-api`，vite.config.ts 代理到 `.env.development` 的 VITE_APP_API_URL；本次核实其指向本地8000且 VITE_MOCK_DEV_SERVER=false。原 dev 配置 server.open=true，常规运行会打开浏览器；本轮 smoke 显式关闭自动打开。

临时表复现命令：在本机 MySQL 客户端交互登录后执行，密码由客户端交互输入，不写进命令：

```sql
USE smart_dorm;
SOURCE D:/JavaProjects/youlai-boot/sql/verification/accommodation-schema-probe.sql;
```

该脚本只复制结构到 TEMPORARY 表并插入虚构值，不读取真实业务行；退出连接即释放。它用于记录现状，未来约束修复后应拒绝相应插入，不能当成要求永远通过的测试。

## 9. 接下来最值得做的三件事

1. **把环境和数据库纳入可重建流程**：保存本机四张业务表及菜单的结构快照，建立独立测试库和 Redis 配置；基于现状设计非破坏性迁移，落实本轮新增的12项权限授权；跑通登录和三个 CRUD 的实际 HTTP 流程。原因：目前 Git 无法复现个人开发现场，环境测试仍被阻塞。
2. **修复基础数据的一致性与边界**：楼宇逻辑删除/关联保护、Form/VO 校验和字段最小化、房间容量与楼宇有效性、学号删除/恢复规则、分页上限；恢复验证码安全路径。使用隔离 MySQL 测试正常/非法/删除后状态，不能仅 Mock Service。
3. **完成住宿分配/退宿的最小闭环，再测并发**：先定义状态、可用床位、性别/停用规则；事务内分配、以数据库唯一约束兜底同学生/同床位冲突，明确容量维护和退宿释放；测试正常入住、重复请求、两个并发学生抢同床、同学生抢不同床、超容量、退宿重入与异常回滚。唯一键/锁方案需基于确定的业务规则及已有数据评估，暂不执行破坏性迁移，也不引入 MQ/微服务/Agent。

## 10. 重读入口与面试边界

建议依次阅读：本文件 → BizRoomController/BizRoomServiceImpl/BizRoom → 三个前端 API 与 room/index.vue → BizStudentController/PageResult → SecurityConfig/PermissionService → BizControllerPermissionTest → sql/verification/accommodation-schema-probe.sql。

可解释的本轮成果：

> 我在已有 Spring Boot 管理底座上补齐校舍模块时，发现新控制器只做登录认证，缺少操作授权。菜单隐藏不能阻止直接调用接口，因此复用现有 Spring Security 方法权限体系，对楼宇、房间、学生的12个接口按模块和动作加权限校验，并同步前端按钮。通过真实 Spring 代理和权限组件运行72个参数化回归用例，修改前48个拒绝场景失败，修改后全部通过；拒绝请求没有进入业务服务。数据库与缓存被替身隔离，所以这证明方法授权边界，而不是生产登录链路、数据级权限或高并发能力。

另一个可以说明的发现：在本地 MySQL 临时表上验证了住宿重复分配和容量约束缺失，但住宿业务代码尚未实现，这是问题识别结果，不是已经解决的并发成果。

简历候选（不要加未经测量的数据）：

> 为智慧校舍管理系统楼宇、房间、学生12个业务接口补齐基于 Spring Security 的操作权限校验，同步前端按钮权限，编写并通过72个方法授权回归用例，覆盖无权限、精确授权、跨模块/动作隔离及超级管理员场景。

不得写成：已实现住宿并发分配、解决所有越权、完成生产上线、响应时间提升、支持某个 QPS、已接入 AI Agent。

## 11. 2026-10-18 安全保存与发布准备

详细文件、敏感信息分类、恢复步骤与 GitHub 剩余操作见 [publication-status.md](publication-status.md)。本轮没有新增住宿/Agent业务，没有对实际 smart_dorm 执行迁移。

- 已备份两仓库原历史、暂存区、工作区与初始未跟踪文件。15个 AM 文件提交采用完整工作区实现，原骨架暂存内容可从备份恢复。
- 原分支保留；整理后建立 codex/github-save 独立根快照作为当前发布开发分支。它不带旧敏感历史，两个原 origin 关系不变，无强推、无仓库重新初始化。
- 配置真实凭据改为环境变量/忽略的 config/application-local.yml；本地覆盖只在 dev 导入，不入 JAR；生成独立本地 JWT 签名值。prod 不导入本地配置。需要轮换的既有数据库/外部服务凭据列表见保存记录。
- 已保存四业务表纯结构快照、16张底座表的无数据初始化、必要系统定义和3菜单/12权限追加式迁移。原含 DROP 和示例账户的 sql/youlai-admin.sql 在发布树中停用；旧内容仍在本地原历史/备份。不得再按上文旧基线描述导入原脚本。
- 隔离 MySQL 验证通过：20表、4张空业务表、3菜单、12权限、串行重跑、已有菜单保留、无自动授权、管理员初始化保护；测试库已移除。数据库版本化已推进，但住宿约束问题仍未修复。
- 本轮后端72个权限用例及打包通过，前端构建通过，5个公开 YAML 解析通过。完整登录/CRUD 仍未验证，不能宣称项目整体可运行。
- README 已整理项目用途、上游来源、真实进度、启动/环境变量/初始化顺序和测试边界；保留许可证及署名。
- GitHub CLI 未找到，无法确认账号。两个私有目标仓库尚未创建，没有 remote URL/远程提交号；只完成本地整理与提交。安装并登录 gh 后，按保存记录运行守卫脚本核实账号/目标名称、创建 private 仓库、添加 personal 远程并推送当前发布分支。后端原 feature/dormitory-management 和前端原 master 不作为推送目标。
