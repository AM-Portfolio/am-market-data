package com.am.marketdata.common.model.tradeB.financials.results;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

@Data
public class StockFinancialData {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnore
    private ObjectNode data = objectMapper.createObjectNode();

    @JsonAnyGetter
    public JsonNode getData() {
        return data;
    }

    @JsonAnySetter
    public void setData(String key, Object value) {
        if (value instanceof JsonNode) {
            data.set(key, (JsonNode) value);
        } else if (value instanceof String) {
            try {
                // Try to parse the string as JSON
                JsonNode jsonValue = objectMapper.readTree((String) value);
                data.set(key, jsonValue);
            } catch (Exception e) {
                // If it's not JSON, store it as a simple string
                data.put(key, value.toString());
            }
        } else {
            data.put(key, value.toString());
        }
    }

    /**
     * Get a specific quarter's data as a JsonNode
     * @param quarterKey The quarter key (e.g., "202412")
     * @return JsonNode containing the quarter's data
     */
    public JsonNode getQuarterData(String quarterKey) {
        return data.get(quarterKey);
    }

    /**
     * Get all quarter keys
     * @return Set of all quarter keys
     */
    public Set<String> getQuarterKeys() {
        Set<String> keys = new HashSet<>();
        Iterator<String> fieldNames = data.fieldNames();
        while (fieldNames.hasNext()) {
            keys.add(fieldNames.next());
        }
        return keys;
    }

    /**
     * Convert a quarter's data into a QuarterlyFinancialMetrics object
     * @param quarterKey The quarter key (e.g., "202412")
     * @return QuarterlyFinancialMetrics object containing the quarter's data
     */
    public QuarterlyFinancialMetrics getQuarterlyMetrics(String quarterKey) {
        JsonNode quarterNode = data.get(quarterKey);
        if (quarterNode == null) {
            return null;
        }

        // If the quarter data is stored as a string, parse it into a map
        Map<String, String> quarterData = new HashMap<>();
        if (quarterNode.isTextual()) {
            String quarterText = quarterNode.asText();
            // Remove the outer quotes and braces if present
            quarterText = quarterText.replaceAll("^\"|\"$", "");
            quarterText = quarterText.replaceAll("^\\{|\\}$", "");
            
            // Split by comma and equals sign
            String[] pairs = quarterText.split(", ");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    quarterData.put(keyValue[0], keyValue[1]);
                }
            }
        }

        QuarterlyFinancialMetrics metrics = new QuarterlyFinancialMetrics();
        
        // Set all fields from the map to QuarterlyFinancialMetrics
        if (!quarterData.isEmpty()) {
            metrics.setYearEnd(quarterData.get("year_end"));
            metrics.setTotalRevenue(parseDouble(quarterData.get("total_revenue")));
            metrics.setNetProfitMargin(parseDouble(quarterData.get("net_profit_margin")));
            metrics.setOtherIncome(parseDouble(quarterData.get("other_income")));
            metrics.setTotalExpenses(parseDouble(quarterData.get("total_expenses")));
            metrics.setEmployeeCost(parseDouble(quarterData.get("employee_cost")));
            metrics.setOperatingRevenue(parseDouble(quarterData.get("operating_revenue")));
            metrics.setDepreciationAndAmortization(parseDouble(quarterData.get("depreciation_and_amortization")));
            metrics.setInterest(parseDouble(quarterData.get("interest")));
            metrics.setProfitBeforeTax(parseDouble(quarterData.get("profit_before_tax")));
            metrics.setTax(parseDouble(quarterData.get("tax")));
            metrics.setPatMargin(parseDouble(quarterData.get("pat_margin")));
            metrics.setNetProfit(parseDouble(quarterData.get("net_profit")));
            metrics.setProfitAfterTax(parseDouble(quarterData.get("profit_after_tax")));
            metrics.setMinorityShare(parseDouble(quarterData.get("minority_share")));
            metrics.setProfitFromAssociates(parseDouble(quarterData.get("profit_from_associates")));
            metrics.setAdjEpsInRsBasic(parseDouble(quarterData.get("adj_eps_in_rs_basic")));
            metrics.setAdjEpsInRsDiluted(parseDouble(quarterData.get("adj_eps_in_rs_diluted")));
            metrics.setOperatingExpenses(parseDouble(quarterData.get("operating_expenses")));
            metrics.setOperationProfit(parseDouble(quarterData.get("operation_profit")));
            metrics.setOpmPercentage(parseDouble(quarterData.get("opm_percentage")));
            metrics.setTaxPer(parseDouble(quarterData.get("tax_per")));
            metrics.setRevenueGrowthPer(parseDouble(quarterData.get("revenue_growth_per")));
            metrics.setNetProfitGrowth(parseDouble(quarterData.get("net_profit_growth")));
            metrics.setNetProfitMarginGrowth(parseDouble(quarterData.get("net_profit_margin_growth")));
            metrics.setPatGrowth(parseDouble(quarterData.get("pat_growth")));
            metrics.setPatMarginGrowth(parseDouble(quarterData.get("pat_margin_growth")));
        } else {
            // If the map is empty, try using the original methods
            metrics.setYearEnd(getFieldValue(quarterKey, "year_end"));
            metrics.setTotalRevenue(getFieldAsDouble(quarterKey, "total_revenue"));
            metrics.setNetProfitMargin(getFieldAsDouble(quarterKey, "net_profit_margin"));
            metrics.setOtherIncome(getFieldAsDouble(quarterKey, "other_income"));
            metrics.setTotalExpenses(getFieldAsDouble(quarterKey, "total_expenses"));
            metrics.setEmployeeCost(getFieldAsDouble(quarterKey, "employee_cost"));
            metrics.setOperatingRevenue(getFieldAsDouble(quarterKey, "operating_revenue"));
            metrics.setDepreciationAndAmortization(getFieldAsDouble(quarterKey, "depreciation_and_amortization"));
            metrics.setInterest(getFieldAsDouble(quarterKey, "interest"));
            metrics.setProfitBeforeTax(getFieldAsDouble(quarterKey, "profit_before_tax"));
            metrics.setTax(getFieldAsDouble(quarterKey, "tax"));
            metrics.setPatMargin(getFieldAsDouble(quarterKey, "pat_margin"));
            metrics.setNetProfit(getFieldAsDouble(quarterKey, "net_profit"));
            metrics.setProfitAfterTax(getFieldAsDouble(quarterKey, "profit_after_tax"));
            metrics.setMinorityShare(getFieldAsDouble(quarterKey, "minority_share"));
            metrics.setProfitFromAssociates(getFieldAsDouble(quarterKey, "profit_from_associates"));
            metrics.setAdjEpsInRsBasic(getFieldAsDouble(quarterKey, "adj_eps_in_rs_basic"));
            metrics.setAdjEpsInRsDiluted(getFieldAsDouble(quarterKey, "adj_eps_in_rs_diluted"));
            metrics.setOperatingExpenses(getFieldAsDouble(quarterKey, "operating_expenses"));
            metrics.setOperationProfit(getFieldAsDouble(quarterKey, "operation_profit"));
            metrics.setOpmPercentage(getFieldAsDouble(quarterKey, "opm_percentage"));
            metrics.setTaxPer(getFieldAsDouble(quarterKey, "tax_per"));
            metrics.setRevenueGrowthPer(getFieldAsDouble(quarterKey, "revenue_growth_per"));
            metrics.setNetProfitGrowth(getFieldAsDouble(quarterKey, "net_profit_growth"));
            metrics.setNetProfitMarginGrowth(getFieldAsDouble(quarterKey, "net_profit_margin_growth"));
            metrics.setPatGrowth(getFieldAsDouble(quarterKey, "pat_growth"));
            metrics.setPatMarginGrowth(getFieldAsDouble(quarterKey, "pat_margin_growth"));
        }

        return metrics;
    }

    /**
     * Parse a string to Double, handling null and exceptions
     * @param value String value to parse
     * @return Double value or null if parsing fails
     */
    private Double parseDouble(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Get a specific field value from a quarter
     * @param quarterKey The quarter key
     * @param fieldName The field name
     * @return Value of the field as String
     */
    public String getFieldValue(String quarterKey, String fieldName) {
        JsonNode quarterNode = data.get(quarterKey);
        if (quarterNode == null) {
            return null;
        }

        if (quarterNode.isObject() && quarterNode.has(fieldName)) {
            JsonNode fieldValue = quarterNode.get(fieldName);
            if (fieldValue.isTextual()) {
                return fieldValue.asText();
            } else if (fieldValue.isNumber()) {
                return fieldValue.asText();
            } else {
                return null;
            }
        } else if (quarterNode.isTextual()) {
            // If the quarter data is stored as a string, parse it into a map
            String quarterText = quarterNode.asText();
            // Remove the outer quotes and braces if present
            quarterText = quarterText.replaceAll("^\"|\"$", "");
            quarterText = quarterText.replaceAll("^\\{|\\}$", "");
            
            // Split by comma and equals sign
            String[] pairs = quarterText.split(", ");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2 && keyValue[0].equals(fieldName)) {
                    return keyValue[1];
                }
            }
        }

        return null;
    }

    /**
     * Get a specific field value from a quarter as Double
     * @param quarterKey The quarter key
     * @param fieldName The field name
     * @return Value of the field as Double
     */
    public Double getFieldAsDouble(String quarterKey, String fieldName) {
        String value = getFieldValue(quarterKey, fieldName);
        return parseDouble(value);
    }
}
