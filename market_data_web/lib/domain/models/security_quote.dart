/// Domain model representing a security's real-time quote.
/// This is the stable entity used by the UI layer.
class SecurityQuote {
  final String symbol;
  final String? name;
  final double lastPrice;
  final double change;
  final double pChange;
  final double open;
  final double high;
  final double low;
  final double prevClose;
  final DateTime lastUpdateTime;

  const SecurityQuote({
    required this.symbol,
    this.name,
    required this.lastPrice,
    required this.change,
    required this.pChange,
    required this.open,
    required this.high,
    required this.low,
    required this.prevClose,
    required this.lastUpdateTime,
  });

  // For UI display helpers
  bool get isPositive => change >= 0;
  String get changeText => "${isPositive ? '+' : ''}${change.toStringAsFixed(2)}";
  String get pChangeText => "${isPositive ? '+' : ''}${pChange.toStringAsFixed(2)}%";
}
