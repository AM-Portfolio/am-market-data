import 'package:flutter_test/flutter_test.dart';
import 'package:market_data_client/api.dart';
import 'package:market_data_web/data/mappers/sdk_mapper.dart';
import 'package:market_data_client/api.dart';

void main() {
  group('SdkMapper Tests', () {
    test('mapUpdateToQuote should correctly map all fields', () {
      final update = MarketDataUpdateV1(
        instrumentKey: 'RELIANCE',
        lastPrice: 2500.0,
        change: 10.0,
        pChange: 0.4,
        open: 2495.0,
        high: 2510.0,
        low: 2480.0,
        previousClose: 2490.0,
        timestamp: '2025-12-28T10:00:00Z',
      );

      final quote = SdkMapper.mapUpdateToQuote(update);

      expect(quote.symbol, 'RELIANCE');
      expect(quote.lastPrice, 2500.0);
      expect(quote.change, 10.0);
      expect(quote.pChange, 0.4);
      expect(quote.open, 2495.0);
      expect(quote.high, 2510.0);
      expect(quote.low, 2480.0);
      expect(quote.prevClose, 2490.0);
    });

    test('mapStockDataToQuote should handle nulls gracefully', () {
      final stock = StockData(
        symbol: 'TCS',
        lastPrice: null,
        change: null,
      );

      final quote = SdkMapper.mapStockDataToQuote(stock);

      expect(quote.symbol, 'TCS');
      expect(quote.lastPrice, 0.0);
      expect(quote.change, 0.0);
      expect(quote.prevClose, 0.0);
    });

    test('mapNSEIndexToQuote should use indexSymbol or index', () {
      final index = NSEIndexV1(
        index: 'NIFTY 50',
        last: 24000.0,
      );

      final quote = SdkMapper.mapNSEIndexToQuote(index);

      expect(quote.symbol, 'NIFTY 50');
      expect(quote.lastPrice, 24000.0);
    });
  });
}
