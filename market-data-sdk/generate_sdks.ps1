# Powershell Script to Generate SDKs from OpenAPI Spec
$ErrorActionPreference = "Stop"

# Define Paths
$API_DIR = Join-Path $PSScriptRoot "../market-data-api"
$SCHEMA_FILE_NAME = "openapi.json"
$LOCAL_SCHEMA_PATH = Join-Path $API_DIR ("target/" + $SCHEMA_FILE_NAME)

$OUTPUT_DIR_PYTHON = Join-Path $PSScriptRoot "market-data-sdk-python"
$OUTPUT_DIR_DART = Join-Path $PSScriptRoot "market-data-sdk-flutter"
$OUTPUT_DIR_JAVA = Join-Path $PSScriptRoot "market-data-sdk-java"

Write-Host "=========================================="
Write-Host "Market Data SDK Generator (API-Based)"
Write-Host "=========================================="

# 1. Generate Schema via Unit Test (in API module)
Write-Host "[1/4] Generating Schema via market-data-api tests..."
Push-Location $API_DIR

# Check if target exists, create if not
if (-Not (Test-Path "target")) { New-Item -ItemType Directory -Path "target" | Out-Null }

try {
    # Run ONLY the generator test in market-data-api
    mvn test -Dtest=OpenApiGeneratorTest
}
catch {
    Write-Error "Maven test failed in market-data-api. Schema generation aborted."
    Pop-Location
    exit 1
}
Pop-Location

# Verify Schema Exists
if (-Not (Test-Path $LOCAL_SCHEMA_PATH)) {
    Write-Error "Schema file not found at: $LOCAL_SCHEMA_PATH after running test."
    exit 1
}
else {
    Write-Host "Schema generated successfully!"
}

# 2. Clean existing SDK directories (optional - remove old generated files)
Write-Host "[2/5] Cleaning existing SDK directories..."
if (Test-Path $OUTPUT_DIR_PYTHON) {
    Write-Host "  Removing old Python SDK..."
    Remove-Item -Recurse -Force $OUTPUT_DIR_PYTHON
}
if (Test-Path $OUTPUT_DIR_DART) {
    Write-Host "  Removing old Dart SDK..."
    Remove-Item -Recurse -Force $OUTPUT_DIR_DART
}
if (Test-Path $OUTPUT_DIR_JAVA) {
    Write-Host "  Removing old Java SDK..."
    Remove-Item -Recurse -Force $OUTPUT_DIR_JAVA
}

# 3. Generate Python Client
Write-Host "[3/5] Generating Python Client..."
cmd /c openapi-generator-cli generate -i $LOCAL_SCHEMA_PATH -g python -o $OUTPUT_DIR_PYTHON `
    --package-name market_data_client `
    --git-repo-id am-market-data `
    --git-user-id AM-Portfolio

# 4. Generate Dart Client (Flutter)
Write-Host "[4/5] Generating Dart/Flutter Client..."
cmd /c openapi-generator-cli generate -i $LOCAL_SCHEMA_PATH -g dart -o $OUTPUT_DIR_DART `
    --additional-properties=pubName=market_data_client

# 5. Generate Java Client
Write-Host "[5/5] Generating Java Client..."
cmd /c openapi-generator-cli generate -i $LOCAL_SCHEMA_PATH -g java -o $OUTPUT_DIR_JAVA `
    --api-package com.am.marketdata.api `
    --model-package com.am.marketdata.model `
    --invoker-package com.am.marketdata.client `
    --group-id com.am `
    --artifact-id market-data-sdk-java `
    --library native

Write-Host "=========================================="
Write-Host "SDK Generation Complete."
Write-Host "=========================================="
