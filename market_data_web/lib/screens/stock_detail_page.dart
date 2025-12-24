import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import '../services/api_service.dart';

class StockDetailPage extends StatefulWidget {
  final String symbol;

  const StockDetailPage({Key? key, required this.symbol}) : super(key: key);

  @override
  _StockDetailPageState createState() => _StockDetailPageState();
}

class _StockDetailPageState extends State<StockDetailPage> {
  final ApiService _apiService = ApiService();
  List<Map<String, dynamic>> _chartData = [];
  bool _isLoading = true;
  String? _error;
  String _selectedRange = '1D'; // '1D' or '5Y'

  @override
  void initState() {
    super.initState();
    _fetchData();
  }

  Future<void> _fetchData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final data = await _apiService.fetchHistory(widget.symbol, _selectedRange);
      // Sort data by time ascending (oldest to newest)
      data.sort((a, b) {
        final dateA = DateTime.tryParse(a['time'].toString()) ?? DateTime.now();
        final dateB = DateTime.tryParse(b['time'].toString()) ?? DateTime.now();
        return dateA.compareTo(dateB);
      });

      setState(() {
        _chartData = data;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1E1E2E), // Dark Theme Background
      appBar: AppBar(
        title: Text(widget.symbol),
        backgroundColor: const Color(0xFF2E2E3E),
      ),
      body: Column(
        children: [
          // Range Selector
          Container(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                _buildRangeButton('1D'),
                const SizedBox(width: 8),
                _buildRangeButton('5Y'),
              ],
            ),
          ),
          
          // Chart Area
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : _error != null
                    ? Center(child: Text('Error: $_error', style: const TextStyle(color: Colors.red)))
                    : _chartData.isEmpty
                        ? const Center(child: Text('No data available', style: TextStyle(color: Colors.white)))
                        : Padding(
                            padding: const EdgeInsets.all(16.0),
                            child: LayoutBuilder(
                              builder: (context, constraints) {
                                // Calculate width based on data points to allow scrolling
                                // approx 20px per point or min screen width
                                double chartWidth = _chartData.length * 20.0;
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
                                              interval: (_chartData.length / 10).ceilToDouble(), // Dynamic interval
                                              getTitlesWidget: (value, meta) {
                                                final index = value.toInt();
                                                if (index >= 0 && index < _chartData.length) {
                                                   final dateStr = _chartData[index]['time'] as String;
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
                                        maxX: _chartData.length.toDouble() - 1,
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
                                                    if (index >= 0 && index < _chartData.length) {
                                                        final dateStr = _chartData[index]['time'] as String;
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
                          ),
          ),
        ],
      ),
    );
  }
  
  double _calculateInterval() {
        if (_chartData.isEmpty) return 1.0;
        double min = _getMinPrice();
        double max = _getMaxPrice();
        return (max - min) / 5;
  }

  double _getMinPrice() {
    if (_chartData.isEmpty) return 0;
    return _chartData.map((e) => (e['close'] as num).toDouble()).reduce((a, b) => a < b ? a : b) * 0.99;
  }

  double _getMaxPrice() {
     if (_chartData.isEmpty) return 100;
     return _chartData.map((e) => (e['close'] as num).toDouble()).reduce((a, b) => a > b ? a : b) * 1.01;
  }

  List<FlSpot> _getSpots() {
    List<FlSpot> spots = [];
    for (int i = 0; i < _chartData.length; i++) {
        spots.add(FlSpot(i.toDouble(), (_chartData[i]['close'] as num).toDouble()));
    }
    return spots;
  }

  Widget _buildRangeButton(String range) {
    bool isSelected = _selectedRange == range;
    return ElevatedButton(
      style: ElevatedButton.styleFrom(
        backgroundColor: isSelected ? Colors.blueAccent : const Color(0xFF2E2E3E),
        foregroundColor: Colors.white,
      ),
      onPressed: () {
        if (!isSelected) {
            setState(() {
                _selectedRange = range;
            });
            _fetchData();
        }
      },
      child: Text(range),
    );
  }
}
