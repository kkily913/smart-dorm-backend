"""Fail-closed checks for the intended Git snapshot; prints locations, never values.

Usage: python scripts/audit_publication.py --index
       python scripts/audit_publication.py --ref HEAD --repo ../vue3-element-admin
Heuristic checks complement manual review; not a guarantee that every secret is detected.
"""
import argparse
import pathlib
import re
import subprocess

p = argparse.ArgumentParser()
p.add_argument("--repo", default=".")
mode = p.add_mutually_exclusive_group(required=True)
mode.add_argument("--index", action="store_true")
mode.add_argument("--ref")
mode.add_argument("--worktree", action="store_true")
args = p.parse_args()
repo = pathlib.Path(args.repo).resolve()

def git(*cmd):
    return subprocess.check_output(["git", "-C", str(repo), *cmd])

entries = []
if args.index:
    for entry in git("ls-files", "--stage", "-z").decode().split("\0"):
        if not entry:
            continue
        meta, path = entry.split("\t", 1)
        _, oid, stage = meta.split()
        if stage != "0":
            raise SystemExit("Unmerged index: publication forbidden")
        entries.append((path, git("cat-file", "blob", oid)))
elif args.ref:
    for entry in git("ls-tree", "-r", "-z", args.ref).decode().split("\0"):
        if not entry:
            continue
        meta, path = entry.split("\t", 1)
        _, kind, oid = meta.split()
        if kind != "blob":
            raise SystemExit("Submodule requires separate manual publication review")
        entries.append((path, git("cat-file", "blob", oid)))
else:
    for path in sorted(set(git("ls-files", "--cached", "--others", "--exclude-standard", "-z").decode().split("\0")) - {""}):
        if (repo / path).is_file():
            entries.append((path, (repo / path).read_bytes()))

rules = {
    "private-key": r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----",
    "provider-token": r"\b(?:gh[pousr]_[A-Za-z0-9]{25,}|github_pat_[A-Za-z0-9_]{25,}|sk-(?:proj-)?[A-Za-z0-9_-]{25,}|AKIA[0-9A-Z]{16})\b",
    "literal-jwt": r"\beyJ[A-Za-z0-9_-]{12,}\.[A-Za-z0-9_-]{12,}\.[A-Za-z0-9_-]{12,}\b",
    "persisted-password-hash": r"\$2[aby]\$\d{2}\$[./A-Za-z0-9]{53}",
}
failures = []
for path, data in entries:
    if re.search(r"(^|/)(target|node_modules|dist|logs|private-backups|uploads)/|\.log$|\.bundle$|\.dump$|\.sql\.gz$|\.pem$|\.key$|(^|/)application-local\.yml$|(^|/)\.env$|\.local$", path):
        failures.append((path, 0, "private-or-generated-file"))
    if b"\0" in data[:8192]:
        continue
    text = data.decode("utf-8", errors="replace")
    for number, line in enumerate(text.splitlines(), 1):
        # Do not match pattern source literals against themselves.
        if path.endswith("scripts/audit_publication.py"):
            continue
        for kind, pattern in rules.items():
            if re.search(pattern, line):
                failures.append((path, number, kind))
        if path.endswith((".yml", ".yaml", ".properties")):
            match = re.match(r"\s*(password|secret-key|access-key(?:-id|-secret)?|accessKeyId|accessKeySecret|accessToken|secret)\s*[:=]\s*(.*?)\s*$", line)
            if match:
                value = match[2].split(" #", 1)[0].strip().strip("\"'")
                if value and not value.startswith("${"):
                    failures.append((path, number, "literal-server-credential"))
        if pathlib.PurePosixPath(path).name.startswith(".env") and re.match(r"\s*VITE_\w*(PASSWORD|SECRET|TOKEN|API_KEY|PRIVATE_KEY)\w*\s*=\s*\S", line, re.I):
            failures.append((path, number, "browser-secret-variable"))
        if path.endswith(".sql") and re.match(r"\s*INSERT\s+(?:IGNORE\s+)?INTO\s+`?biz_", line, re.I):
            failures.append((path, number, "business-row-export"))

for path, number, kind in failures:
    print(f"BLOCK {path}:{number} [{kind}]")
print(f"Reviewed {len(entries)} files; blocking findings: {len(failures)}")
raise SystemExit(1 if failures else 0)
