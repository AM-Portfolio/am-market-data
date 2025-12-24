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

  // Fetch all available indices (Broad + Sectoral) flattened
  Future<List<String>> fetchAllIndices() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/api/v1/indices/available'));
      if (response.statusCode == 200) {
        final Map<String, dynamic> data = json.decode(response.body);
        List<String> all = [];
        if (data['broad'] != null) all.addAll(List<String>.from(data['broad']));
        if (data['sector'] != null) all.addAll(List<String>.from(data['sector']));
        return all; 
      } else {
        throw Exception('Failed to load indices');
      }
    } catch (e) {
      throw Exception('Error fetching indices: $e');
    }
  }

  Future<List<Map<String, dynamic>>> fetchHistory(String symbol, String range) async {
      try {
        final response = await http.get(Uri.parse('$baseUrl/api/v1/market-data/historical-charts/$symbol?range=$range'));

        if (response.statusCode == 200) {
           final Map<String, dynamic> jsonResponse = json.decode(response.body);
             if (jsonResponse.containsKey('data')) {
                 return List<Map<String, dynamic>>.from(jsonResponse['data']);
             } else {
                 return [];
             }
        } else {
          throw Exception('Failed to load history');
        }
      } catch (e) {
        throw Exception('Error fetching history: $e');
      }
  }

  Future<bool> refreshCookies() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/api/scraper/cookies'));
      return response.statusCode == 200;
    } catch (e) {
      print('Error refreshing cookies: $e');
      return false;
    }
  }

  // --- Streamer & Auth Methods ---

  Future<String?> getLoginUrl(String provider) async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/api/v1/market-data/auth/login-url?provider=$provider'));
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        return data['loginUrl'] ?? data['url'] ?? data['authUrl'];
      }
    } catch (e) {
      print('Error fetching login URL: $e');
    }
    return null;
  }

  Future<bool> connectStream(List<String> symbols, String provider) async {
    try {
      final payload = {
        'instrumentKeys': symbols,
        'mode': 'FULL',
        'provider': provider
      };
      
      final response = await http.post(
        Uri.parse('$baseUrl/api/v1/market-data/stream/connect'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode(payload),
      );
      
      return response.statusCode == 200;
    } catch (e) {
      print('Error connecting stream: $e');
      return false;
    }
  }

    Future<bool> disconnectStream(String provider) async {
    try {
      final response = await http.post(
          Uri.parse('$baseUrl/api/v1/market-data/stream/disconnect?provider=$provider')
      );
      return response.statusCode == 200;
    } catch (e) {
      print('Error disconnecting stream: $e');
      return false;
    }
  }

  Future<List<Map<String, dynamic>>> searchInstruments(String query, String provider) async {
    if (query.isEmpty) return [];
    
    try {
      final payload = {
        'queries': [query],
        'provider': provider
      };

      final response = await http.post(
        Uri.parse('$baseUrl/api/instruments/search'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode(payload)
      );

      if (response.statusCode == 200) {
        return List<Map<String, dynamic>>.from(json.decode(response.body));
      }
    } catch (e) {
      print('Error searching instruments: $e');
    }
    return [];
  }
}
