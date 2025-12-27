class SectorPerformance {
  final String sector;
  final double change;
  final double pChange;
  final int stockCount;

  const SectorPerformance({
    required this.sector,
    required this.change,
    required this.pChange,
    this.stockCount = 0,
  });
}
