import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/market_data.dart';

class ApiService {
  // Assuming the Flutter app runs on the same host or proxied. 
  // Should probably use relative paths if served from same origin, or configure base URL.
  // For dev, if using 'flutter run -d chrome', we might need a proxy or full URL.
  // Assuming localhost:8080 for development if not specified.
  static const String baseUrl = 'http://localhost:8092'; 

  Future<AvailableIndices> fetchAvailableIndices() async {
    final response = await http.get(Uri.parse('$baseUrl/api/v1/indices/available'));
    if (response.statusCode == 200) {
      return AvailableIndices.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to load indices');
    }
  }

  Future<StockIndicesMarketData> fetchIndexData(String indexSymbol, {bool forceRefresh = false}) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/v1/indices/batch?forceRefresh=$forceRefresh'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode([indexSymbol]),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      if (data.isNotEmpty) {
        return StockIndicesMarketData.fromJson(data[0]);
      } else {
        throw Exception('No data found for index');
      }
    } else {
      throw Exception('Failed to load index data');
    }
  }
}
