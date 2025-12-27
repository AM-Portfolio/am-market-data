import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import '../widgets/stock_chart.dart';
import '../widgets/time_range_selector.dart';
import '../domain/models/candle.dart';
import '../domain/repository/market_data_repository.dart';
import '../domain/models/security_quote.dart';

import 'package:provider/provider.dart';
import '../providers/market_provider.dart';

class StockDetailPage extends StatefulWidget {
  final String symbol;

  const StockDetailPage({Key? key, required this.symbol}) : super(key: key);

  @override
  _StockDetailPageState createState() => _StockDetailPageState();
}

class _StockDetailPageState extends State<StockDetailPage> {
  List<Candle> _chartData = [];
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
      final repository = context.read<MarketDataRepository>();
      final data = await repository.getHistoricalData(widget.symbol, _selectedRange);
      
      // Sort data by time ascending (oldest to newest)
      data.sort((a, b) {
        return a.date.compareTo(b.date);
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
    // Listen to MarketProvider for live updates using Stream to avoid full rebuilds
    final marketProvider = Provider.of<MarketProvider>(context, listen: false);

    return Scaffold(
      backgroundColor: const Color(0xFF1E1E2E), // Dark Theme Background
      appBar: AppBar(
        title: StreamBuilder<SecurityQuote>(
          stream: marketProvider.quoteStream.where((quote) => quote.symbol == widget.symbol),
          builder: (context, snapshot) {
            final liveData = marketProvider.livePrices[widget.symbol];
            
            double? ltp;
            double? change;
            double? pChange;
            Color color = Colors.grey;

            if (liveData != null) {
               ltp = liveData.lastPrice;
               change = liveData.change;
               pChange = liveData.pChange;
               color = change >= 0 ? Colors.greenAccent : Colors.redAccent;
            } else if (snapshot.hasData) {
               final data = snapshot.data!;
               ltp = data.lastPrice;
               change = data.change;
               pChange = data.pChange;
               color = change >= 0 ? Colors.greenAccent : Colors.redAccent;
            }

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(widget.symbol),
                if (ltp != null)
                  Text(
                    "₹${ltp.toStringAsFixed(2)}  ${change! >= 0 ? '+' : ''}${change.toStringAsFixed(2)} (${pChange!.toStringAsFixed(2)}%)",
                     style: TextStyle(fontSize: 12, color: color),
                  ),
              ],
            );
          }
        ),
        backgroundColor: const Color(0xFF2E2E3E),
      ),
      body: Column(
        children: [
          // Range Selector
          TimeRangeSelector(
            selectedRange: _selectedRange,
            onRangeSelected: (range) {
              if (_selectedRange != range) {
                setState(() {
                  _selectedRange = range;
                });
                _fetchData();
              }
            },
            ranges: const ['10m', '15m', '30m', '1H', '4H', '1D', '1W', '1M', '5Y'],
          ),
          
          // Chart Area
          Expanded(
            child: StockChart(
              chartData: _chartData,
              isLoading: _isLoading,
              error: _error,
            ),
          ),
        ],
      ),
    );
  }
}
