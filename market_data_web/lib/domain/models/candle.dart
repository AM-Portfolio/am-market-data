/// Domain model representing a single point in an OHLC (Open-High-Low-Close) chart.
class Candle {
  final DateTime date;
  final double open;
  final double high;
  final double low;
  final double close;
  final int volume;

  const Candle({
    required this.date,
    required this.open,
    required this.high,
    required this.low,
    required this.close,
    required this.volume,
  });

  bool get isBullish => close >= open;
  bool get isBearish => close < open;
}
