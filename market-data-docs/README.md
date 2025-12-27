# Market Data Documentation

This module contains **project-wide** documentation that spans multiple modules.

## Purpose

Centralized documentation for:
- Architecture decisions and refactoring plans
- Cross-module workflows
- Developer guides
- Deployment instructions

## Structure

```
market-data-docs/
├── architecture/          # Architecture documentation
│   ├── REFACTORING_PLAN.md
│   ├── ARCHITECTURE_COMPARISON.md
│   ├── REFACTORING_SUMMARY.md
│   └── images/
│       └── complete_architecture_flow.png
├── guides/                # Developer and operational guides
│   ├── DEVELOPER_GUIDE.md
│   ├── SDK_GENERATION_GUIDE.md
│   └── DEPLOYMENT_GUIDE.md
└── README.md             # This file
```

## Documentation Index

### Architecture

- **[REFACTORING_PLAN.md](architecture/REFACTORING_PLAN.md)** - Complete refactoring plan with module isolation
- **[ARCHITECTURE_COMPARISON.md](architecture/ARCHITECTURE_COMPARISON.md)** - Current vs. proposed architecture
- **[REFACTORING_SUMMARY.md](architecture/REFACTORING_SUMMARY.md)** - Quick reference guide

### Guides

- **[SDK_GENERATION_GUIDE.md](guides/SDK_GENERATION_GUIDE.md)** - How to generate SDKs (coming soon)
- **[DEVELOPER_GUIDE.md](guides/DEVELOPER_GUIDE.md)** - Developer onboarding (coming soon)

## Module-Specific Documentation

For module-specific documentation, see the `docs/` folder in each module:

- **market-data-api** → `market-data-api/docs/`
- **market-data-service** → `market-data-service/docs/`
- **market-data-provider** → `market-data-provider/docs/`
- **market-data-sdk** → `market-data-sdk/docs/`
- **market-data-parser** → `market-data-parser/docs/`

## Rule

> **"If it's about THIS module, it goes in THAT module's docs/ folder."**
> 
> **"If it's about the ENTIRE project, it goes here in market-data-docs/."**

See `.agent/RULES_AND_WORKFLOWS.md` for complete documentation standards.
