"""Verify published SQL in a newly created, disposable LOCAL MySQL database.

Requires mysql on PATH (or MYSQL_CLIENT), MYSQL_USER and MYSQL_PWD in the process
environment. Never reads application credentials or real user/business rows.
"""
import os
import pathlib
import re
import secrets
import subprocess

ROOT = pathlib.Path(__file__).resolve().parents[1]
host = os.environ.get("MYSQL_HOST", "127.0.0.1")
if host not in {"127.0.0.1", "localhost", "::1"}:
    raise SystemExit("Only loopback MySQL hosts are allowed.")
user = os.environ.get("MYSQL_USER")
if not user:
    raise SystemExit("Set MYSQL_USER and MYSQL_PWD privately before running.")
client = os.environ.get("MYSQL_CLIENT", "mysql")
database = "smart_dorm_verify_" + secrets.token_hex(8)
assert re.fullmatch(r"smart_dorm_verify_[a-f0-9]{16}", database)
args = [client, "--host=" + host, "--user=" + user, "--connect-timeout=3",
        "--batch", "--skip-column-names", "--default-character-set=utf8mb4"]

def query(sql, selected=False):
    result = subprocess.run(args + (["--database=" + database] if selected else []),
                            input=sql.encode("utf-8"), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if result.returncode:
        # Report error code only; SQL text and connection settings can be private.
        codes = re.findall(rb"ERROR \d+", result.stderr)
        raise RuntimeError("MySQL verification failed: " + ", ".join(x.decode() for x in codes))
    return result.stdout.decode("utf-8").strip()

def source(path):
    text = (ROOT / path).read_text(encoding="utf-8")
    assert not re.search(r"(?im)^\s*(?:DROP\s+TABLE|USE\s+|TRUNCATE\s+)", text), path
    return query(text, True)

created = False
try:
    # No IF NOT EXISTS: an unexpected collision fails without touching that database.
    query(f"CREATE DATABASE `{database}` CHARACTER SET utf8mb4;")
    created = True
    source("sql/init/001-system-schema.sql")
    source("sql/init/002-system-definitions.sql")
    source("sql/schema/2026-10-17-business-snapshot.sql")
    assert query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE();", True) == "20"
    assert query("SELECT COUNT(*) FROM sys_user;", True) == "0"
    # Cover reuse of one existing menu without moving/overwriting it.
    query("INSERT INTO sys_menu (parent_id,tree_path,name,type,route_path,component) "
          "VALUES (1,'0,1','Existing building menu','M','building','system/building/index');", True)
    source("sql/migrations/20261017_01_dorm_menu_permissions.sql")
    count_before = query("SELECT COUNT(*) FROM sys_menu;", True)
    source("sql/migrations/20261017_01_dorm_menu_permissions.sql")
    assert query("SELECT COUNT(*) FROM sys_menu;", True) == count_before
    assert query("SELECT COUNT(*) FROM sys_menu WHERE perm LIKE 'biz:%';", True) == "12"
    assert query("SELECT COUNT(*) FROM sys_menu WHERE component IN "
                 "('system/building/index','system/room/index','system/student/index');", True) == "3"
    assert query("SELECT parent_id FROM sys_menu WHERE component='system/building/index';", True) == "1"
    assert query("SELECT COUNT(*) FROM sys_role_menu;", True) == "0"
    assert query("SELECT COUNT(*) FROM sys_user_role;", True) == "0"
    source("sql/init/003-bootstrap-admin.sql")
    assert query("SELECT COUNT(*) FROM sys_user;", True) == "0"
    # Synthetic BCrypt-shaped test hash with no known password; never real account data.
    synthetic_hash = "$2b$12$" + "".join(secrets.choice("./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz") for _ in range(53))
    bootstrap = (ROOT / "sql/init/003-bootstrap-admin.sql").read_text(encoding="utf-8")
    query("SET @bootstrap_username='schema-test-only', @bootstrap_password_hash='" + synthetic_hash + "';\n" + bootstrap, True)
    assert query("SELECT COUNT(*) FROM sys_user;", True) == "1"
    assert query("SELECT COUNT(*) FROM sys_user_role ur JOIN sys_role r ON r.id=ur.role_id WHERE r.code='ROOT';", True) == "1"
    query("SET @bootstrap_username='must-not-create', @bootstrap_password_hash='" + synthetic_hash + "';\n" + bootstrap, True)
    assert query("SELECT COUNT(*) FROM sys_user;", True) == "1"
    assert query("SELECT (SELECT COUNT(*) FROM biz_building)+(SELECT COUNT(*) FROM biz_room)"
                 "+(SELECT COUNT(*) FROM biz_student)+(SELECT COUNT(*) FROM biz_accommodation);", True) == "0"
    print("PASS: 20 tables; 4 empty business tables; 3 menus; 12 permissions; serial migration idempotence;")
    print("existing menu preserved; no automatic role grants; guarded bootstrap and repeat protection.")
finally:
    if created:
        query(f"DROP DATABASE `{database}`;")
        print("Disposable verification database removed; existing smart_dorm was not modified.")
