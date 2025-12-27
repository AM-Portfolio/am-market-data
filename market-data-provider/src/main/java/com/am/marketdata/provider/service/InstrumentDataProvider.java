package com.am.marketdata.provider.service;

import com.am.marketdata.provider.dto.InstrumentSearchCriteria;
import java.io.IOException;
import java.util.List;

public interface InstrumentDataProvider {

    String getProviderName();

    void updateInstruments(String source) throws IOException;

    List<?> searchInstruments(InstrumentSearchCriteria criteria);
}
