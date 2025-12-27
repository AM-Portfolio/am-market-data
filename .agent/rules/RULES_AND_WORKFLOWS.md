# Market Data Project - Documentation Standards & Rules

## Core Rule: Module Documentation Structure

**EVERY module MUST follow this structure:**

```
market-data-{module-name}/
├── src/
│   ├── main/
│   └── test/
├── docs/
│   ├── README.md              # Module overview and purpose
│   ├── ARCHITECTURE.md        # Module architecture (if complex)
│   ├── API.md                 # API documentation (if applicable)
│   ├── SETUP.md               # Setup and configuration guide
│   ├── images/                # All diagrams and screenshots
│   │   ├── module-diagram.png
│   │   └── ...
│   └── examples/              # Code examples and tutorials
│       └── ...
├── pom.xml (or package.json, pubspec.yaml)
└── README.md                  # Quick start (redirects to docs/)
```

## Mandatory Rules

### 1. Documentation Location
- ✅ **DO**: Place all module documentation in `{module}/docs/`
- ❌ **DON'T**: Scatter documentation at project root
- ❌ **DON'T**: Mix documentation with source code

### 2. Image Storage
- ✅ **DO**: Store images in `{module}/docs/images/`
- ✅ **DO**: Use relative paths: `![Diagram](images/diagram.png)`
- ❌ **DON'T**: Use absolute paths
- ❌ **DON'T**: Reference images from other modules

### 3. README Files
- **Module README** (`{module}/README.md`):
  - Brief description (2-3 sentences)
  - Link to full documentation: "See [docs/](docs/) for complete documentation"
  - Quick start commands
  
- **Docs README** (`{module}/docs/README.md`):
  - Detailed module purpose
  - Architecture overview
  - Links to other docs
  - Table of contents

### 4. Duplicate Handling ⚠️ CRITICAL

**Critical Rule**: NO duplicate markdown files allowed!

#### For Markdown Files (.md):
- ✅ **DO**: Keep file in ONE correct location only
- ✅ **DO**: Delete all duplicate copies after moving
- ❌ **DON'T**: Keep multiple copies of the same document
- ❌ **DON'T**: Have duplicate filenames anywhere in the project

**Priority for correct location**:
1. `market-data-docs/` → for project-wide documentation
2. `{module}/docs/` → for module-specific documentation  
3. Anywhere else → DELETE (incorrect location)

**Example**:
```
Found: REFACTORING_PLAN.md in 3 locations
  /REFACTORING_PLAN.md                                   ← DELETE
  /market-data-docs/architecture/REFACTORING_PLAN.md    ← KEEP
  /.agent/REFACTORING_PLAN.md                            ← DELETE
```

#### For Images (.png, .jpg, .svg):
- ✅ **DO**: Use versioning approach (keep history)
- ✅ **DO**: Rename with version suffix if duplicates exist
- ✅ **DO**: Keep all versions for historical reference
- ❌ **DON'T**: Delete old image versions

**Versioning Strategy**:
```
Found: architecture_diagram.png in 2 locations

Action:
  /docs/images/architecture_diagram.png          ← Keep as is
  /market-data-docs/architecture/images/architecture_diagram.png 
    → Rename to: architecture_diagram_v1.png     ← Version it
```

**When to Delete vs Version**:
| File Type | Strategy | Reason |
|-----------|----------|--------|
| Markdown (.md) | DELETE duplicates | Single source of truth |
| Images (.png, .jpg) | VERSION duplicates | Historical reference |
| Code (.java, .js) | Module isolation | Build dependency |

### 5. Cleanup During Refactoring

Every refactoring MUST include duplicate cleanup (Phase 0):

1. **Detect**: Find all duplicate files (by name)
2. **Analyze**: Determine correct location
3. **Markdown**: Delete old copies, keep only correct location
4. **Images**: Version duplicates (filename_v1, filename_v2)
5. **Verify**: Ensure no duplicates remain

See `/refactor` workflow for automated scripts.

### 6. Project-Wide Documentation
For **cross-module or project-wide** documentation:
- Create `market-data-docs/` module (no code, only docs)
- Structure:
  ```
  market-data-docs/
  ├── architecture/
  │   ├── REFACTORING_PLAN.md
  │   ├── ARCHITECTURE_COMPARISON.md
  │   └── images/
  ├── guides/
  │   ├── DEVELOPER_GUIDE.md
  │   ├── DEPLOYMENT.md
  │   └── SDK_GENERATION.md
  └── README.md
  ```

## Examples

### Example 1: market-data-api
```
market-data-api/
├── docs/
│   ├── README.md              # "API Module - Interface Definitions"
│   ├── CONTROLLERS.md         # Controller documentation
│   ├── VERSIONING.md          # API versioning strategy
│   └── images/
│       └── api-flow.png
├── src/main/java/...
└── README.md                  # Quick: "See docs/ for details"
```

### Example 2: market-data-provider
```
market-data-provider/
├── docs/
│   ├── README.md              # "Provider Module - Upstox, Zerodha"
│   ├── ADDING_PROVIDER.md     # How to add new provider
│   ├── UPSTOX.md              # Upstox-specific docs
│   ├── ZERODHA.md             # Zerodha-specific docs
│   └── images/
│       ├── provider-pattern.png
│       └── upstox-flow.png
├── src/main/java/...
└── README.md
```

### Example 3: market-data-sdk
```
market-data-sdk/
├── docs/
│   ├── README.md              # "SDK Generation Module"
│   ├── GENERATION_PROCESS.md  # How SDK generation works
│   ├── PYTHON_SDK.md          # Python SDK usage
│   ├── DART_SDK.md            # Dart SDK usage
│   ├── JAVA_SDK.md            # Java SDK usage
│   └── images/
│       └── sdk-generation-flow.png
├── market-data-sdk-python/
├── market-data-sdk-flutter/
├── market-data-sdk-java/
├── generate_sdks.ps1
└── README.md
```

## Enforcement

When creating a new module, use this template:

```bash
# Create module structure
mkdir market-data-{name}
mkdir market-data-{name}/docs
mkdir market-data-{name}/docs/images
mkdir market-data-{name}/docs/examples

# Create required files
touch market-data-{name}/README.md
touch market-data-{name}/docs/README.md
touch market-data-{name}/docs/ARCHITECTURE.md
```

## Benefits

✅ **Modularity**: Each module is self-contained  
✅ **Discoverability**: Documentation is where developers expect it  
✅ **Maintainability**: Docs stay in sync with code  
✅ **Version Control**: Docs version with the module  
✅ **Portability**: Modules can be extracted easily  
✅ **No Duplicates**: Single source of truth  
✅ **Clean History**: Images are versioned, docs are not duplicated  

---

## Summary

**Golden Rule**: 
> "If it's about THIS module, it goes in THIS module's docs/ folder."

**Exception**: 
> "If it's about the ENTIRE project, it goes in market-data-docs/ module."

**Duplicate Rule**:
> "Markdown: DELETE duplicates. Images: VERSION duplicates."

---

*This file consolidates all project rules. Individual `.agent/workflows/*.md` files are preserved for tooling compatibility.*

## Related Workflows

- `/refactor` - Refactoring workflow with duplicate cleanup (Phase 0)
- See `.agent/workflows/` for all available workflows
