import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../models/market_data.dart';

class IndicesPerformanceView extends StatelessWidget {
  const IndicesPerformanceView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Consumer<MarketProvider>(
      builder: (context, provider, child) {
        if (provider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }
        
        if (provider.error != null) {
          return Center(child: Text('Error: ${provider.error}', style: const TextStyle(color: Colors.red)));
        }

        if (provider.allIndicesData.isEmpty) {
          return const Center(child: Text('No data loaded. Select "All Indices" to load.', style: TextStyle(color: Colors.white)));
        }

        // Filter and sort data for chart
        // The model directly has properties, no metadata field
        final validData = List<StockIndicesMarketData>.from(provider.allIndicesData);
        validData.sort((a, b) => b.pChange.compareTo(a.pChange));

        return Column(
          children: [
             // Bar Chart Section
             Container(
               height: 300,
               padding: const EdgeInsets.all(16),
               child: BarChart(
                 BarChartData(
                   gridData: FlGridData(show: true, drawVerticalLine: false, horizontalInterval: 0.5),
                   titlesData: FlTitlesData(
                     show: true,
                     bottomTitles: AxisTitles(
                        sideTitles: SideTitles(
                          showTitles: true,
                          reservedSize: 60,
                          getTitlesWidget: (value, meta) {
                             int index = value.toInt();
                             if (index >= 0 && index < validData.length) {
                                // Show only few labels to avoid clutter
                                if (validData.length > 20 && index % 2 != 0) return const Text('');
                                return Padding(
                                  padding: const EdgeInsets.only(top: 8),
                                  child: Text(
                                    validData[index].indexSymbol,
                                    style: const TextStyle(color: Colors.white, fontSize: 10),
                                    textAlign: TextAlign.center,
                                  ),
                                );
                             }
                             return const Text('');
                          },
                        ),
                     ),
                     leftTitles: AxisTitles(
                       sideTitles: SideTitles(showTitles: true, reservedSize: 40, getTitlesWidget: (val, meta) => Text(val.toStringAsFixed(1), style: const TextStyle(color: Colors.grey, fontSize: 10))),
                     ),
                     topTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
                     rightTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
                   ),
                   borderData: FlBorderData(show: false),
                   barGroups: validData.asMap().entries.map((entry) {
                      double val = entry.value.pChange;
                      return BarChartGroupData(
                        x: entry.key,
                        barRods: [
                          BarChartRodData(
                            toY: val,
                            color: val >= 0 ? Colors.green : Colors.red,
                            width: 12,
                            borderRadius: BorderRadius.circular(2),
                          )
                        ],
                      );
                   }).toList(),
                 ),
               ),
             ),

             const Divider(color: Colors.grey),

             // Table Section
             Expanded(
               child: SingleChildScrollView(
                 child: DataTable(
                   headingRowColor: MaterialStateProperty.all(const Color(0xFF2E2E3E)),
                   columns: const [
                     DataColumn(label: Text('Index Name', style: TextStyle(color: Colors.grey))),
                     DataColumn(label: Text('Price', style: TextStyle(color: Colors.grey))),
                     DataColumn(label: Text('Change', style: TextStyle(color: Colors.grey))),
                     DataColumn(label: Text('% Change', style: TextStyle(color: Colors.grey))),
                   ],
                   rows: validData.map((data) {
                      double change = data.change;
                      double pChange = data.pChange;
                      Color color = change >= 0 ? Colors.green : Colors.red;

                      return DataRow(cells: [
                        DataCell(Text(data.indexSymbol, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
                        DataCell(Text(data.lastPrice.toStringAsFixed(2), style: const TextStyle(color: Colors.white))),
                        DataCell(Text(change.toStringAsFixed(2), style: TextStyle(color: color))),
                        DataCell(Text('${pChange.toStringAsFixed(2)}%', style: TextStyle(color: color))),
                      ]);
                   }).toList(),
                 ),
               ),
             ),
          ],
        );
      },
    );
  }
}
