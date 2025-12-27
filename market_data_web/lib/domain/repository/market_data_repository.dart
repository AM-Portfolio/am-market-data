import '../models/security_quote.dart';
import '../models/market_index.dart';

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
}
