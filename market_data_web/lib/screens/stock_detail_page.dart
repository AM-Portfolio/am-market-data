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
                            child: LineChart(
                              LineChartData(
                                gridData: FlGridData(
                                  show: true,
                                  drawVerticalLine: false,
                                  horizontalInterval: _calculateInterval(),
                                  getDrawingHorizontalLine: (value) {
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
                                      interval: _chartData.length / 5, // Show ~5 labels
                                      getTitlesWidget: (value, meta) {
                                        final index = value.toInt();
                                        if (index >= 0 && index < _chartData.length) {
                                           final dateStr = _chartData[index]['time'] as String;
                                           // Simplistic parsing, adjust based on actual API format
                                            try {
                                              // expected format: yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss
                                                final date = DateTime.parse(dateStr);
                                                if (_selectedRange == '1D') {
                                                  return Text(DateFormat('HH:mm').format(date), style: const TextStyle(color: Colors.grey, fontSize: 10));
                                                } else {
                                                  return Text(DateFormat('MMM yy').format(date), style: const TextStyle(color: Colors.grey, fontSize: 10));
                                                }
                                            } catch (e) {
                                                return Text('');
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
                              ),
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
