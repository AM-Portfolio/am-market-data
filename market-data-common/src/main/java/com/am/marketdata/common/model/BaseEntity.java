package com.am.marketdata.common.model;

import lombok.Data;

@Data
public abstract class BaseEntity {
    private Long id;
    private String createdBy;
    private String modifiedBy;
}
