param(
    [Parameter(Mandatory=$true)]
    [ValidateSet('smart-dorm-backend','smart-dorm-frontend')]
    [string]$RepoName
)
$ErrorActionPreference = 'Stop'
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { throw 'Install GitHub CLI, then run gh auth login first.' }
if ($env:GH_HOST -and $env:GH_HOST -ne 'github.com') { throw 'GH_HOST points elsewhere; use an explicit github.com CLI session.' }
& gh auth status --hostname github.com *> $null
if ($LASTEXITCODE -ne 0) { throw 'GitHub CLI is not authenticated. Run gh auth login.' }
$owner = (& gh api --hostname github.com user --jq .login).Trim()
if ($LASTEXITCODE -ne 0 -or $owner -notmatch '^[A-Za-z0-9-]+$') { throw 'Cannot verify authenticated GitHub owner.' }
$branch = (& git branch --show-current).Trim()
if ($LASTEXITCODE -ne 0 -or $branch -ne 'codex/github-save') { throw 'Switch to the audited codex/github-save branch first.' }
if (& git status --porcelain) { throw 'Working tree/index is not clean. Review and commit before publishing.' }
if ($RepoName -eq 'smart-dorm-backend' -and -not (Test-Path -LiteralPath 'pom.xml')) { throw 'Not the backend repository.' }
if ($RepoName -eq 'smart-dorm-frontend' -and -not (Test-Path -LiteralPath 'package.json')) { throw 'Not the frontend repository.' }
$rootCommit = (& git rev-list --max-parents=0 HEAD).Trim()
$rootSubject = (& git show -s --format=%s $rootCommit).Trim()
if ($rootSubject -ne 'chore: publish sanitized smart dorm baseline') { throw 'Unexpected history root; refusing to publish upstream history.' }
foreach ($commit in @(& git rev-list HEAD)) {
    & python (Join-Path $PSScriptRoot 'audit_publication.py') --ref $commit
    if ($LASTEXITCODE -ne 0) { throw 'Publication history audit failed.' }
}
$repo = "$owner/$RepoName"
# Any same-name repository is reviewed manually, even if empty. Never overwrite it.
& gh repo view $repo --json nameWithOwner,isPrivate,isEmpty *> $null
if ($LASTEXITCODE -eq 0) { throw "Repository $repo already exists. Verify its purpose before configuring any push." }
# A transient/API error here cannot cause overwrite: create must succeed independently.
& gh repo create $repo --private --description 'Smart Dorm personal project; sanitized baseline; derived from YouLai open source'
if ($LASTEXITCODE -ne 0) { throw 'Creation failed; no Git remote or push performed.' }
$info = (& gh repo view $repo --json nameWithOwner,isPrivate,isEmpty | ConvertFrom-Json)
if (-not $info.isPrivate -or -not $info.isEmpty -or $info.nameWithOwner -ine $repo) { throw 'Owner/privacy/empty-repository verification failed.' }
$url = "https://github.com/$repo.git"
$remotes = @(& git remote)
if ($remotes -contains 'personal') {
    $existing = (& git remote get-url personal).Trim()
    if ($existing -ne $url) { throw 'Existing personal remote differs; not replacing it.' }
} else {
    & git remote add personal $url
    if ($LASTEXITCODE -ne 0) { throw 'Could not configure personal remote.' }
}
& git -c push.followTags=false push --set-upstream personal 'HEAD:refs/heads/codex/github-save'
if ($LASTEXITCODE -ne 0) { throw 'Push failed. Repository may exist; inspect before retrying (never force push).' }
$local = (& git rev-parse HEAD).Trim()
$remote = (& git ls-remote personal 'refs/heads/codex/github-save') -split '\s+'
if ($LASTEXITCODE -ne 0 -or $remote[0] -ne $local) { throw 'Remote commit verification failed.' }
Write-Output "Verified private repository: https://github.com/$repo"
Write-Output "Branch: codex/github-save; commit: $local"
