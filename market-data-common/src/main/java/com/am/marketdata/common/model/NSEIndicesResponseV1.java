package com.am.marketdata.common.model;

import lombok.Data;
import java.util.List;

@Data
public class NSEIndicesResponseV1 {
    private List<NSEIndexV1> data;
}
