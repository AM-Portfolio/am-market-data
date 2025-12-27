---
trigger: always_on
glob: '**/*'
description: 'Automation rules for agent tool usage'
---
# Agent Workflow Automation Rules

To ensure a smooth and efficient workflow, follow these rules regarding tool execution and user approval:

## 1. Automatic Approval (SafeToAutoRun: true)
The following categories of commands MUST be executed with 'SafeToAutoRun: true' to avoid unnecessary prompting:

- **Build Operations**: Commands related to building the project, such as:
  - 'mvn compile', 'mvn install', 'mvn verify', 'mvn clean', etc.
  - 'gradle build', 'gradle compileJava', etc.
  - Any equivalent build script execution.
- **Content Retrieval & Reading**: Commands used to read file contents or get data from the system, such as:
  - 'Get-Content', 'type', 'cat', etc.
  - Reading JSON, XML, or other configuration files.
  - 'Select-String', 'grep', 'rg' for searching within files.
- **Project Exploration**: Commands used to navigate and understand the codebase:
  - 'find', 'fd', 'ls', 'dir', 'Get-ChildItem'.
  - 'mvn dependency:tree', 'npm list', etc.
  - Resource listing commands like 'ls', 'dir' on specific resource paths.

## 2. Requirement for Manual Approval (SafeToAutoRun: false)
The following categories of operations MUST ALWAYS require user approval and should NEVER be auto-run:

- **Deletions**: Any command that deletes or destroys files or directories (e.g., 'rm', 'del', 'Remove-Item' where it's not a temporary artifact).
- **Destructive State Changes**: Commands that irreversibly change system state outside of the current workspace.
- **External Network Requests (Mutating)**: Commands that push data to external services (e.g., 'git push').

## 3. General Principles
- Use your best judgment for commands not explicitly listed here.
- Prioritize speed and lack of friction for safe, read-only or build-related tasks.
- Prioritize safety and user control for destructive tasks.