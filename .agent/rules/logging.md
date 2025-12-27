---
trigger: always_on
glob: "**/*.java"
description: "Mandatory logging standards using AppLogger"
---
# Operational Excellence & Logging Standards
- **Centralized Logger**: MUST use the common `AppLogger`.
- **Mandatory Log Context**:
    - **Correlation ID**: For tracing requests across the system.
    - **Class Name**: The source class.
    - **Method Name**: The source method.
- **Log Levels Rules**:
    - **DEBUG**: Full payload details (JSON), complete data structures.
    - **INFO**: Minimal execution milestones (e.g., "Job Started"). **NO** large payloads.
    - **WARN**: Missing non-critical data, retries.
    - **ERROR**: Unhandled exceptions, critical failures.
