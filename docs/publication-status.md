# 私有仓库保存记录

日期：2026-10-18（Asia/Hong_Kong）。本轮只整理版本、配置、数据库结构和说明，没有开发 Agent 或住宿事务。

## 当前状态与历史策略

- 后端、前端仍是两个独立 Git 仓库，保留各自 .git、许可证及上游来源。
- 后端原开发分支 `feature/dormitory-management`，前端原分支 `master`。后端15个 AM 文件的 index 内容是四行骨架，工作区是完整实现；均已核对和备份，提交使用完整工作区实现。
- 两个 `origin` 均为原作者 Gitee 地址，没有推送到它们。不要执行 `git push origin`、`git push --all`、`git push --mirror` 或推送旧标签。
- 原分支保存整理后的本地检查点，旧历史不重写。另建立 `codex/github-save` 独立根快照，作为当前待发布开发分支；它与原检查点具有相同文件树，但不携带旧历史中的凭据、演示 JWT 或原提交者个人邮件。新提交采用项目维护者占位身份，不修改全局 Git 身份。
- 当前未找到 GitHub CLI，可检索的标准 gh 配置位置及 GH_TOKEN/GITHUB_TOKEN 也未提供可用登录。**无法核实当前 GitHub 账号，未创建远程仓库、未配置 personal 远程、未推送。** Git 的用户名不作为 GitHub 所有权证明。
- 拟创建私有仓库：`smart-dorm-backend` 和 `smart-dorm-frontend`。目前没有可返回的真实仓库链接。提交号在各仓库 `git log -1 --format=%H` 查询，最终回复提供实际值。

## 备份与恢复

修改前备份位于本机：

`C:/codexdata/visualizations/2026/10/17/01a148be-87ac-7303-b734-87d94cf11f7f/private-backups/smart-dorm-20261017/`

backend/frontend 各包含 history.bundle、index.zip、worktree.zip、index.patch、worktree.patch、原配置/refs/status 记录及 SHA256SUMS.json。bundle 验证成功。目录标签沿用开始备份时的日期；它们在仓库之外，**含旧秘密，仅本地保管，不能上传/分享**。index.zip 单独保留原暂存内容，worktree.zip 保存完整文件，包含初始未跟踪业务文件。

恢复应在另一个空目录克隆 history.bundle，选择对应原分支；按 SHA256SUMS 校验，再在该恢复副本应用 index.patch 到暂存区并解压 worktree.zip。不要在当前成果目录执行 reset/覆盖。原仓库的原分支也保留，没有删除 refs 或强制改写历史。

## 敏感信息检查（不记录原值）

扫描包含修改前工作区、index、两仓库所有可达历史的文本 blob（后端6345、前端7839），以及最终准备提交/发布的文件树。报告只记录位置与类型；规则扫描不能数学保证发现所有秘密，候选项已结合配置用途人工分类。

| 位置 | 问题类型与处理 |
| --- | --- |
| 后端 application-dev.yml / application-prod.yml | 实际本机数据库凭据和可复用连接/签名/外部服务配置。已改为环境变量。实际本机数据库连接移入忽略的 config/application-local.yml，dev 才读取，不进 JAR |
| 历史 application-*.yml，包括旧 youlai-application 路径、application-generator.yml、测试配置 | 历史字面密码、签名配置、云访问密钥形态的候选值；无法证明仍有效，保守不随发布历史上传。原历史只保留本机/备份 |
| docker/docker-compose.yml | 字面服务密码改为必填环境变量；移除自动导入整个旧 SQL 目录的挂载 |
| sql/youlai-admin.sql（原版本） | DROP TABLE、演示账号密码哈希、电话/邮件样例。发布版本改为停用说明；提取无用户记录的底座结构和必要元数据，不复制原账号数据 |
| frontend mock/auth.mock.ts 及旧 mock/user.ts 历史 | 演示 JWT，当前替换为明确无效的 Mock 字符串；旧历史不上传。没有宣称这些演示值是用户真实生产 Token |
| 前端 .env.development / .env.production | 可公开的标题、代理、Mock 开关，保留跟踪并调整为本地项目。没有模型密钥；本地覆盖文件忽略 |
| 前端 deploy.mjs | 原默认上游主机移除；必须显式提供部署目标，保留 SSH 主机校验，本轮不部署 |
| 上游测试/代码示例及 mock | 固定演示验证值、虚构电话号码和类型字段是扫描候选，不当成已发现的真实个人记录。功能风险在 project-status.md 继续记录；没有从本机导出用户/学生业务数据 |
| Git 作者元数据 | 原历史含上游及本机个人邮箱；原历史不推送，发布根快照使用项目维护者占位身份 |

### 撤销/轮换建议

1. 本机 dev/prod 中曾保存的数据库密码：按实际数据库账号轮换；本轮仅迁移配置，没有修改数据库账户。若在别处复用也应更换。
2. 原 JWT 签名值：若任何运行实例仍在使用，应更换并使旧会话失效。本轮已为忽略的本地 dev 配置生成独立新随机签名值，prod 必须通过 JWT_SECRET 提供自己的值。
3. Redis、MinIO、邮件授权码、OSS/SMS 云访问密钥、微信 AppSecret、XXL Job Token：若历史中的值实际属于你且曾启用，分别到服务方撤销/轮换；示例值不应继续用于真实服务。未擅自操作外部账号。
4. 未发现需要撤销的真实 GitHub/模型 Token。未来模型密钥只能放后端环境变量或服务端秘密管理，不能放 VITE_*。

已跟踪的配置文件在本轮提交中真正被替换为占位配置，而不是只往 .gitignore 添加规则。旧版本仍存在原历史，所以必须只推送隔离的发布分支。

## 数据库文件和验证

四张业务表由本机 mysqldump `--no-data --skip-add-drop-table --skip-triggers --no-tablespaces` 导出，移除当前自增计数，未导出账户/学生/住宿行。

纯结构快照、空库初始化、追加式菜单权限迁移已经分开，顺序和限制见 [sql/README.md](../sql/README.md)。保留原结构缺陷，不擅自增加住宿唯一约束或重构业务。菜单迁移不自动授权角色，也未应用到现有 smart_dorm。

本轮验证：

- 隔离 MySQL 验证通过：20张表、4张空业务表、3个业务菜单、12项权限；串行重跑无重复；已有菜单位置不变；没有自动用户/角色授权；管理员引导缺参/重复执行保护有效。验证库创建后删除，smart_dorm 未修改。
- 后端72个方法授权测试与打包通过；前端完整构建通过；5个公开 YAML 解析通过；部署脚本语法检查通过。
- 全量登录/CRUD 仍未完成，Redis/专用运行测试环境的问题沿用上轮记录。Docker CLI 不可用，未声称 Compose 实际启动通过。
- 最终工作区/index/发布树用 scripts/audit_publication.py 复查，不包含 target、node_modules、dist、日志、私有配置、密码哈希或业务转储。原有 BizBuilding.java 文件末尾空行作为原业务文件细节保留，不冒充此次新增问题。

## GitHub 登录后的剩余操作

安装官方 GitHub CLI 后，用户在自己的终端完成登录（不要把 Token 发到聊天）：

```powershell
gh auth login --hostname github.com --git-protocol https
gh auth status
```

确认这是自己的账号后，可以运行本轮提供的守卫脚本。它读取当前认证账号作为所有者，只创建私有空仓库；同名仓库存在时停止要求核实用途，不覆盖。它会保留 origin，仅添加 personal，并只推送当前 codex/github-save 分支，不推送标签或原历史，最后核对远程提交号。

```powershell
Set-Location D:\JavaProjects\youlai-boot
.\scripts\publish-private.ps1 -RepoName smart-dorm-backend

Set-Location D:\JavaProjects\vue3-element-admin
& D:\JavaProjects\youlai-boot\scripts\publish-private.ps1 -RepoName smart-dorm-frontend
```

脚本尚未在真实 GitHub 账号上执行，只做语法/本地守卫检查。若同名仓库存在或创建成功后推送失败，先查询仓库 owner、visibility、内容和用途，不要强推或重新运行覆盖。成功后把真实仓库链接、分支和提交号补充到项目记录；云端保存目前仍是未完成项。
