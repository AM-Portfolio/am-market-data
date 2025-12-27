# Image Versioning Policy

## Overview
All generated images MUST be versioned to maintain a complete history of architectural changes and iterations.

## Rules

### ✅ DO:
- Keep ALL versions of images
- Use versioning format: `image_name_v1.png`, `image_name_v2.png`, etc.
- Update markdown files to reference latest version
- Preserve previous versions for historical reference

### ❌ DON'T:
- Delete or overwrite existing images
- Use non-versioned filenames (except first time)
- Keep "backup" or "old" suffixed files

## Workflow

### When Generating a New Image:

1. **Check for existing versions**:
   ```powershell
   Get-ChildItem images/ -Filter "architecture_diagram_v*.png"
   ```

2. **Determine next version number**:
   - If no versions exist: Use `v1`
   - If versions exist: Find max version number and increment

3. **Save with version**:
   ```powershell
   Copy-Item $newImage -Destination "images/architecture_diagram_v3.png"
   ```

4. **Update markdown reference**:
   ```markdown
   ![Architecture](images/architecture_diagram_v3.png)
   ```

5. **Keep all previous versions** - DO NOT DELETE

## Current Architecture Images

### complete_architecture_flow
- `complete_architecture_flow_v1.png` - Initial architecture diagram showing module dependencies

### final_architecture_diagram
- `final_architecture_diagram_v1.png` - Polished architecture with all layers, Kafka, Redis, detailed tech stack

## Benefits

✅ **History Tracking**: See evolution of architecture
✅ **Rollback**: Can reference previous versions
✅ **Comparison**: Compare different iterations
✅ **Documentation**: Shows decision progression
✅ **No Data Loss**: All work preserved

## Example Structure

```
market-data-docs/architecture/images/
├── architecture_diagram_v1.png       ← Initial design
├── architecture_diagram_v2.png       ← Added scheduler
├── architecture_diagram_v3.png       ← Added Kafka layer (LATEST)
├── complete_flow_v1.png              ← Original flow
├── complete_flow_v2.png              ← Enhanced flow (LATEST)
└── README.md                         ← This file
```

## Automation Script

Use this PowerShell function for automatic versioning:

```powershell
function Save-VersionedImage {
    param(
        [string]$SourceImage,
        [string]$BaseName,
        [string]$DestinationDir
    )
    
    # Find existing versions
    $versions = Get-ChildItem "$DestinationDir\${BaseName}_v*.png" -ErrorAction SilentlyContinue
    
    if ($versions) {
        $maxVersion = ($versions | ForEach-Object {
            if ($_.Name -match "_v(\d+)\.png$") { [int]$matches[1] }
        } | Measure-Object -Maximum).Maximum
        $newVersion = $maxVersion + 1
    } else {
        $newVersion = 1
    }
    
    $newName = "${BaseName}_v${newVersion}.png"
    Copy-Item $SourceImage -Destination "$DestinationDir\$newName"
    
    Write-Host "✅ Saved as $newName (version $newVersion)"
    Write-Host "✅ Previous versions preserved"
    
    return $newName
}

# Usage:
# Save-VersionedImage -SourceImage "temp.png" -BaseName "architecture_diagram" -DestinationDir "images"
```

## Markdown Update

Always update markdown files to reference the LATEST version:

```powershell
$mdFile = "ARCHITECTURE.md"
$latestImage = "architecture_diagram_v3.png"

$content = Get-Content $mdFile -Raw
$content = $content -replace "!\[Architecture\]\(images/architecture_diagram_v\d+\.png\)", "![Architecture](images/$latestImage)"
Set-Content $mdFile -Value $content
```

---

**Last Updated**: 2025-12-27  
**Policy Version**: 1.0  
**Contact**: Architecture Team
