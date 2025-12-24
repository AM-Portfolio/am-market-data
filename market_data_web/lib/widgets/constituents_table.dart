import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';

class ConstituentsTable extends StatelessWidget {
  const ConstituentsTable({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MarketProvider>();
    final data = provider.currentIndexData;

    if (data == null || data.stocks.isEmpty) {
      return const Center(child: Text('No data available'));
    }

    return SingleChildScrollView(
      scrollDirection: Axis.vertical,
      child: SizedBox(
        width: double.infinity,
        child: DataTable(
          columns: const [
            DataColumn(label: Text('Symbol')),
            DataColumn(label: Text('Price')),
            DataColumn(label: Text('Change %')),
            DataColumn(label: Text('Open')),
            DataColumn(label: Text('High')),
            DataColumn(label: Text('Low')),
          ],
          rows: data.stocks.map((stock) {
            final isPositive = stock.pChange >= 0;
            return DataRow(
              cells: [
                DataCell(Text(stock.symbol, style: const TextStyle(fontWeight: FontWeight.bold))),
                DataCell(Text(stock.lastPrice.toStringAsFixed(2))),
                DataCell(Text(
                  '${isPositive ? '+' : ''}${stock.pChange.toStringAsFixed(2)}%',
                  style: TextStyle(color: isPositive ? Colors.green : Colors.red),
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
}
