import 'dart:convert';
import 'package:http/http.dart' as http;

class MarketDataClient {
  final String baseUrl;
  final http.Client _client;

  MarketDataClient({
    required this.baseUrl,
    http.Client? client,
  }) : _client = client ?? http.Client();

  /// Get quotes for symbols
  Future<Map<String, dynamic>> getQuotes({
    required String symbols,
    String timeFrame = '1D',
  }) async {
    final uri = Uri.parse('$baseUrl/api/v1/market-data/quotes').replace(
      queryParameters: {
        'symbols': symbols,
        'timeFrame': timeFrame,
      },
    );

    final response = await _client.get(uri);

    if (response.statusCode == 200) {
      return json.decode(response.body) as Map<String, dynamic>;
    } else {
      throw Exception('Failed to load quotes: ${response.statusCode}');
    }
  }
}
