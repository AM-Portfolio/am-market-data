import 'security_quote.dart';

/// Domain model representing a market index and its components.
class MarketIndex {
  final String symbol;
  final String name;
  final SecurityQuote quote;
  final List<SecurityQuote> constituents;

  const MarketIndex({
    required this.symbol,
    required this.name,
    required this.quote,
    this.category,
    this.constituents = const [],
  });

  final IndexCategory? category;
}

/// Categories for organizing indices.
enum IndexCategory { build, sectoral, thematic, strategy }
