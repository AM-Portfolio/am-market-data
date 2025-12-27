---
trigger: always_on
glob: "**/*.java"
description: "Global coding standards for Agent & LLM to ensure maintainability and modularity"
---
# Code Quality & Maintainability
- **Small, Focused Files**: Avoid monolithic files. Break down large files into smaller, logical units based on responsibility.
- **Modular Approach**: Ensure strict separation of concerns. Features should be loosely coupled and highly cohesive.
- **Clean Code**: Prioritize readability and simplicity. Code should be self-documenting.

# Methodology & Structure
- **No Big Methods**: Methods should be short and do one thing well. Extract responsibilities into helpers or services.
- **DRY (Don't Repeat Yourself)**: Eliminate duplication. Use utility classes and shared services.
- **Design Patterns**: Apply patterns (Singleton, Factory, Observer) where appropriate.

# Technical Specifics
- **Imports**: Use specific imports. Organize logically (Std lib -> 3rd party -> Internal).
- **Type Safety**: Use strong typing. Avoid `Object` or `any` unless absolutely necessary.
- **Documentation**: Javadoc/Comments for complex logic and public interfaces.
- **Refactoring**: Apply the "Boy Scout Rule"  leave code better than you found it.
