package com.am.marketdata.common.model.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardOfDirector {
    @JsonProperty("DIRNAME")
    private String directorName;

    @JsonProperty("Reported_DSG")
    private String designation;

    public enum DesignationType {
        CHAIRMAN_AND_MANAGING_DIRECTOR,
        EXECUTIVE_DIRECTOR,
        NON_EXECUTIVE_AND_INDEPENDENT_DIRECTOR,
        NON_EXECUTIVE_DIRECTOR,
        INDEPENDENT_DIRECTOR,
        COMPANY_SECRETARY_AND_COMPLIANCE_OFFICER,
        NON_EXECUTIVE_AND_NON_INDEPENDENT_DIRECTOR
    }

    public DesignationType getDesignationType() {
        return DesignationType.valueOf(designation
                .replace(" & ", "_")
                .replace(".", "")
                .replace(" ", "_")
                .toUpperCase());
    }
}
