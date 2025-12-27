# Project Documentation Standards & Rules

> **Location**: `.agent/RULES_AND_WORKFLOWS.md`  
> **Purpose**: Consolidated rules with GENERIC regex patterns for any project

## Core Rule: Module Documentation Structure

```
{project-prefix}-{module-name}/
├── src/
├── docs/
│   ├── README.md
│   ├── ARCHITECTURE.md  
│   ├── images/
│   └── examples/
└── README.md (redirects to docs/)
```

## Regex Patterns (Generic - Works for ANY project)

```regex
# Project docs: ^{project-name}-docs$
# API modules: ^{project}-\w+-api$
# Services: ^{project}-\w+-service$
# Providers: ^{project}-\w+-(provider|integration)$
# SDKs: ^{project}-\w+-sdk$
```

## Critical Exclusions ⚠️

**NEVER touch during refactoring**:
- `.agent/` folder and all its contents
- `.agent/RULES_AND_WORKFLOWS.md`
- `.agent/workflows/*.md`
- `.git/` folder
- `node_modules/`
- `target/` build folders

**Refactoring scripts MUST exclude these paths**:
```powershell
$excludePatterns = @(
    "\\.agent\\",
    "\\.git\\",
    "node_modules",
    "target",
    "build"
)

# Example usage in script:
$files | Where-Object { 
    $excluded = $false
    foreach ($pattern in $excludePatterns) {
        if ($_.FullName -match $pattern) { $excluded = $true; break }
    }
    -not $excluded
}
```

## Rules

### 1. Duplicate Handling ⚠️

**Markdown**: DELETE duplicates (single source of truth)  
**Images**: VERSION all iterations (keep complete history)

**Priority for MD files**:
1. `{project}-docs/` → **KEEP**
2. `{project}-{module}/docs/` → **KEEP**  
3. **Root level** → **MOVE to appropriate location** (see below)
4. Anywhere else → **DELETE**

**Image Versioning Strategy** (MANDATORY):

When generating or updating images:
- ✅ **ALWAYS version**: Never overwrite existing images
- ✅ **Keep history**: All versions preserved for reference
- ✅ **Naming**: `image_name_v1.png`, `image_name_v2.png`, `image_name_v3.png`
- ✅ **Latest**: Update markdown to reference latest version
- ❌ **NEVER delete**: Previous versions remain in `images/` folder

**Image Versioning Workflow**:
```powershell
# When generating a new version of an image
$imageName = "architecture_diagram"
$imageDir = "market-data-docs\architecture\images"

# Find existing versions
$existingVersions = Get-ChildItem -Path $imageDir -Filter "${imageName}*.png" | 
    Where-Object { $_.Name -match "_v(\d+)\.png$" }

# Determine next version number
if ($existingVersions.Count -eq 0) {
    # First version
    $newName = "${imageName}_v1.png"
} else {
    # Get highest version number
    $maxVersion = ($existingVersions | ForEach-Object {
        if ($_.Name -match "_v(\d+)\.png$") { [int]$matches[1] }
    } | Measure-Object -Maximum).Maximum
    
    $newVersion = $maxVersion + 1
    $newName = "${imageName}_v${newVersion}.png"
}

# Save new version
Copy-Item $sourceImage -Destination "$imageDir\$newName"

# Update markdown to reference latest
$mdFile = "market-data-docs\architecture\ARCHITECTURE.md"
$content = Get-Content $mdFile -Raw
$content = $content -replace "!\[([^\]]*)\]\(images/${imageName}[^)]*\)", "![`$1](images/$newName)"
Set-Content -Path $mdFile -Value $content

Write-Host "✅ Saved as version $newVersion"
Write-Host "✅ Previous versions preserved"
```

**Example**:
```
images/
├── architecture_diagram_v1.png    ← Initial version (kept)
├── architecture_diagram_v2.png    ← Second iteration (kept)
├── architecture_diagram_v3.png    ← Latest version (referenced in .md)
├── complete_flow_v1.png           ← Initial (kept)
└── complete_flow_v2.png           ← Latest (referenced in .md)
```

### 2. Root-Level Document Handling ⚠️

**Rule**: NO documentation at project root (except README.md)

**Allowed at root**:
- ✅ `README.md` (main entry point)
- ✅ `.gitignore`, `pom.xml`, `package.json` (build files)

**NOT allowed at root** (must be moved):
- ❌ Any `*.md` files (except README.md)
- ❌ Architecture docs
- ❌ Guide docs
- ❌ Analysis docs

**Where to move root-level docs**:

| File Type | Example | Move To |
|-----------|---------|---------|
| Architecture docs | REFACTORING_PLAN.md | `{project}-docs/architecture/` |
| Design docs | ARCHITECTURE_COMPARISON.md | `{project}-docs/architecture/` |
| Guides | SDK_GENERATION_ANALYSIS.md | `{project}-docs/guides/` |
| Project structure | DOCUMENTATION_STRUCTURE.md | `{project}-docs/guides/` |
| Implementation plans | implementation_plan.md | `{project}-docs/architecture/` |
| Completion summaries | REFACTORING_COMPLETE.md | `{project}-docs/architecture/` |

**PowerShell script for root cleanup**:
```powershell
# Find all root-level MD files (except README.md)
$rootDocs = Get-ChildItem -Path "." -Filter "*.md" -File | 
    Where-Object { $_.Name -ne "README.md" }

foreach ($doc in $rootDocs) {
    # Categorize based on filename
    $dest = if ($doc.Name -match "(REFACTOR|ARCHITECTURE|DESIGN|implementation)") {
        "market-data-docs\architecture\$($doc.Name)"
    } else {
        "market-data-docs\guides\$($doc.Name)"
    }
    
    Write-Host "Moving: $($doc.Name) -> $dest"
    Move-Item $doc.FullName -Destination $dest -Force
}

# Verify only README.md remains
$remaining = Get-ChildItem -Path "." -Filter "*.md" -File
if ($remaining.Count -eq 1 -and $remaining[0].Name -eq "README.md") {
    Write-Host "✅ Root cleanup complete!"
} else {
    Write-Host "❌ Still have files at root:"
    $remaining | ForEach-Object { Write-Host "  - $($_.Name)" }
}
```

### 3. Documentation Location

- Module docs → `{module}/docs/`
- Project docs → `{project}-docs/`
- Images → Same folder as markdown in `images/` subfolder
- Use relative paths: `![Image](images/diagram.png)`

## Generic PowerShell Scripts

```powershell
# Auto-detect project and find all modules
$project = (Get-Location).Path.Split('\')[-1]
$modules = Get-ChildItem -Directory | Where-Object { 
    $_.Name -match "^$project-\w+$" 
}

# Check for docs/ folders
foreach ($module in $modules) {
    $docsPath = Join-Path $module.FullName "docs"
    if (Test-Path $docsPath) { Write-Host "✅ $($module.Name)" }
    else { Write-Host "❌ $($module.Name) MISSING docs/" }
}

# Find duplicates
$duplicates = Get-ChildItem -Recurse -Filter "*.md" | 
    Where-Object { $_.FullName -notmatch "node_modules|target|\\.git" } |
    Group-Object Name | 
    Where-Object { $_.Count -gt 1 }

# Delete duplicates (keep correct location)
foreach ($dup in $duplicates) {
    $pattern1 = "^$project-docs\\"
    $pattern2 = "^$project-[\w-]+\\docs\\"
    $correct = $dup.Group | Where-Object {
        $rel = $_.FullName.Replace($PWD.Path, "").TrimStart('\')
        $rel -match $pattern1 -or $rel -match $pattern2
    } | Select-Object -First 1
    
    $dup.Group | Where-Object { $_.FullName -ne $correct.FullName } | 
        ForEach-Object { Remove-Item $_.FullName -Force }
}
```

## Summary

**Golden Rules**:
1. Module docs → `{module}/docs/`
2. Project docs → `{project}-docs/`
3. Markdown: DELETE duplicates
4. Images: VERSION duplicates
5. Use regex patterns for automation

**Workflows**: See `.agent/workflows/refactor.md` for Phase 0 duplicate cleanup

---

File organization:
```
.agent/
├── RULES_AND_WORKFLOWS.md  ← This file (consolidated)
└── workflows/
    ├── refactor.md          ← Includes duplicate cleanup
    └── *.md                 ← Other workflows
```
