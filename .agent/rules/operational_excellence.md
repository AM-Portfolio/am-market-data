---
trigger: always_on
glob: "**/*.java"
description: "Logging, Exception Handling, and Observability"
---
# Operational Excellence & Logging

## 1. Logging Standards
- **Framework**: Use AppLogger from market-data-common (wraps SLF4J/Logback).
- **Format**: JSON in Production.
- **Context**: Ensure 	race_id and span_id are present in MDC.

## 2. Log Levels
- **ERROR**: Critical failures (DB down, Data loss).
- **WARN**: Recoverable issues (Retries, Rate limits).
- **INFO**: Milestones (Job Started, Job Finished). NO data payloads.
- **DEBUG**: Full variable state and raw payloads.

## 3. Exception Handling
- **Global Handler**: Use @ControllerAdvice in market-data-api to map exceptions to standard HTTP responses.
- **Custom Exceptions**: Define business exceptions in market-data-common (e.g., MarketDataException).
