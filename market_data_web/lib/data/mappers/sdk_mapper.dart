import 'package:market_data_client/api.dart';
import '../../domain/models/security_quote.dart';
import '../../domain/models/market_index.dart';
import '../../domain/models/candle.dart';

/// Anti-Corruption Layer (ACL) Mapper.
/// Translates SDK-specific models into clean Domain entities.
class SdkMapper {
  /// Map a single real-time update to a SecurityQuote.
  static SecurityQuote mapUpdateToQuote(MarketDataUpdateV1 update) {
    return SecurityQuote(
      symbol: update.instrumentKey ?? 'Unknown',
      lastPrice: update.lastPrice ?? 0.0,
      change: update.change ?? 0.0,
      pChange: update.pChange ?? 0.0,
      open: update.open ?? 0.0,
      high: update.high ?? 0.0,
      low: update.low ?? 0.0,
      prevClose: update.previousClose ?? 0.0,
      lastUpdateTime: DateTime.tryParse(update.timestamp ?? '') ?? DateTime.now(),
    );
  }

  /// Map SDK StockData (from polling/REST) to SecurityQuote.
  static SecurityQuote mapStockDataToQuote(StockData data) {
    return SecurityQuote(
      symbol: data.symbol ?? 'Unknown',
      lastPrice: data.lastPrice ?? 0.0,
      change: data.change ?? 0.0,
      pChange: data.pChange ?? 0.0,
      open: data.open ?? 0.0,
      high: data.dayHigh ?? 0.0,
      low: data.dayLow ?? 0.0,
      prevClose: data.previousClose ?? (data.lastPrice ?? 0.0) - (data.change ?? 0.0),
      lastUpdateTime: DateTime.now(),
    );
  }

  /// Map SDK NSEIndexV1 to SecurityQuote.
  static SecurityQuote mapNSEIndexToQuote(NSEIndexV1 index) {
    return SecurityQuote(
      symbol: index.indexSymbol ?? index.index ?? 'Unknown',
      name: index.index ?? 'Unknown',
      lastPrice: index.last ?? 0.0,
      change: index.variation ?? 0.0,
      pChange: index.percentChange ?? 0.0,
      open: index.open ?? 0.0,
      high: index.high ?? 0.0,
      low: index.low ?? 0.0,
      prevClose: index.previousClose ?? 0.0,
      lastUpdateTime: DateTime.now(), // Index responses often lack per-item timestamp in this object
    );
  }

  /// Map SDK Index response to MarketIndex domain model.
  static MarketIndex mapIndexToDomain(NSEStockIndicesDataV1 sdkIndex) {
    final indexQuote = SecurityQuote(
      symbol: sdkIndex.name ?? 'Unknown',
      lastPrice: 0.0, // Top-level index LTP
      change: 0.0,
      pChange: 0.0,
      open: 0.0,
      high: 0.0,
      low: 0.0,
      prevClose: 0.0,
      lastUpdateTime: DateTime.tryParse(sdkIndex.timestamp ?? '') ?? DateTime.now(),
    );

    final constituents = (sdkIndex.data ?? [])
        .map((s) => mapStockDataToQuote(s))
        .toList();

    return MarketIndex(
      symbol: sdkIndex.name ?? 'Unknown',
      name: sdkIndex.name ?? 'Unknown',
      quote: indexQuote,
      constituents: constituents,
    );
  }

  /// Map Historical Data response to List<Candle>.
  static List<Candle> mapHistoricalDataToCandles(HistoricalDataResponseV1 response) {
    if (response.data == null || response.data!.isEmpty) return [];
    
    // Assuming the map key is the symbol, we take the first available data series
    final historicalData = response.data!.values.first;
    if (historicalData.dataPoints == null) return [];

    return historicalData.dataPoints!.map((p) => Candle(
      date: DateTime.parse(p.time!).toLocal(),
      open: p.open ?? 0.0,
      high: p.high ?? 0.0,
      low: p.low ?? 0.0,
      close: p.close ?? 0.0,
      volume: p.volume ?? 0,
    )).toList();
  }

  /// Map Security Search response to List<SecurityQuote>.
  /// Note: Search results might not have price data.
  static SecurityQuote mapSecurityToQuote(SecurityV1 security) {
    return SecurityQuote(
      symbol: security.symbol ?? 'Unknown',
      name: security.name,
      lastPrice: 0.0, // Search results usually don't have price
      change: 0.0,
      pChange: 0.0,
      open: 0.0,
      high: 0.0,
      low: 0.0,
      prevClose: 0.0,
      lastUpdateTime: DateTime.now(),
    );
  }

  /// Map Market Mover to SecurityQuote.
  static SecurityQuote mapMarketMoverToQuote(MarketMoverV1 mover) {
    return SecurityQuote(
      symbol: mover.symbol ?? 'Unknown',
      lastPrice: mover.lastPrice ?? 0.0,
      change: mover.change ?? 0.0,
      pChange: mover.pChange ?? 0.0,
      open: 0.0, // Not provided in Mover DTO
      high: 0.0,
      low: 0.0,
      prevClose: (mover.lastPrice ?? 0.0) - (mover.change ?? 0.0),
      lastUpdateTime: DateTime.now(),
    );
  }
}
