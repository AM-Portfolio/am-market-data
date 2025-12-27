from pathlib import Path

# Expected API clients (one per controller)
EXPECTED_API_CLIENTS = [
    "BrokerageCalculatorApi",
    "MarginCalculatorApi",
    "MarketAnalyticsApi",
    "MarketDataApi",
    "MarketDataPollingApi",
    "MarketIndicesApi",
    "SecurityMetadataApi",
    "StockIndicesApi"
]

# Paths
SCRIPTS_DIR = Path(__file__).parent
SDK_DIR = SCRIPTS_DIR.parent
PROJECT_ROOT = SDK_DIR.parent
API_DIR = PROJECT_ROOT / "market-data-api"
SCHEMA_PATH = API_DIR / "target" / "openapi.json"
ASYNC_SCHEMA_PATH = SDK_DIR / "asyncapi.yaml"

SDKS = {
    "Python": {
        "path": SDK_DIR / "market-data-sdk-python",
        "gen": "python",
        "props": "--package-name market_data_client --git-repo-id am-market-data --git-user-id AM-Portfolio"
    },
    "Dart": {
        "path": SDK_DIR / "market-data-sdk-flutter",
        "gen": "dart",
        "props": "--additional-properties=pubName=market_data_client"
    },
    "Java": {
        "path": SDK_DIR / "market-data-sdk-java",
        "gen": "java",
        "props": "--api-package com.am.marketdata.api --model-package com.am.marketdata.model --invoker-package com.am.marketdata.client --group-id com.am --artifact-id market-data-sdk-java --library native"
    }
}
