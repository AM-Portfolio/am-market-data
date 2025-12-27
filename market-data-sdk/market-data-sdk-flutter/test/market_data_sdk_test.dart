import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:http/http.dart' as http;
import 'package:market_data_sdk/market_data_sdk.dart';

import 'market_data_sdk_test.mocks.dart';

@GenerateMocks([http.Client])
void main() {
  group('MarketDataClient', () {
    test('getQuotes returns map when http call is successful', () async {
      final client = MockClient();
      final marketDataClient = MarketDataClient(baseUrl: 'http://localhost:8080', client: client);

      when(client.get(Uri.parse('http://localhost:8080/api/v1/market-data/quotes?symbols=NSE%3ARELIANCE&timeFrame=1D')))
          .thenAnswer((_) async => http.Response('{"NSE:RELIANCE": {"lastPrice": 2500.0}}', 200));

      expect(await marketDataClient.getQuotes(symbols: 'NSE:RELIANCE'), isA<Map<String, dynamic>>());
    });
  });
}
