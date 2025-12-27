---
description: Refactor code with clean separation and documentation organization
---

# Refactoring Workflow

When the user says "refactor", follow this systematic workflow to ensure clean module separation and proper documentation organization.

## Phase 0: Duplicate Detection & Cleanup

**Rule**: 
- ✅ **Images**: Use versioning approach (keep history)
- ❌ **Markdown files**: Delete old duplicates after moving

### 0.1 Find Duplicate Markdown Files
```powershell
# Find all .md files in the project
$allMdFiles = Get-ChildItem -Path "." -Recurse -Filter "*.md" | Where-Object {
    $_.FullName -notmatch "node_modules" -and 
    $_.FullName -notmatch "target" -and
    $_.FullName -notmatch "\\.git"
}

# Group by filename to find duplicates
$duplicates = $allMdFiles | Group-Object -Property Name | Where-Object { $_.Count -gt 1 }

Write-Host "=== Duplicate Markdown Files Found ==="
foreach ($dup in $duplicates) {
    Write-Host "`n📄 $($dup.Name) (found $($dup.Count) times):"
    foreach ($file in $dup.Group) {
        Write-Host "   📍 $($file.FullName)"
    }
}
```

### 0.2 Determine Correct Location & Delete Duplicates
**Strategy**: Keep the file in the correct location, delete all others

```powershell
foreach ($dup in $duplicates) {
    $files = $dup.Group
    $correctLocation = $null
    $filesToDelete = @()
    
    # Determine correct location based on content
    foreach ($file in $files) {
        $relativePath = $file.FullName.Replace($PWD.Path, "").TrimStart('\')
        
        # Priority order (highest to lowest):
        # 1. market-data-docs/ (for project-wide docs)
        # 2. {module}/docs/ (for module-specific docs)
        # 3. Anywhere else (to be deleted)
        
        if ($relativePath -match "^market-data-docs\\") {
            $correctLocation = $file
        }
        elseif ($relativePath -match "^market-data-[^\\]+\\docs\\") {
            if (-not $correctLocation) {
                $correctLocation = $file
            }
        }
    }
    
    # If no correct location found, keep the newest file
    if (-not $correctLocation) {
        $correctLocation = $files | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    }
    
    # Mark others for deletion
    foreach ($file in $files) {
        if ($file.FullName -ne $correctLocation.FullName) {
            $filesToDelete += $file
        }
    }
    
    # Show plan
    Write-Host "`n✅ KEEP: $($correctLocation.FullName)"
    foreach ($fileToDelete in $filesToDelete) {
        Write-Host "❌ DELETE: $($fileToDelete.FullName)"
    }
    
    # Execute deletion
    foreach ($fileToDelete in $filesToDelete) {
        Write-Host "Deleting duplicate: $($fileToDelete.Name) from $($fileToDelete.DirectoryName)"
        Remove-Item $fileToDelete.FullName -Force
    }
}
```

**Checklist**:
- [ ] Run duplicate detection
- [ ] Review which files will be kept vs deleted
- [ ] Confirm correct location is identified
- [ ] Delete all duplicate MD files
- [ ] Verify only one copy remains in correct location

### 0.3 Find Duplicate Images (Versioning Approach)
```powershell
# Find all images
$allImages = Get-ChildItem -Path "." -Recurse -Include "*.png","*.jpg","*.jpeg","*.svg" | Where-Object {
    $_.FullName -notmatch "node_modules" -and 
    $_.FullName -notmatch "target" -and
    $_.FullName -notmatch "\\.git"
}

# Group by filename
$imageDuplicates = $allImages | Group-Object -Property Name | Where-Object { $_.Count -gt 1 }

Write-Host "`n=== Duplicate Images Found ==="
foreach ($dup in $imageDuplicates) {
    Write-Host "`n🖼️ $($dup.Name) (found $($dup.Count) times):"
    foreach ($file in $dup.Group) {
        Write-Host "   📍 $($file.FullName)"
    }
}
```

### 0.4 Version Duplicate Images
**Strategy**: Keep all versions but rename with version suffix

```powershell
foreach ($dup in $imageDuplicates) {
    $files = $dup.Group | Sort-Object DirectoryName
    $basename = [System.IO.Path]::GetFileNameWithoutExtension($dup.Name)
    $extension = [System.IO.Path]::GetExtension($dup.Name)
    
    Write-Host "`nVersioning: $($dup.Name)"
    
    # First file keeps original name
    Write-Host "   ✅ KEEP: $($files[0].FullName) (as $($dup.Name))"
    
    # Other files get versioned
    for ($i = 1; $i -lt $files.Count; $i++) {
        $newName = "${basename}_v${i}${extension}"
        $newPath = Join-Path $files[$i].DirectoryName $newName
        
        Write-Host "   🔄 RENAME: $($files[$i].FullName)"
        Write-Host "      TO: $newName"
        
        Rename-Item -Path $files[$i].FullName -NewName $newName -Force
    }
}
```

**Checklist**:
- [ ] Run duplicate image detection
- [ ] Review image usage across documents
- [ ] Apply versioning (image_v1.png, image_v2.png)
- [ ] Update markdown files to reference correct version
- [ ] Keep all versions for history

### 0.5 Clean Up Root-Level Documents
**Critical Rule**: ONLY `README.md` allowed at project root

```powershell
# Find ALL .md files at project root (except README.md)
$rootMdFiles = Get-ChildItem -Path "." -Filter "*.md" -File | 
    Where-Object { $_.Name -ne "README.md" }

Write-Host "`n=== Root-Level Documents Found ==="
Write-Host "Count: $($rootMdFiles.Count) (should be 0)"

if ($rootMdFiles.Count -gt 0) {
    foreach ($file in $rootMdFiles) {
        Write-Host "`n📄 $($file.Name)"
        
        # Categorize based on filename/content
        $dest = if ($file.Name -match "(REFACTOR|ARCHITECTURE|DESIGN|implementation|COMPLETE)") {
            "market-data-docs\architecture\$($file.Name)"
        } else {
            "market-data-docs\guides\$($file.Name)"
        }
        
        Write-Host "   Moving to: $dest"
        
        # Ensure destination directory exists
        $destDir = Split-Path $dest -Parent
        if (-not (Test-Path $destDir)) {
            New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        }
        
        # Move the file
        if (Test-Path $dest) {
            Write-Host "   ⚠️ Destination exists, removing old version"
            Remove-Item $dest -Force
        }
        
        Move-Item $file.FullName -Destination $dest -Force
        Write-Host "   ✅ Moved successfully"
    }
}

# Verify only README.md remains
$remaining = Get-ChildItem -Path "." -Filter "*.md" -File
if ($remaining.Count -eq 1 -and $remaining[0].Name -eq "README.md") {
    Write-Host "`n✅ Root cleanup complete! Only README.md remains."
} else {
    Write-Host "`n❌ Root still has files:"
    $remaining | ForEach-Object { Write-Host "  - $($_.Name)" }
}
```

**Checklist**:
- [ ] Find all .md files at root (except README.md)
- [ ] Categorize as architecture/ or guides/
- [ ] Move to market-data-docs/{category}/
- [ ] Delete root copies after moving
- [ ] Verify ONLY README.md remains at root



## Phase 1: Analyze Current Structure

### 1.1 Identify Module Violations
```powershell
# Check for implementation code in API module
Get-ChildItem -Path "market-data-api\src\main\java" -Recurse -Filter "*Impl.java"
Get-ChildItem -Path "market-data-api\src\main\java" -Recurse -Filter "*Service.java" | Where-Object { $_.FullName -notmatch "interface" }

# Check for provider code in service module
Get-ChildItem -Path "market-data-service\src\main\java" -Recurse -Filter "*Upstox*.java"
Get-ChildItem -Path "market-data-service\src\main\java" -Recurse -Filter "*Zerodha*.java"
```

### 1.2 Find Misplaced Documentation
```powershell
# Find all .md files at project root (except allowed ones)
Get-ChildItem -Path "." -Filter "*.md" -Depth 0 | Where-Object { 
    $_.Name -notin @("README.md", "DOCUMENTATION_STRUCTURE.md") 
}

# Find images outside docs folders
Get-ChildItem -Path "." -Recurse -Include "*.png","*.jpg","*.svg" | Where-Object {
    $_.FullName -notmatch "docs\\images" -and $_.FullName -notmatch "market-data-docs"
}
```

## Phase 2: Clean Module Separation

### 2.1 Move Service Implementations OUT of API
**Rule**: API module should ONLY have controllers and interfaces

```powershell
# Identify files to move
$apiServicePath = "market-data-api\src\main\java\com\am\marketdata\api\service"
$targetPath = "market-data-service\src\main\java\com\am\marketdata\service\impl"

# Move implementation files
Get-ChildItem -Path $apiServicePath -Recurse -Filter "*Impl.java" | ForEach-Object {
    $dest = $_.FullName.Replace($apiServicePath, $targetPath)
    Write-Host "Moving $($_.Name) to service module"
    # Move-Item $_.FullName -Destination $dest
}
```

**Checklist**:
- [ ] Identify all `*Impl.java` and `*Service.java` (non-interfaces) in API
- [ ] Move to `market-data-service/src/.../impl/`
- [ ] Keep ONLY interfaces in API module
- [ ] Update imports in controllers
- [ ] Compile and verify: `mvn compile -pl market-data-api`

### 2.2 Extract Provider Code
**Rule**: Provider-specific code goes in market-data-provider module

```powershell
# Create provider module if not exists
if (-not (Test-Path "market-data-provider")) {
    Write-Host "Creating market-data-provider module..."
    # Follow Phase 1 of REFACTORING_PLAN.md
}

# Move Upstox code
$upstoxFiles = Get-ChildItem -Path "market-data-service" -Recurse -Filter "*Upstox*.java"
foreach ($file in $upstoxFiles) {
    Write-Host "Upstox file identified: $($file.FullName)"
    # Move to market-data-provider/src/.../provider/upstox/
}
```

**Checklist**:
- [ ] Create `market-data-provider` module structure
- [ ] Create `MarketDataProvider` interface
- [ ] Move all Upstox code to `provider/upstox/`
- [ ] Move all Zerodha code to `provider/zerodha/`
- [ ] Create mapper classes (e.g., `UpstoxToCommonMapper`)
- [ ] Update service layer to use provider interface
- [ ] Compile and verify: `mvn compile -pl market-data-provider`

### 2.3 Verify Module Dependencies
**Rule**: Enforce clean dependency flow

```bash
# API should only depend on common
# turbo
mvn dependency:tree -pl market-data-api | grep -v "market-data-common"

# Service should not have direct provider SDK dependencies
# turbo
mvn dependency:tree -pl market-data-service | grep -E "(upstox|zerodha|kite)"

# Check circular dependencies
# turbo
mvn validate
```

**Expected Dependencies**:
```
market-data-common     → ZERO
market-data-api        → common
market-data-provider   → common
market-data-service    → api, common, provider (interface)
```

## Phase 3: Organize Documentation

### 3.1 Move Root-Level Documentation
**Rule**: ONLY `README.md` allowed at project root

```powershell
# Find ALL .md files at root (except README.md only)
$rootDocs = Get-ChildItem -Path "." -Filter "*.md" -File | 
    Where-Object { $_.Name -ne "README.md" }

foreach ($doc in $rootDocs) {
    # Categorize based on filename
    $category = if ($doc.Name -match "(ARCHITECTURE|REFACTOR|DESIGN|implementation|COMPLETE)") {
        "architecture"
    } else {
        "guides"
    }
    
    $destination = "market-data-docs\$category\$($doc.Name)"
    Write-Host "Moving $($doc.Name) to market-data-docs/$category/"
    
    # Ensure destination directory exists
    $destDir = "market-data-docs\$category"
    if (-not (Test-Path $destDir)) {
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    }
    
    # Move the file (delete if exists at destination)
    if (Test-Path $destination) {
        Remove-Item $destination -Force
    }
    Move-Item $doc.FullName -Destination $destination -Force
    
    # Update image paths in document
    $content = Get-Content $destination -Raw
    $content = $content -replace '\!\[([^\]]*)\]\(([^\/][^\)]+)\)', '![${1}](images/${2})'
    Set-Content -Path $destination -Value $content
}

# Verify only README.md remains
Write-Host "`nVerifying root cleanup..."
$remaining = Get-ChildItem -Path "." -Filter "*.md" -File
if ($remaining.Count -eq 1 -and $remaining[0].Name -eq "README.md") {
    Write-Host "✅ Success! Only README.md at root"
} else {
    Write-Host "❌ Still have files at root:"
    $remaining | ForEach-Object { Write-Host "  - $($_.Name)" }
}
```

**Checklist**:
- [ ] Identify ALL root-level .md files (except README.md)
- [ ] Categorize as `architecture/` or `guides/` based on filename
- [ ] Move to `market-data-docs/{category}/`
- [ ] Update image paths to use relative paths
- [ ] Verify ONLY README.md remains at root

### 3.2 Move Images to Proper Locations
**Rule**: Images go in docs/images/ folder alongside their documents

```powershell
# Find orphaned images
$orphanedImages = Get-ChildItem -Path "." -Recurse -Include "*.png","*.jpg","*.svg" | Where-Object {
    $_.FullName -notmatch "docs\\images" -and 
    $_.FullName -notmatch "market-data-docs" -and
    $_.FullName -notmatch "node_modules" -and
    $_.FullName -notmatch "target"
}

foreach ($img in $orphanedImages) {
    # Determine which doc uses this image
    $docFiles = Get-ChildItem -Path "market-data-docs" -Recurse -Filter "*.md"
    
    foreach ($doc in $docFiles) {
        $content = Get-Content $doc.FullName -Raw
        if ($content -match $img.BaseName) {
            $targetPath = Join-Path (Split-Path $doc.FullName) "images\$($img.Name)"
            Write-Host "Moving $($img.Name) to $targetPath"
            # Ensure images directory exists
            $imagesDir = Join-Path (Split-Path $doc.FullName) "images"
            if (-not (Test-Path $imagesDir)) {
                New-Item -ItemType Directory -Path $imagesDir | Out-Null
            }
            Copy-Item $img.FullName -Destination $targetPath
            break
        }
    }
}
```

**Checklist**:
- [ ] Find all images outside docs folders
- [ ] Identify which document references each image
- [ ] Move image to `{document-directory}/images/`
- [ ] Update document to use relative path: `![](images/filename.png)`
- [ ] Remove original orphaned images

### 3.3 Create Module Documentation Structure
**Rule**: Each module has its own docs/ folder

```powershell
$modules = @(
    "market-data-api",
    "market-data-service", 
    "market-data-provider",
    "market-data-sdk",
    "market-data-parser"
)

foreach ($module in $modules) {
    if (Test-Path $module) {
        $docsPath = Join-Path $module "docs"
        $imagesPath = Join-Path $docsPath "images"
        
        # Create structure
        if (-not (Test-Path $docsPath)) {
            New-Item -ItemType Directory -Path $docsPath | Out-Null
            New-Item -ItemType Directory -Path $imagesPath | Out-Null
            
            # Create README
            $readme = @"
# $module Documentation

## Overview
[Brief description of this module]

## Contents
- [Architecture](ARCHITECTURE.md)
- [API Documentation](API.md) (if applicable)
- [Setup Guide](SETUP.md)

## Quick Start
[Quick start commands]

See parent [README.md](../README.md) for module overview.
"@
            Set-Content -Path (Join-Path $docsPath "README.md") -Value $readme
            
            Write-Host "Created docs structure for $module"
        }
    }
}
```

**Checklist**:
- [ ] Create `docs/` folder in each module
- [ ] Create `docs/images/` subfolder
- [ ] Create `docs/README.md` with template
- [ ] Update module's root README.md to redirect to docs/
- [ ] Move module-specific documentation to its docs/ folder

## Phase 4: Verify Clean Separation

### 4.1 Compile Each Module Independently
```bash
# Each module should compile independently
# turbo
mvn clean compile -pl market-data-common

# turbo
mvn clean compile -pl market-data-api

# turbo
mvn clean compile -pl market-data-provider

# turbo
mvn clean compile -pl market-data-service
```

**Checklist**:
- [ ] Common compiles with zero dependencies
- [ ] API compiles with only common dependency
- [ ] Provider compiles with only common dependency
- [ ] Service compiles with api, common, provider dependencies
- [ ] No compilation errors in any module

### 4.2 Run Tests
```bash
# turbo
mvn test -pl market-data-common
# turbo
mvn test -pl market-data-api
# turbo
mvn test -pl market-data-provider
# turbo
mvn test -pl market-data-service
```

### 4.3 Verify Documentation Organization
```powershell
# Check: No .md files at root (except allowed)
$rootMds = Get-ChildItem -Path "." -Filter "*.md" -Depth 0
Write-Host "Root MD files: $($rootMds.Count) (should be 2: README.md, DOCUMENTATION_STRUCTURE.md)"

# Check: All modules have docs/ folders
$modules = Get-ChildItem -Path "." -Directory -Filter "market-data-*"
foreach ($module in $modules) {
    $docsPath = Join-Path $module.FullName "docs"
    if (Test-Path $docsPath) {
        Write-Host "✅ $($module.Name) has docs/"
    } else {
        Write-Host "❌ $($module.Name) MISSING docs/"
    }
}

# Check: All images are in docs/images/
$orphanedImages = Get-ChildItem -Path "." -Recurse -Include "*.png","*.jpg" | Where-Object {
    $_.FullName -notmatch "docs\\images" -and $_.FullName -notmatch "node_modules" -and $_.FullName -notmatch "target"
}
if ($orphanedImages.Count -eq 0) {
    Write-Host "✅ All images properly organized"
} else {
    Write-Host "❌ Found $($orphanedImages.Count) orphaned images"
}
```

## Phase 5: Update Configuration

### 5.1 Update .gitignore
Ensure proper files are tracked:
```
# Documentation structure (KEEP)
!market-data-docs/
!**/docs/
!**/docs/images/

# Generated content (IGNORE)
**/target/
**/build/
**/.idea/
```

### 5.2 Update Root README
```markdown
# Market Data Service

## Documentation
See [market-data-docs/](market-data-docs/) for complete project documentation.

## Modules
- [market-data-api](market-data-api/) - API controllers and interfaces
- [market-data-service](market-data-service/) - Business logic implementation
- [market-data-provider](market-data-provider/) - Provider integrations (Upstox, Zerodha)
- [market-data-sdk](market-data-sdk/) - Generated SDKs

Each module contains its own documentation in `{module}/docs/`.
```

## Success Criteria

✅ **Module Separation**:
- [ ] API module has NO implementation code
- [ ] Service module has NO provider-specific code
- [ ] Provider module exists and is isolated
- [ ] All modules compile independently

✅ **Documentation Organization**:
- [ ] All project-wide docs in `market-data-docs/`
- [ ] All module-specific docs in `{module}/docs/`
- [ ] All images in `docs/images/` folders
- [ ] All image references use relative paths
- [ ] No orphaned .md files at root (except allowed)

✅ **Build & Test**:
- [ ] All modules compile without errors
- [ ] All tests pass
- [ ] No dependency violations
- [ ] OpenAPI spec generates successfully

## Rollback Plan

If refactoring causes issues:
1. Revert changes: `git reset --hard HEAD`
2. Review specific module causing issues
3. Refactor incrementally (one module at a time)
4. Test after each module refactoring

## Post-Refactoring

After successful refactoring:
1. Commit changes with descriptive message
2. Update DOCUMENTATION_STRUCTURE.md if needed
3. Regenerate SDKs to verify: `./market-data-sdk/generate_sdks.ps1`
4. Document any lessons learned
