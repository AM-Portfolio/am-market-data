---
description: Build and test the Scraper module
---
# Scraper Module Workflow

This workflow handles operations on the **market-data-scraper** module.

## Keywords
When you say: `scraper`, `scraper module`, `external api`, `upstox`, `kite`, `data fetching`

## Common Operations

1. **Compile Scraper Module**
   // turbo
   ```bash
   mvn compile -pl market-data-scraper
   ```

2. **Test Scraper Module**
   // turbo
   ```bash
   mvn test -pl market-data-scraper -am
   ```

3. **Install Scraper Module**
   // turbo
   ```bash
   mvn install -DskipTests -pl market-data-scraper
   ```
