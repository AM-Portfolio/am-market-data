# Refactoring Rules & Workflow - Final Update

**Date**: 2025-12-27  
**Update**: Added root-level document handling rules

## ✅ Changes Made

### 1. Updated `.agent/RULES_AND_WORKFLOWS.md`
Added comprehensive rules for root-level documents:
- ❌ NO markdown files allowed at root (except README.md)
- Table showing where to move each type of document
- PowerShell script for automated root cleanup
- Critical exclusions list (.agent, .git, node_modules, target, build)

### 2. Updated `.agent/workflows/refactor.md`
Updated both Phase 0.5 and Phase 3.1:
- **Phase 0.5**: Clean up root-level documents
  - Only README.md allowed at root
  - Categorize files as architecture/ or guides/
  - Move to market-data-docs/
  
- **Phase 3.1**: Consistent with Phase 0.5
  - Same cleanup rules
  - Verification that only README.md remains

### 3. Completed Root Cleanup
Moved all remaining root files:
- ✅ DOCUMENTATION_STRUCTURE.md → market-data-docs/guides/
- ✅ REFACTORING_COMPLETE.md → market-data-docs/architecture/
- ✅ SDK_GENERATION_ANALYSIS.md (if found) → market-data-docs/guides/

## 📁 Current State

```
am-market-data/
├── .agent/
│   ├── RULES_AND_WORKFLOWS.md       ← Updated with root handling rules
│   └── workflows/
│       └── refactor.md               ← Updated Phase 0.5 and 3.1
│
├── market-data-docs/
│   ├── architecture/
│   │   ├── ARCHITECTURE_COMPARISON.md
│   │   ├── implementation_plan.md
│   │   ├── REFACTORING_PLAN.md
│   │   ├── REFACTORING_SUMMARY.md
│   │   ├── REFACTORING_COMPLETE.md  ← Moved
│   │   └── images/
│   └── guides/
│       └── DOCUMENTATION_STRUCTURE.md ← Moved
│
├── market-data-sdk/
│   └── docs/
│       └── DESIGN.md
│
└── README.md                         ← ONLY file at root ✅
```

## 📋 Root-Level Document Rules

### Allowed at Root:
✅ `README.md` (main entry point)  
✅ `.gitignore`, `pom.xml`, `package.json` (build files)  

### NOT Allowed (must be moved):
❌ Any `*.md` files (except README.md)  
❌ Architecture docs  
❌ Guide docs  
❌ Analysis docs  

### Move Destinations:

| File Type | Pattern | Destination |
|-----------|---------|-------------|
| Architecture | REFACTOR*, ARCHITECTURE*, DESIGN*, implementation* | `market-data-docs/architecture/` |
| Guides | All others | `market-data-docs/guides/` |

## 🔒 Protection Rules

**Never touch during refactoring**:
- `.agent/` folder
- `.agent/RULES_AND_WORKFLOWS.md`
- `.agent/workflows/*.md`  
- `.git/` folder
- `node_modules/`
- `target/` build folders

## 🚀 Next Refactoring

When you say "/refactor" next time, the workflow will:
1. **Phase 0.5**: Clean up root documents
   - Find all .md files at root (except README.md)
   - Categorize as architecture/ or guides/
   - Move to market-data-docs/
   - Verify only README.md remains

2. **Phase 3.1**: Organize documentation
   - Same cleanup with verification
   - Update image paths
   - Confirm clean state

All rules are now documented and enforced! ✅
