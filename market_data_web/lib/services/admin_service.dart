import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/ingestion_log.dart';

class AdminService {
  // Use localhost for now, assume proxy or direct access
  static const String baseUrl = 'http://localhost:8092/api/admin';

  Future<List<IngestionLog>> getLogs({int page = 0, int size = 20, DateTime? startDate, DateTime? endDate}) async {
    String query = 'page=$page&size=$size';
    if (startDate != null) {
      query += '&startDate=${startDate.toIso8601String().split('T')[0]}';
    }
    if (endDate != null) {
      query += '&endDate=${endDate.toIso8601String().split('T')[0]}';
    }
    
    final response = await http.get(Uri.parse('$baseUrl/logs?$query'));
    
    if (response.statusCode == 200) {
      final List<dynamic> body = jsonDecode(response.body);
      return body.map((e) => IngestionLog.fromJson(e)).toList();
    } else {
      throw Exception('Failed to load logs');
    }
  }

  Future<IngestionLog> getJobDetails(String jobId) async {
    final response = await http.get(Uri.parse('$baseUrl/logs/$jobId'));
    if (response.statusCode == 200) {
      return IngestionLog.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to load job details');
    }
  }

  Future<void> triggerHistoricalSync({String? symbol, bool forceRefresh = true}) async {
    final uri = Uri.parse('$baseUrl/sync/historical').replace(
      queryParameters: {
        if (symbol != null && symbol.isNotEmpty) 'symbol': symbol,
        'forceRefresh': forceRefresh.toString()
      }
    );
    final response = await http.post(uri);
    if (response.statusCode != 200) {
      throw Exception('Failed to trigger sync: ${response.body}');
    }
  }

  Future<void> startIngestion(String provider, {List<String>? symbols}) async {
    String url = '$baseUrl/ingestion/start?provider=$provider';
    if (symbols != null && symbols.isNotEmpty) {
      url += '&symbols=${symbols.join(",")}';
    }
    final response = await http.post(Uri.parse(url));
    if (response.statusCode != 200) {
      throw Exception('Failed to start ingestion: ${response.body}');
    }
  }

  Future<void> stopIngestion(String provider) async {
    final response = await http.post(Uri.parse('$baseUrl/ingestion/stop?provider=$provider'));
    if (response.statusCode != 200) {
      throw Exception('Failed to stop ingestion: ${response.body}');
    }
  }
}
