package com.am.marketdata.service.mapper;

import com.am.common.investment.model.equity.Instrument;
import com.am.common.investment.model.equity.Instrument.InstrumentType;
import com.am.common.investment.model.equity.Instrument.Segment;
import com.am.marketdata.common.model.InstrumentV1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class to convert between Zerodha InstrumentV1 and AM Common
 * InstrumentV1
 * models
 */
@Component
public class InstrumentMapper {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InstrumentMapper.class);

    /**
     * Convert a Zerodha InstrumentV1 to AM common InstrumentV1 model
     *
     * @param zerodhaInstrument Zerodha InstrumentV1 model
     * @return AM common InstrumentV1 model
     */
    public Instrument toCommonInstrument(com.zerodhatech.models.Instrument zerodhaInstrument) {
        if (zerodhaInstrument == null) {
            return null;
        }

        try {
            Instrument instrument = new Instrument();

            // Map basic properties
            instrument.setTradingSymbol(zerodhaInstrument.tradingsymbol);
            instrument.setInstrumentToken(zerodhaInstrument.instrument_token);
            instrument.setName(zerodhaInstrument.name);
            instrument.setExchangeToken(zerodhaInstrument.exchange_token);

            // Map Instrument type
            if (zerodhaInstrument.instrument_type != null) {
                instrument.setInstrumentType(mapInstrumentType(zerodhaInstrument.instrument_type));
            }

            // Map segment - assuming there's a method to set segment
            if (zerodhaInstrument.segment != null) {
                instrument.setSegment(mapSegment(zerodhaInstrument.segment));
            }

            // Map tick size and lot size
            if (zerodhaInstrument.tick_size > 0) {
                instrument.setTickSize(BigDecimal.valueOf(zerodhaInstrument.tick_size));
            }
            instrument.setLotSize(zerodhaInstrument.lot_size);

            // Map expiry if available
            if (zerodhaInstrument.expiry != null) {
                instrument.setExpiry(zerodhaInstrument.expiry);
            }

            return instrument;
        } catch (Exception e) {
            log.error("Error mapping Zerodha Instrument to common instrument: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert a list of Zerodha instruments to a list of AM common instruments
     *
     * @param zerodhaInstruments List of Zerodha Instrument models
     * @return List of AM common Instrument models
     */
    public List<Instrument> toCommonInstruments(List<com.zerodhatech.models.Instrument> zerodhaInstruments) {
        if (zerodhaInstruments == null || zerodhaInstruments.isEmpty()) {
            return new ArrayList<>();
        }

        return zerodhaInstruments.stream()
                .map(this::toCommonInstrument)
                .filter(instrument -> instrument != null)
                .collect(Collectors.toList());
    }

    /**
     * Convert the new provider common InstrumentV1 to service's internal Instrument
     * model
     *
     * @param providerInstrument Provider's common InstrumentV1 model
     * @return Service's internal Instrument model
     */
    public Instrument fromProviderInstrument(InstrumentV1 providerInstrument) {
        if (providerInstrument == null) {
            return null;
        }

        try {
            Instrument instrument = new Instrument();
            instrument.setTradingSymbol(providerInstrument.getTradingSymbol());

            // Map token safely
            String token = providerInstrument.getInstrumentToken();
            if (token != null && !token.isEmpty()) {
                try {
                    instrument.setInstrumentToken(Long.parseLong(token));
                } catch (NumberFormatException e) {
                    instrument.setInstrumentToken(0L);
                }
            }

            instrument.setName(providerInstrument.getName());

            // Map exchange token
            String exToken = providerInstrument.getExchangeToken();
            if (exToken != null && !exToken.isEmpty()) {
                try {
                    instrument.setExchangeToken(Long.parseLong(exToken));
                } catch (NumberFormatException e) {
                    instrument.setExchangeToken(0L);
                }
            }

            // Map type and segment
            instrument.setInstrumentType(mapInstrumentType(providerInstrument.getInstrumentType()));
            instrument.setSegment(mapSegment(providerInstrument.getSegment()));

            // Map tick and lot size
            if (providerInstrument.getTickSize() != null) {
                instrument.setTickSize(BigDecimal.valueOf(providerInstrument.getTickSize()));
            }
            if (providerInstrument.getLotSize() != null) {
                instrument.setLotSize(providerInstrument.getLotSize().intValue());
            }

            if (providerInstrument.getExpiry() != null && !providerInstrument.getExpiry().isEmpty()) {
                try {
                    instrument.setExpiry(
                            new java.text.SimpleDateFormat("yyyy-MM-dd").parse(providerInstrument.getExpiry()));
                } catch (Exception e) {
                    log.warn("Error parsing expiry date {}: {}", providerInstrument.getExpiry(), e.getMessage());
                }
            }
            return instrument;
        } catch (Exception e) {
            log.error("Error mapping provider Instrument to internal instrument: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert a list of provider common instruments to a list of internal
     * instruments
     *
     * @param providerInstruments List of provider's common InstrumentV1 models
     * @return List of internal Instrument models
     */
    public List<Instrument> fromProviderInstruments(
            List<InstrumentV1> providerInstruments) {
        if (providerInstruments == null || providerInstruments.isEmpty()) {
            return new ArrayList<>();
        }

        return providerInstruments.stream()
                .map(this::fromProviderInstrument)
                .filter(instrument -> instrument != null)
                .collect(Collectors.toList());
    }

    /**
     * Map Zerodha InstrumentV1 type to AM common InstrumentV1 type
     *
     * @param zerodhaType Zerodha InstrumentV1 type
     * @return AM common InstrumentV1 type
     */
    private InstrumentType mapInstrumentType(String zerodhaType) {
        if (zerodhaType == null) {
            return InstrumentType.UNKNOWN;
        }

        switch (zerodhaType.toUpperCase()) {
            case "EQ":
                return InstrumentType.EQUITY;
            case "FUT":
                return InstrumentType.FUTURE;
            case "OPT":
                return InstrumentType.UNKNOWN;
            case "CE":
                return InstrumentType.CALL_OPTION;
            case "PE":
                return InstrumentType.PUT_OPTION;
            case "IDX":
                return InstrumentType.INDEX;
            case "ETF":
                return InstrumentType.UNKNOWN;
            default:
                return InstrumentType.UNKNOWN;
        }
    }

    /**
     * Map Zerodha segment to AM common segment
     *
     * @param zerodhaSegment Zerodha segment
     * @return AM common segment
     */
    private Segment mapSegment(String zerodhaSegment) {
        if (zerodhaSegment == null) {
            return Segment.UNKNOWN;
        }

        switch (zerodhaSegment.toUpperCase()) {
            case "NSE":
                return Segment.NSE;
            case "BSE":
                return Segment.BSE;
            case "MCX_OPT":
                return Segment.MCX_OPT;
            case "NFO_FUT":
                return Segment.NFO_FUT;
            case "NCO_FUT":
                return Segment.NCO_FUT;
            case "NFO_OPT":
                return Segment.NFO_OPT;
            default:
                return Segment.UNKNOWN;
        }
    }

}
