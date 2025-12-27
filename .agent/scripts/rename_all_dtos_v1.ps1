# Complete DTO Renaming Script - Renames ALL DTOs to V1 versioning

Write-Host "=== Complete DTO Renaming to V1 ===" -ForegroundColor Cyan
Write-Host ""

$baseDir = "a:\InfraCode\AM-Portfolio\am-market-data\market-data-common\src\main\java\com\am\marketdata\common\model"

# Define ALL DTOs to rename (excluding already renamed ones)
$dtosToRename = @(
    # Core Models
    @{Old = "Instrument"; New = "InstrumentV1" },
    @{Old = "NSEIndex"; New = "NSEIndexV1" },
    @{Old = "NSEIndicesResponse"; New = "NSEIndicesResponseV1" },
    @{Old = "NSEStockInsidicesData"; New = "NSEStockIndicesDataV1" },  # Also fix typo
    @{Old = "NseETF"; New = "NSEETFDtoV1" },
    @{Old = "NseETFResponse"; New = "NSEETFResponseV1" },
    
    # Financial Data Models
    @{Old = "AbstractFinancialData"; New = "AbstractFinancialDataV1" },
    @{Old = "BalanceSheetData"; New = "BalanceSheetDataV1" },
    @{Old = "BalanceSheetMetrics"; New = "BalanceSheetMetricsV1" },
    @{Old = "BalanceSheetResponse"; New = "BalanceSheetResponseV1" },
    @{Old = "BoardOfDirector"; New = "BoardOfDirectorV1" },
    @{Old = "CashFlowData"; New = "CashFlowDataV1" },
    @{Old = "CashFlowMetrics"; New = "CashFlowMetricsV1" },
    @{Old = "CashFlowResponse"; New = "CashFlowResponseV1" },
    @{Old = "DividendData"; New = "DividendDataV1" },
    @{Old = "DividendMetrics"; New = "DividendMetricsV1" },
    @{Old = "FactSheetDividendResponse"; New = "FactSheetDividendResponseV1" },
    @{Old = "FinancialDataJsonAdapter"; New = "FinancialDataJsonAdapterV1" },
    @{Old = "HalfYearlyFinancialMetrics"; New = "HalfYearlyFinancialMetricsV1" },
    @{Old = "HalfYearlyStatementResponse"; New = "HalfYearlyStatementResponseV1" },
    @{Old = "ProfitLossData"; New = "ProfitLossDataV1" },
    @{Old = "ProfitLossMetrics"; New = "ProfitLossMetricsV1" },
    @{Old = "ProfitLossStatementResponse"; New = "ProfitLossStatementResponseV1" },
    @{Old = "QuarterlyFinancialMetrics"; New = "QuarterlyFinancialMetricsV1" },
    @{Old = "QuaterlyFinancialStatementResponse"; New = "QuarterlyFinancialStatementResponseV1" },  # Also fix typo
    @{Old = "StockFinancialData"; New = "StockFinancialDataV1" },
    @{Old = "StockHalfYearlyData"; New = "StockHalfYearlyDataV1" },
    
    # Event Models
    @{Old = "BalanceSheetFinancialsUpdateEvent"; New = "BalanceSheetFinancialsUpdateEventV1" },
    @{Old = "BoardOfDirectorsUpdateEvent"; New = "BoardOfDirectorsUpdateEventV1" },
    @{Old = "CashFlowFinancialsUpdateEvent"; New = "CashFlowFinancialsUpdateEventV1" },
    @{Old = "FactSheetFinancialsUpdateEvent"; New = "FactSheetFinancialsUpdateEventV1" },
    @{Old = "QuaterlyFinancialsUpdateEvent"; New = "QuarterlyFinancialsUpdateEventV1" },  # Fix typo
    @{Old = "StockProfitAndLossFinancialsUpdateEvent"; New = "StockProfitAndLossFinancialsUpdateEventV1" },
    @{Old = "StockResultsFinancialsUpdateEvent"; New = "StockResultsFinancialsUpdateEventV1" }
)

Write-Host "Step 1: Renaming files using git mv..." -ForegroundColor Yellow
$renamed = 0

foreach ($dto in $dtosToRename) {
    $oldName = $dto.Old
    $newName = $dto.New
    
    # Find the file
    $files = Get-ChildItem -Path $baseDir -Recurse -Filter "$oldName.java"
    
    foreach ($file in $files) {
        $oldPath = $file.FullName -replace '\\', '/'
        $newPath = $oldPath -replace "$oldName\.java$", "$newName.java"
        
        # Convert to relative paths for git
        $relativeOldPath = $oldPath -replace 'A:/InfraCode/AM-Portfolio/am-market-data/', ''
        $relativeNewPath = $newPath -replace 'A:/InfraCode/AM-Portfolio/am-market-data/', ''
        
        try {
            git -c core.quotepath=off mv "$relativeOldPath" "$relativeNewPath" 2>&1 | Out-Null
            Write-Host "  ✓ Renamed: $oldName → $newName" -ForegroundColor Green
            $renamed++
        }
        catch {
            Write-Host "  ✗ Failed to rename: $oldName (may not exist or already renamed)" -ForegroundColor Red
        }
    }
}

Write-Host "`nStep 2: Updating class names inside files..." -ForegroundColor Yellow

# Update class declarations in the renamed files
foreach ($dto in $dtosToRename) {
    $oldName = $dto.Old
    $newName = $dto.New
    
    $files = Get-ChildItem -Path $baseDir -Recurse -Filter "$newName.java"
    
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw
        
        # Update class declaration
        $content = $content -replace "public class $oldName ", "public class $newName "
        $content = $content -replace "public abstract class $oldName ", "public abstract class $newName "
        $content = $content -replace "public enum $oldName ", "public enum $newName "
        
        # Update constructors
        $content = $content -replace "    $oldName\(", "    $newName("
        $content = $content -replace "public $oldName\(", "public $newName("
        $content = $content -replace "private $oldName\(", "private $newName("
        
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}

Write-Host "`nStep 3: Updating all references across the project..." -ForegroundColor Yellow

# Get all Java files
$allJavaFiles = Get-ChildItem -Path "a:\InfraCode\AM-Portfolio\am-market-data" -Recurse -Filter "*.java" | 
Where-Object { $_.FullName -notmatch "\\target\\" -and $_.FullName -notmatch "\\generated\\" }

$filesUpdated = 0

foreach ($file in $allJavaFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    $fileChanged = $false
    
    foreach ($dto in $dtosToRename) {
        $old = $dto.Old
        $new = $dto.New
        
        # Update imports
        if ($content -match "import com\.am\.marketdata\.common\.model\..*\.$old;") {
            $content = $content -replace "import (com\.am\.marketdata\.common\.model\..*)\.$old;", "import `$1.$new;"
            $fileChanged = $true
        }
        
        # Update type references (be careful with word boundaries)
        $patterns = @(
            @{Pattern = "\<$old\>"; Replacement = "<$new>" },
            @{Pattern = "\($old\)"; Replacement = "($new)" },
            @{Pattern = " $old "; Replacement = " $new " },
            @{Pattern = " $old,"; Replacement = " $new," },
            @{Pattern = ",$old\>"; Replacement = ",$new>" },
            @{Pattern = "List\<$old\>"; Replacement = "List<$new>" },
            @{Pattern = "Map\<String, $old\>"; Replacement = "Map<String, $new>" }
        )
        
        foreach ($pattern in $patterns) {
            if ($content -match [regex]::Escape($pattern.Pattern)) {
                $content = $content -replace [regex]::Escape($pattern.Pattern), $pattern.Replacement
                $fileChanged = $true
            }
        }
    }
    
    if ($fileChanged) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        $filesUpdated++
    }
}

Write-Host "`n=== Summary ===" -ForegroundColor Cyan
Write-Host "Files renamed: $renamed" -ForegroundColor Green
Write-Host "Files updated with new references: $filesUpdated" -ForegroundColor Green
Write-Host "`nNext: Run 'mvn clean compile -DskipTests' to verify" -ForegroundColor Yellow
