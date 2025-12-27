import '../models/candle.dart';
import '../models/security_quote.dart';
import '../models/market_index.dart';
import '../models/security_search_request.dart';
import '../models/sector_performance.dart';

/// Contract for market data operations.
/// UI components should depend on this interface, not concrete implementations.
abstract class MarketDataRepository {
  /// Fetch real-time quotes for a list of symbols via REST.
  Future<List<SecurityQuote>> getQuotes(List<String> symbols);

  /// Fetch full details for a market index, including constituents.
  Future<MarketIndex> getIndexDetails(String symbol);

  /// Subscribe to real-time updates for a list of symbols.
  Stream<SecurityQuote> streamQuotes(List<String> symbols);
  
  /// Fetch the list of available indices by category.
  Future<Map<IndexCategory, List<String>>> getAvailableIndices();

  /// Fetch historical data for charting.
  Future<List<Candle>> getHistoricalData(String symbol, String range);

  /// Search for securities by query string.
  Future<List<SecurityQuote>> searchSecurities(String query);

  /// Advanced search for securities.
  Future<List<SecurityQuote>> searchSecuritiesAdvanced(SecuritySearchRequest request);

  /// Fetch top market movers (gainers/losers).
  Future<List<SecurityQuote>> getMarketMovers(String type);

  /// Fetch sector performance data.
  Future<List<SectorPerformance>> getSectorPerformance(String? indexSymbol);

  /// Get login URL for a provider.
  Future<String?> getLoginUrl(String provider);

  /// Connect to the market data stream.
  Future<bool> connectStream(List<String> symbols, String provider, {bool isIndexSymbol = false});

  /// Disconnect from the market data stream.
  Future<bool> disconnectStream(String provider);
}
