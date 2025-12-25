import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/market_data.dart';
import '../utils/app_logger.dart';

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
      AppLogger.error("ApiService.fetchAllIndices", "Error fetching indices", e);
      throw Exception('Error fetching indices: $e');
    }
  }

  Future<List<Map<String, dynamic>>> fetchHistory(String symbol, String range) async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/api/v1/market-data/historical-charts/$symbol?range=$range'));

      if (response.statusCode == 200) {
        final dynamic jsonResponse = json.decode(response.body);
        
        // Helper to extract list from potential structures
        // Structure seems to be: { "data": { "SYMBOL": { "dataPoints": [...] } } }
        List<dynamic>? extractList(dynamic data) {
           if (data is List) return data;
           if (data is Map) {
               // Prioritize "dataPoints" if present (deepest level)
               if (data.containsKey('dataPoints')) {
                   return extractList(data['dataPoints']);
               }
               // Then check if keyed by symbol
               if (data.containsKey(symbol)) {
                   return extractList(data[symbol]);
               }
               // Then check for "data" wrapper
               if (data.containsKey('data')) {
                   return extractList(data['data']);
               }
           }
           return null;
        }

        final list = extractList(jsonResponse);
        if (list != null) {
            return List<Map<String, dynamic>>.from(list);
        } else {
            AppLogger.warning("ApiService.fetchHistory", "Failed to parse history structure. Response: $jsonResponse");
            return [];
        }
      } else {
        throw Exception('Failed to load history: ${response.statusCode}');
      }
    } catch (e) {
      AppLogger.error("ApiService.fetchHistory", "Error fetching history for $symbol", e);
      throw Exception('Error fetching history: $e');
    }
  }

  Future<bool> refreshCookies() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/api/scraper/cookies'));
      return response.statusCode == 200;
    } catch (e) {
      AppLogger.error("ApiService.refreshCookies", "Error refreshing cookies", e);
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
      AppLogger.error("ApiService.getLoginUrl", "Error fetching login URL", e);
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
      AppLogger.error("ApiService.connectStream", "Error connecting stream for $symbols", e);
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
      AppLogger.error("ApiService.disconnectStream", "Error disconnecting stream", e);
      return false;
    }
  }

  Future<List<Map<String, dynamic>>> searchInstruments(String query, String provider) async {
    return advancedSearchInstruments({'queries': [query], 'provider': provider});
  }

  Future<List<Map<String, dynamic>>> advancedSearchInstruments(Map<String, dynamic> criteria) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/instruments/search'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode(criteria)
      );

      if (response.statusCode == 200) {
        return List<Map<String, dynamic>>.from(json.decode(response.body));
      }
    } catch (e) {
      AppLogger.error("ApiService.advancedSearchInstruments", "Error searching instruments", e);
    }
    return [];
  }

  // --- Market Analytics Methods ---

  Future<List<Map<String, dynamic>>> fetchMovers({
    String type = 'gainers', 
    int limit = 10, 
    String? indexSymbol
  }) async {
    try {
      String url = '$baseUrl/api/v1/market-analytics/movers?type=$type&limit=$limit';
      if (indexSymbol != null && indexSymbol.isNotEmpty) {
        url += '&indexSymbol=$indexSymbol';
      }
      
      final response = await http.get(Uri.parse(url));
      
      if (response.statusCode == 200) {
        return List<Map<String, dynamic>>.from(json.decode(response.body));
      } else {
        throw Exception('Failed to fetch movers: ${response.statusCode}');
      }
    } catch (e) {
      AppLogger.error("ApiService.fetchMovers", "Error fetching $type", e);
      return [];
    }
  }

  Future<List<Map<String, dynamic>>> fetchSectorPerformance({String? indexSymbol}) async {
    try {
      String url = '$baseUrl/api/v1/market-analytics/sectors';
      if (indexSymbol != null && indexSymbol.isNotEmpty) {
        url += '?indexSymbol=$indexSymbol';
      }
      
      final response = await http.get(Uri.parse(url));
      
      if (response.statusCode == 200) {
        return List<Map<String, dynamic>>.from(json.decode(response.body));
      } else {
        throw Exception('Failed to fetch sector performance: ${response.statusCode}');
      }
    } catch (e) {
      AppLogger.error("ApiService.fetchSectorPerformance", "Error fetching sectors", e);
      return [];
    }
  }

  Future<Map<String, dynamic>> fetchMarketCapAnalysis() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/api/v1/market-analytics/market-cap'));
      
      if (response.statusCode == 200) {
        return Map<String, dynamic>.from(json.decode(response.body));
      } else {
        throw Exception('Failed to fetch market cap analysis: ${response.statusCode}');
      }
    } catch (e) {
      AppLogger.error("ApiService.fetchMarketCapAnalysis", "Error fetching market cap analysis", e);
      return {};
    }
  }



  Future<Map<String, dynamic>> fetchLivePrices(List<String> symbols, [bool indexSymbol = true]) async {
    try {
      final query = symbols.join(',');
      final response = await http.get(
        Uri.parse('$baseUrl/api/v1/market-data/live-prices?symbols=$query&isIndexSymbol=$indexSymbol')
      );
      
      if (response.statusCode == 200) {
        return Map<String, dynamic>.from(json.decode(response.body));
      } else {
        throw Exception('Failed to fetch live prices: ${response.statusCode}');
      }
    } catch (e) {
      AppLogger.error("ApiService.fetchLivePrices", "Error fetching live prices", e);
      return {};
    }
  }

  Future<Map<String, dynamic>> fetchHistoricalData({
    required List<String> symbols,
    required String from,
    required String to,
    required String interval,
    bool forceRefresh = false,
    bool isIndexSymbol = false,
    String instrumentType = 'STOCK',
    bool continuous = false,
  }) async {
    try {
      final requestBody = {
        'symbols': symbols.join(','),
        'from': from,
        'to': to,
        'interval': interval,
        'forceRefresh': forceRefresh,
        'isIndexSymbol': isIndexSymbol,
        'instrumentType': instrumentType,
        'continuous': continuous,
      };

      final response = await http.post(
        Uri.parse('$baseUrl/api/v1/market-data/historical-data'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode(requestBody),
      );

      if (response.statusCode == 200) {
        return Map<String, dynamic>.from(json.decode(response.body));
      } else {
        AppLogger.error("ApiService.fetchHistoricalData", 
          "Failed with ${response.statusCode}: ${response.body}");
        throw Exception('Failed to fetch historical data: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      AppLogger.error("ApiService.fetchHistoricalData", "Error fetching historical data", e);
      rethrow;
    }
  }

  // Legacy GET search
  Future<List<dynamic>> searchSecurities(String query) async {
    return searchSecuritiesAdvanced({'query': query});
  }

  // New POST search with request object
  Future<List<dynamic>> searchSecuritiesAdvanced(Map<String, dynamic> request) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/securities/search'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode(request)
      );

      if (response.statusCode == 200) {
        return json.decode(response.body);
      } else {
        throw Exception('Failed to search securities');
      }
    } catch (e) {
      AppLogger.error("ApiService.searchSecuritiesAdvanced", "Error searching securities", e);
      return [];
    }
  }
}
