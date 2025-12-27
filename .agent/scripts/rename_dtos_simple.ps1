# Complete DTO Renaming - Simple Find and Replace Approach
Write-Host "=== Starting Complete DTO Renaming ===" -ForegroundColor Cyan

# All DTOs to rename (old name -> new name)
$renamings = @{
    "Instrument"                              = "InstrumentV1"
    "NSEIndex"                                = "NSEIndexV1"
    "NSEIndicesResponse"                      = "NSEIndicesResponseV1"
    "NSEStockInsidicesData"                   = "NSEStockIndicesDataV1"
    "NseETF"                                  = "NSEETFDtoV1"
    "NseETFResponse"                          = "NSEETFResponseV1"
    "AbstractFinancialData"                   = "AbstractFinancialDataV1"
    "BalanceSheetData"                        = "BalanceSheetDataV1"
    "BalanceSheetMetrics"                     = "BalanceSheetMetricsV1"
    "BalanceSheetResponse"                    = "BalanceSheetResponseV1"
    "BoardOfDirector"                         = "BoardOfDirectorV1"
    "CashFlowData"                            = "CashFlowDataV1"
    "CashFlowMetrics"                         = "CashFlowMetricsV1"
    "CashFlowResponse"                        = "CashFlowResponseV1"
    "DividendData"                            = "DividendDataV1"
    "DividendMetrics"                         = "DividendMetricsV1"
    "FactSheetDividendResponse"               = "FactSheetDividendResponseV1"
    "FinancialDataJsonAdapter"                = "FinancialDataJsonAdapterV1"
    "HalfYearlyFinancialMetrics"              = "HalfYearlyFinancialMetricsV1"
    "HalfYearlyStatementResponse"             = "HalfYearlyStatementResponseV1"
    "ProfitLossData"                          = "ProfitLossDataV1"
    "ProfitLossMetrics"                       = "ProfitLossMetricsV1"
    "ProfitLossStatementResponse"             = "ProfitLossStatementResponseV1"
    "QuarterlyFinancialMetrics"               = "QuarterlyFinancialMetricsV1"
    "QuaterlyFinancialStatementResponse"      = "QuarterlyFinancialStatementResponseV1"
    "StockFinancialData"                      = "StockFinancialDataV1"
    "StockHalfYearlyData"                     = "StockHalfYearlyDataV1"
    "BalanceSheetFinancialsUpdateEvent"       = "BalanceSheetFinancialsUpdateEventV1"
    "BoardOfDirectorsUpdateEvent"             = "BoardOfDirectorsUpdateEventV1"
    "CashFlowFinancialsUpdateEvent"           = "CashFlowFinancialsUpdateEventV1"
    "FactSheetFinancialsUpdateEvent"          = "FactSheetFinancialsUpdateEventV1"
    "QuaterlyFinancialsUpdateEvent"           = "QuarterlyFinancialsUpdateEventV1"
    "StockProfitAndLossFinancialsUpdateEvent" = "StockProfitAndLossFinancialsUpdateEventV1"
    "StockResultsFinancialsUpdateEvent"       = "StockResultsFinancialsUpdateEventV1"
}

Write-Host "Step 1: Updating all Java files with new references..." -ForegroundColor Yellow

$allJavaFiles = Get-ChildItem -Path "." -Recurse -Filter "*.java" | 
Where-Object { $_.FullName -notmatch "\\target\\" -and $_.FullName -notmatch "\\generated\\" }

$totalUpdated = 0

foreach ($file in $allJavaFiles) {
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
    if (-not $content) { continue }
    
    $modified = $false
    
    foreach ($old in $renamings.Keys) {
        $new = $renamings[$old]
        
        # Pattern 1: Import statements
        if ($content -match "import\s+com\.am\.marketdata\.common\.model\..*\.$old;") {
            $content = $content -replace "import\s+(com\.am\.marketdata\.common\.model\..*)\.$old;", "import `$1.$new;"
            $modified = $true
        }
        
        # Pattern 2: Class declarations
        if ($content -match "public\s+(class|abstract\s+class|enum)\s+$old\s") {
            $content = $content -replace "public\s+(class|abstract\s+class|enum)\s+$old\s", "public `$1 $new "
            $modified = $true
        }
        
        # Pattern 3: Constructors
        if ($content -match "\s+$old\(") {
            $content = $content -replace "(\s+)$old\(", "`$1$new("
            $modified = $true
        }
        
        # Pattern 4: Generic type parameters
        if ($content -match "<$old>") {
            $content = $content -replace "<$old>", "<$new>"
            $modified = $true
        }
        
        # Pattern 5: List/Map types
        if ($content -match "List<$old>") {
            $content = $content -replace "List<$old>", "List<$new>"
            $modified = $true
        }
        
        if ($content -match "Map<String,\s*$old>") {
            $content = $content -replace "Map<String,\s*$old>", "Map<String, $new>"
            $modified = $true
        }
        
        # Pattern 6: Variable declarations and casts
        if ($content -match "\s$old\s") {
            $content = $content -replace "(\s)$old(\s)", "`$1$new`$2"
            $modified = $true
        }
        
        if ($content -match "\($old\)") {
            $content = $content -replace "\($old\)", "($new)"
            $modified = $true
        }
    }
    
    if ($modified) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        $totalUpdated++
        Write-Host "  Updated: $($file.Name)" -ForegroundColor Green
    }
}

Write-Host "`nStep 2: Renaming actual files..." -ForegroundColor Yellow

$baseDir = "market-data-common\src\main\java\com\am\marketdata\common\model"
$filesRenamed = 0

foreach ($old in $renamings.Keys) {
    $new = $renamings[$old]
    
    $files = Get-ChildItem -Path $baseDir -Recurse -Filter "$old.java" -ErrorAction SilentlyContinue
    
    foreach ($file in $files) {
        $newPath = $file.FullName -replace "$old\.java$", "$new.java"
        
        try {
            Move-Item -Path $file.FullName -Destination $newPath -Force
            Write-Host "  Renamed: $old.java -> $new.java" -ForegroundColor Green
            $filesRenamed++
        }
        catch {
            Write-Host "  Failed: $old.java (may not exist)" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Java files updated: $totalUpdated" -ForegroundColor Green
Write-Host "DTO files renamed: $filesRenamed" -ForegroundColor Green
Write-Host "`nDone! Run 'mvn clean compile -DskipTests' to verify." -ForegroundColor Yellow
