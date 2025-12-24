import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import 'package:intl/intl.dart';

class HeatmapView extends StatefulWidget {
  const HeatmapView({super.key});

  @override
  State<HeatmapView> createState() => _HeatmapViewState();
}

class _HeatmapViewState extends State<HeatmapView> {
  String _timeFrame = '1D'; // Default to Day
  String? _percentFilter; // 'Above +5%', etc.

  final List<String> _timeFrames = ['5M', '10M', '15M', '30M', '1H', '1D'];
  final List<String> _filters = ['Above +5%', '+2 to +5%', '0 to +2%', '0 to -2%', '-2 to -5%', 'Below -5%'];

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MarketProvider>();
    final data = provider.currentIndexData;

    if (data == null || data.stocks.isEmpty) {
      return const Center(child: Text('No data available'));
    }

    // Filter Logic
    List stocks = data.stocks;
    if (_percentFilter != null) {
      stocks = stocks.where((s) {
        final p = s.pChange;
        switch (_percentFilter) {
          case 'Above +5%': return p > 5;
          case '+2 to +5%': return p > 2 && p <= 5;
          case '0 to +2%': return p >= 0 && p <= 2;
          case '0 to -2%': return p < 0 && p >= -2;
          case '-2 to -5%': return p < -2 && p >= -5;
          case 'Below -5%': return p < -5;
          default: return true;
        }
      }).toList();
    }

    // Sort by pChange descending
    stocks.sort((a, b) => b.pChange.compareTo(a.pChange));

    return Column(
      children: [
        // Top Filter Bar
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          width: double.infinity, // Ensures full width
          color: const Color(0xFF2C2C3E),
          child: Wrap( // Use Wrap to handle smaller widths gracefully
            spacing: 20,
            runSpacing: 10,
            crossAxisAlignment: WrapCrossAlignment.center,
            alignment: WrapAlignment.spaceBetween,
            children: [
              // Time Frame Dropdown
              DropdownButton<String>(
                value: _timeFrame,
                dropdownColor: const Color(0xFF2C2C3E),
                style: const TextStyle(color: Colors.white),
                underline: Container(height: 1, color: Colors.blue),
                items: _timeFrames.map((tf) => DropdownMenuItem(value: tf, child: Text(tf))).toList(),
                onChanged: (val) {
                  setState(() => _timeFrame = val!);
                  // TODO: Fetch data for specific timeframe involved
                },
              ),
              
              // Percent Filters
              Wrap(
                spacing: 8,
                children: _filters.map((f) {
                  final isSelected = _percentFilter == f;
                  Color color;
                    if (f.contains('Above')) color = Colors.green[700]!;
                    else if (f.contains('+2')) color = Colors.green[500]!;
                    else if (f.contains('0 to +2')) color = Colors.green[300]!;
                    else if (f.contains('0 to -2')) color = Colors.red[300]!;
                    else if (f.contains('-2 to')) color = Colors.red[500]!;
                    else color = Colors.red[900]!;

                  return FilterChip(
                    label: Text(f, style: const TextStyle(fontSize: 12)),
                    selected: isSelected,
                    onSelected: (selected) {
                      setState(() => _percentFilter = selected ? f : null);
                    },
                    backgroundColor: Colors.black12,
                    selectedColor: color,
                    checkmarkColor: Colors.white,
                    labelStyle: TextStyle(color: isSelected ? Colors.white : Colors.grey),
                    side: BorderSide.none,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                  );
                }).toList(),
              ),
            ],
          ),
        ),

        // Grid
        Expanded(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: LayoutBuilder(
              builder: (context, constraints) {
                // Determine column count based on width
                int crossAxisCount = (constraints.maxWidth / 200).floor();
                if (crossAxisCount < 2) crossAxisCount = 2; // Min columns

                return GridView.builder(
                  gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: crossAxisCount,
                    childAspectRatio: 2.2, // Rectangular boxes
                    crossAxisSpacing: 10,
                    mainAxisSpacing: 10,
                  ),
                  itemCount: stocks.length,
                  itemBuilder: (context, index) {
                    final stock = stocks[index];
                    final isPositive = stock.pChange >= 0;
                    final intensity = (stock.pChange.abs() / 3).clamp(0.2, 1.0); // Simple intensity scaling
                    final baseColor = isPositive ? Colors.green : Colors.red;
                    final color = baseColor.withOpacity(intensity);

                    return Container(
                      decoration: BoxDecoration(
                        color: color,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      padding: const EdgeInsets.all(12),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              // Use flexible to avoid overflow
                              Flexible(
                                child: Text(
                                  stock.symbol,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.bold,
                                    fontSize: 14,
                                    color: Colors.white
                                  ),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              Text(
                                '${isPositive ? '+' : ''}${stock.pChange.toStringAsFixed(2)}%',
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 14,
                                  color: Colors.white
                                ),
                              ),
                            ],
                          ),
                          Row(
                            children: [
                                Text(
                                  NumberFormat.currency(symbol: '₹', locale: 'en_IN').format(stock.lastPrice),
                                  style: const TextStyle(fontSize: 12, color: Colors.white70),
                                ),
                            ],
                          )
                        ],
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ),
      ],
    );
  }
}
