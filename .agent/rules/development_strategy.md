---
trigger: always_on
glob: "**/*.java"
description: "Development strategy and feature implementation guidelines"
---
# Development Strategy

## 1. Feature Development Workflow

### Phase 1: Planning
1. **Understand Requirements**: Clarify business goals and acceptance criteria.
2. **Design API First**: Define DTOs in *-common, then API contracts in *-api.
3. **Create Implementation Plan**: Document in implementation_plan.md with:
   - Problem statement
   - Proposed changes (file-by-file)
   - Verification steps

### Phase 2: Implementation
1. **Bottom-Up Approach**:
   - Start with *-common (DTOs, Enums)
   - Then *-api (Controllers, Interfaces)
   - Finally *-service (Business Logic)
2. **Incremental Commits**: Small, focused commits with clear messages.
3. **Continuous Verification**: Run mvn compile after each logical change.

### Phase 3: Testing
1. **Unit Tests First**: Write tests before or alongside implementation.
2. **Integration Tests**: Verify module interactions.
3. **Manual Testing**: Use Postman collections from market-data-versions.

### Phase 4: Documentation
1. **Update API Docs**: Ensure Swagger/OpenAPI is current.
2. **Update README**: Document new features or configuration changes.
3. **Create Walkthrough**: Document what was built and how to use it.

## 2. Code Development Guidelines

### Naming Conventions
- **Classes**: PascalCase, descriptive (e.g., MarketDataFetchService)
- **Methods**: camelCase, verb-based (e.g., etchHistoricalData)
- **Variables**: camelCase, meaningful (avoid data, 	emp, obj)
- **Constants**: UPPER_SNAKE_CASE (e.g., MAX_RETRY_ATTEMPTS)

### Method Design
- **Single Responsibility**: One method, one purpose.
- **Max Length**: Keep methods under 30 lines; extract helpers if longer.
- **Parameters**: Max 4 parameters; use DTOs for complex inputs.
- **Return Types**: Use specific types, not Object or Map<String, Object>.

### Error Handling
- **Fail Fast**: Validate inputs at method entry.
- **Specific Exceptions**: Throw custom exceptions (e.g., MarketDataException).
- **Logging**: Log at ERROR for failures, WARN for retries, INFO for milestones.
- **Never Swallow**: Don't catch exceptions without handling or re-throwing.

### Performance Best Practices
- **Avoid N+1 Queries**: Use JOIN FETCH or batch operations.
- **Cache Wisely**: Cache static/slow-changing data (indices, instruments).
- **Async I/O**: Use @Async or CompletableFuture for external calls.
- **Pagination**: Always paginate large result sets.

### Security
- **Input Validation**: Validate all user inputs.
- **SQL Injection**: Use parameterized queries (JPA handles this).
- **Sensitive Data**: Never log passwords, tokens, or PII.
- **Rate Limiting**: Implement for public APIs.

## 3. Common Patterns

### Repository Pattern
\\\java
public interface MarketDataRepository extends JpaRepository<MarketDataEntity, String> {
    Optional<MarketDataEntity> findBySymbol(String symbol);
}
\\\

### Service Layer Pattern
\\\java
@Service
public class MarketDataServiceImpl implements MarketDataService {
    private final MarketDataRepository repository;
    
    @Override
    public MarketData getMarketData(String symbol) {
        return repository.findBySymbol(symbol)
            .map(this::toDto)
            .orElseThrow(() -> new MarketDataNotFoundException(symbol));
    }
}
\\\

### Controller Pattern
\\\java
@RestController
@RequestMapping(\"/api/v1/market-data\")
public class MarketDataController {
    private final MarketDataService service;
    
    @GetMapping(\"/{symbol}\")
    public ResponseEntity<MarketData> getMarketData(@PathVariable String symbol) {
        return ResponseEntity.ok(service.getMarketData(symbol));
    }
}
\\\

## 4. Build & Verification Strategy

### Before Committing
1. **Compile**: mvn compile -DskipTests
2. **Test**: mvn test -pl <module> -am
3. **Verify**: mvn verify -DskipTests

### CI/CD Readiness
- All tests must pass.
- No compilation warnings.
- Code coverage meets thresholds.

## 5. Troubleshooting Checklist

### Build Failures
- [ ] Check dependency versions in pom.xml
- [ ] Verify module dependencies are correct
- [ ] Run mvn clean install on dependent modules first

### Runtime Errors
- [ ] Check application logs for stack traces
- [ ] Verify environment variables in .env
- [ ] Ensure Redis/MongoDB are running

### Test Failures
- [ ] Check test data setup
- [ ] Verify mocks are configured correctly
- [ ] Ensure test isolation (no shared state)
