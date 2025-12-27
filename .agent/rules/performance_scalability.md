---
trigger: always_on
glob: "**/*cache*"
description: "Caching, DB Optimization, and Async Patterns"
---
# Performance & Scalability

## 1. Caching
- **L1**: Caffeine (Local).
- **L2**: Redis (Shared).
- **Strategy**: Cache-Aside.

## 2. Database
- **No N+1**: Use JOIN FETCH.
- **Async**: Use @Async or CompletableFuture for non-blocking I/O.
