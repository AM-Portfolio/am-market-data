---
trigger: always_on
glob: "**/*sdk*"
description: "SDK Generation, Automation, and Platform Specifics"
---
# SDK Automation & Architecture

## 1. Source of Truth
- **Module**: market-data-api is the master.
- **Mechanism**: mvn test in API module -> openapi.json -> openapi-generator.

## 2. Platform Rules
- **Java**: Native HttpClient / Retrofit. Package: com.am.marketdata.client.
- **Python**: Pydantic Models. Package: market_data_client.
- **Dart**: Dio + Freezed. Package: market_data_client.

## 3. Automation Workflow
1.  **Update DTO**: In market-data-common.
2.  **Expose**: In market-data-api Controller.
3.  **Generate**: Run market-data-sdk/generate_sdks.ps1.
