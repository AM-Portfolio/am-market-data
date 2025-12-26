import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../screens/stock_detail_page.dart';

class ConstituentsTable extends StatelessWidget {
  const ConstituentsTable({super.key});

  @override
  Widget build(BuildContext context) {
    // Watch for index data changes (e.g. user selects new index)
    // livePrice updates will NOT trigger this anymore.
    final provider = context.watch<MarketProvider>();
    final data = provider.currentIndexData;

    if (data == null || data.stocks.isEmpty) {
      return const Center(child: Text('No data available'));
    }

    return StreamBuilder<Map<String, dynamic>>(
      stream: provider.livePriceStream,
      builder: (context, snapshot) {
        // Rebuild table when live data arrives (or just render using current cache)
        return SingleChildScrollView(
          scrollDirection: Axis.vertical,
          child: SizedBox(
            width: double.infinity,
            child: DataTable(
              showCheckboxColumn: false, // Hide checkboxes
              columns: const [
                DataColumn(label: Text('Symbol')),
                DataColumn(label: Text('Price')),
                DataColumn(label: Text('Change %')),
                DataColumn(label: Text('Open')),
                DataColumn(label: Text('High')),
                DataColumn(label: Text('Low')),
              ],
              rows: data.stocks.map((stock) {
                // Check for live updates
                final liveData = provider.livePrices[stock.symbol];
                double price = stock.lastPrice;
                double pChange = stock.pChange;

                if (liveData != null) {
                   price = (liveData['lastPrice'] as num).toDouble();
                   if (liveData['changePercent'] != null) {
                      pChange = (liveData['changePercent'] as num).toDouble();
                   }
                }

                final isPositive = pChange >= 0;
                
                return DataRow(
                  onSelectChanged: (selected) {
                    if (selected == true) {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => StockDetailPage(symbol: stock.symbol),
                        ),
                      );
                    }
                  },
                  cells: [
                    DataCell(Text(stock.symbol, style: const TextStyle(fontWeight: FontWeight.bold))),
                    DataCell(Text(price.toStringAsFixed(2), style: TextStyle(
                        fontWeight: liveData != null ? FontWeight.bold : FontWeight.normal,
                        color: liveData != null ? Colors.blueAccent : null // Highlight live
                    ))),
                    DataCell(Text(
                      '${isPositive ? '+' : ''}${pChange.toStringAsFixed(2)}%',
                      style: TextStyle(color: isPositive ? Colors.green : Colors.red, fontWeight: FontWeight.bold),
                    )),
                    DataCell(Text(stock.open.toStringAsFixed(2))),
                    DataCell(Text(stock.dayHigh.toStringAsFixed(2))),
                    DataCell(Text(stock.dayLow.toStringAsFixed(2))),
                  ],
                );
              }).toList(),
            ),
          ),
        );
      }
    );
  }
}

