# Documentation Structure - Summary

## ✅ Final Organization

```
{project-name}/                          # e.g., am-market-data
├── .agent/
│   ├── RULES_AND_WORKFLOWS.md        ← CONSOLIDATED rules with REGEX patterns
│   └── workflows/                     ← Individual workflow files
│       ├── refactor.md
│       ├── build_and_test.md
│       └── ...
│
├── {project}-docs/                    ← PROJECT-WIDE documentation  
│   ├── README.md
│   ├── architecture/
│   │   ├── REFACTORING_PLAN.md
│   │   ├── ARCHITECTURE_COMPARISON.md
│   │   ├── REFACTORING_SUMMARY.md
│   │   └── images/
│   │       └── complete_architecture_flow.png
│   └── guides/
│       └── (future guides)
│
├── {project}-{module}-api/            ← Pattern: ^{project}-\w+-api$
│   ├── docs/
│   │   ├── README.md
│   │   ├── images/
│   │   └── ...
│   ├── src/
│   └── README.md (redirects to docs/)
│
├── {project}-{module}-service/        ← Pattern: ^{project}-\w+-service$
│   ├── docs/
│   │   ├── README.md
│   │   ├── images/
│   │   └── ...
│   ├── src/
│   └── README.md
│
├── {project}-{module}-provider/       ← Pattern: ^{project}-\w+-(provider|integration)$
│   ├── docs/
│   │   ├── README.md
│   │   ├── ADDING_PROVIDER.md
│   │   ├── {PROVIDER_1}.md
│   │   └── images/
│   └── src/
│
├── {project}-{module}-sdk/            ← Pattern: ^{project}-\w+-sdk$
│   ├── docs/
│   │   ├── README.md
│   │   ├── PYTHON_SDK.md
│   │   ├── DART_SDK.md
│   │   ├── JAVA_SDK.md
│   │   └── images/
│   ├── {project}-sdk-python/
│   ├── {project}-sdk-flutter/
│   └── {project}-sdk-java/
│
├── .gitignore (configured to track .agent/)
└── README.md (project root - links to {project}-docs/)
```

## Key Principles

### 1. Generic Regex Patterns
Use patterns instead of hardcoded names for universal applicability:
```regex
^{project}-docs$                        # Project-wide docs
^{project}-\w+-api$                     # API modules
^{project}-\w+-service$                 # Service modules  
^{project}-\w+-(provider|integration)$  # Provider modules
^{project}-\w+-sdk$                     # SDK modules
```

### 2. Consolidated Rules
- **All rules** in ONE file: `.agent/RULES_AND_WORKFLOWS.md`
- **Workflows** in `.agent/workflows/*.md` (for tooling)
- **No scattered** rule files

```
am-market-data/
├── .agent/
│   ├── RULES_AND_WORKFLOWS.md    ← CONSOLIDATED rules (this file)
│   └── workflows/                 ← Individual workflow files (for tooling)
│
├── market-data-docs/              ← PROJECT-WIDE documentation
│   ├── README.md
│   ├── architecture/
│   │   ├── REFACTORING_PLAN.md
│   │   ├── ARCHITECTURE_COMPARISON.md
│   │   ├── REFACTORING_SUMMARY.md
│   │   └── images/
│   │       └── complete_architecture_flow.png
│   └── guides/
│       └── (future guides)
│
├── market-data-api/
│   ├── docs/                      ← API-specific docs
│   │   ├── README.md
│   │   ├── images/
│   │   └── ...
│   ├── src/
│   └── README.md (redirects to docs/)
│
├── market-data-service/
│   ├── docs/                      ← Service-specific docs
│   │   ├── README.md
│   │   ├── images/
│   │   └── ...
│   ├── src/
│   └── README.md
│
├── market-data-provider/          ← (to be created)
│   ├── docs/
│   │   ├── README.md
│   │   ├── UPSTOX.md
│   │   ├── ZERODHA.md
│   │   └── images/
│   └── src/
│
├── market-data-sdk/
│   ├── docs/                      ← SDK-specific docs
│   │   ├── README.md
│   │   ├── PYTHON_SDK.md
│   │   ├── DART_SDK.md
│   │   ├── JAVA_SDK.md
│   │   └── images/
│   ├── market-data-sdk-python/
│   ├── market-data-sdk-flutter/
│   └── market-data-sdk-java/
│
└── README.md (project root - links to market-data-docs/)
```

## Key Principles

### 1. Consolidated .agent Rules
- ✅ All rules in ONE file: `.agent/RULES_AND_WORKFLOWS.md`
- ✅ Workflow files in `.agent/workflows/` preserved for tooling
- ❌ No scattered rule files

### 2. Module Documentation
- ✅ Each module has `docs/` folder
- ✅ Module-specific docs stay with the module
- ✅ Images in `{module}/docs/images/`
- ✅ Relative paths: `![Image](images/diagram.png)`

### 3. Project-Wide Documentation
- ✅ Lives in `market-data-docs/` module
- ✅ Organized by category (architecture/, guides/)
- ✅ Has its own images folder

## Golden Rules

**Rule 1**: "If it's about THIS module, it goes in THIS module's `docs/` folder."

**Rule 2**: "If it's about the ENTIRE project, it goes in `market-data-docs/`."

**Rule 3**: "All images for a document go in the same directory's `images/` folder."

**Rule 4**: "Always use relative paths for images."

**Rule 5**: "Module README.md should be brief and redirect to `docs/` for details."

## Migration Completed

✅ Created `market-data-docs/` module  
✅ Moved REFACTORING_PLAN.md → `market-data-docs/architecture/`  
✅ Moved ARCHITECTURE_COMPARISON.md → `market-data-docs/architecture/`  
✅ Moved REFACTORING_SUMMARY.md → `market-data-docs/architecture/`  
✅ Moved architecture diagram → `market-data-docs/architecture/images/`  
✅ Updated all image references to use relative paths  
✅ Consolidated .agent rules → `.agent/RULES_AND_WORKFLOWS.md`  

## Next Steps

For each module, create the `docs/` structure:

```bash
# Example for market-data-api
cd market-data-api
mkdir docs
mkdir docs/images
echo "# Market Data API Documentation" > docs/README.md
echo "See [docs/](docs/) for complete documentation." > README.md
```

## Reference

See `.agent/RULES_AND_WORKFLOWS.md` for:
- Complete documentation standards
- Module structure templates
- Enforcement guidelines
- Examples for each module type
