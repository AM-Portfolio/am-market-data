import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';

class StockChart extends StatelessWidget {
  final List<Map<String, dynamic>> chartData;
  final bool isLoading;
  final String? error;

  const StockChart({
    super.key,
    required this.chartData,
    this.isLoading = false,
    this.error,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (error != null) {
      return Center(child: Text('Error: $error', style: const TextStyle(color: Colors.red)));
    }
    if (chartData.isEmpty) {
      return const Center(child: Text('No data available', style: TextStyle(color: Colors.white)));
    }

    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: LayoutBuilder(
        builder: (context, constraints) {
          // Calculate width based on data points to allow scrolling
          // approx 20px per point or min screen width
          double chartWidth = chartData.length * 20.0;
          if (chartWidth < constraints.maxWidth) {
            chartWidth = constraints.maxWidth;
          }

          return SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: SizedBox(
              width: chartWidth,
              height: constraints.maxHeight,
              child: LineChart(
                LineChartData(
                  gridData: FlGridData(
                    show: true,
                    drawVerticalLine: true,
                    horizontalInterval: _calculateInterval(),
                    getDrawingHorizontalLine: (value) {
                      return FlLine(
                        color: Colors.white.withOpacity(0.1),
                        strokeWidth: 1,
                      );
                    },
                    getDrawingVerticalLine: (value) {
                      return FlLine(
                        color: Colors.white.withOpacity(0.1),
                        strokeWidth: 1,
                      );
                    },
                  ),
                  titlesData: FlTitlesData(
                    show: true,
                    rightTitles: AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    topTitles: AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    bottomTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 30,
                        interval: (chartData.length / 10).ceilToDouble(), // Dynamic interval
                        getTitlesWidget: (value, meta) {
                          final index = value.toInt();
                          if (index >= 0 && index < chartData.length) {
                             final dateStr = chartData[index]['time'] as String;
                              try {
                                  final date = DateTime.parse(dateStr);
                                  final formatter = DateFormat('dd MMM yy');
                                  return Padding(
                                    padding: const EdgeInsets.only(top: 8.0),
                                    child: Text(formatter.format(date), style: const TextStyle(color: Colors.grey, fontSize: 10)),
                                  );
                              } catch (e) {
                                  return const Text('');
                              }
                          }
                          return const Text('');
                        },
                      ),
                    ),
                    leftTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 42,
                        getTitlesWidget: (value, meta) {
                           return Text(value.toStringAsFixed(0), style: const TextStyle(color: Colors.grey, fontSize: 10));
                        },
                      ),
                    ),
                  ),
                  borderData: FlBorderData(
                    show: true,
                    border: Border.all(color: const Color(0xFF37434d), width: 1),
                  ),
                  minX: 0,
                  maxX: chartData.length.toDouble() - 1,
                  minY: _getMinPrice(),
                  maxY: _getMaxPrice(),
                  lineBarsData: [
                    LineChartBarData(
                      spots: _getSpots(),
                      isCurved: true,
                      color: Colors.blueAccent,
                      barWidth: 2,
                      isStrokeCapRound: true,
                      dotData: FlDotData(show: false),
                      belowBarData: BarAreaData(
                        show: true,
                        color: Colors.blueAccent.withOpacity(0.1),
                      ),
                    ),
                  ],
                  lineTouchData: LineTouchData(
                      touchTooltipData: LineTouchTooltipData(
                          // tooltipBgColor: Colors.blueGrey.withOpacity(0.8),
                          getTooltipItems: (List<LineBarSpot> touchedBarSpots) {
                            return touchedBarSpots.map((barSpot) {
                              final flSpot = barSpot;
                              final index = flSpot.x.toInt();
                              if (index >= 0 && index < chartData.length) {
                                  final dateStr = chartData[index]['time'] as String;
                                  final date = DateTime.parse(dateStr);
                                  final formattedDate = DateFormat('yyyy-MM-dd HH:mm').format(date);
                                  return LineTooltipItem(
                                      '$formattedDate \n ${flSpot.y.toStringAsFixed(2)}',
                                      const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                                  );
                              }
                              return null;
                            }).toList();
                          }
                      )
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  double _calculateInterval() {
    if (chartData.isEmpty) return 1.0;
    double min = _getMinPrice();
    double max = _getMaxPrice();
    return (max - min) / 5;
  }

  double _getMinPrice() {
    if (chartData.isEmpty) return 0;
    return chartData.map((e) => (e['close'] as num).toDouble()).reduce((a, b) => a < b ? a : b) * 0.99;
  }

  double _getMaxPrice() {
     if (chartData.isEmpty) return 100;
     return chartData.map((e) => (e['close'] as num).toDouble()).reduce((a, b) => a > b ? a : b) * 1.01;
  }

  List<FlSpot> _getSpots() {
    List<FlSpot> spots = [];
    for (int i = 0; i < chartData.length; i++) {
        spots.add(FlSpot(i.toDouble(), (chartData[i]['close'] as num).toDouble()));
    }
    return spots;
  }
}
