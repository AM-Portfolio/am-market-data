---
trigger: always_on
glob: "**/*.java"
description: "Performance and caching guidelines for the application"
---
# Performance & Optimization
- **High-Performance Caching (Cassette)**: 
  - Target response time: **< 1 second** even under heavy load.
  - Implementation: Use robust strategies like Redis or Caffeine.
  - Configuration: Ensure proper TTL, eviction policies, and cache-aside patterns.
- **Efficient Queries**: Optimize database queries.
  - Use proper indexing.
  - Avoid N+1 problems.
  - Select only necessary columns.
- **Asynchronous Execution**: Offload heavy computational tasks or blocking I/O to background threads.
