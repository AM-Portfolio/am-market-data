import 'dart:async';
import 'dart:convert';
import 'package:market_data_client/api.dart';
// Accessing src directly as generated API doesn't export custom WebSocket client
import 'package:market_data_client/src/websocket/client.dart';
import '../../domain/models/security_quote.dart';
import '../../domain/models/market_index.dart';
import '../../domain/models/candle.dart';
import '../../domain/repository/market_data_repository.dart';
import '../mappers/sdk_mapper.dart';

/// Concrete implementation of MarketDataRepository using the generated SDK.
class MarketDataRepositoryImpl implements MarketDataRepository {
  final ApiClient _apiClient;
  final MarketDataPollingApi _pollingApi;
  final MarketIndicesApi _indicesApi;
  final AMMarketDataStreamingAPI _wsClient;
  
  // Broadcast controller to share stream across multiple subscribers
  final StreamController<SecurityQuote> _quoteStreamController = StreamController<SecurityQuote>.broadcast();
  bool _isWsConnected = false;

  MarketDataRepositoryImpl({
    required String baseUrl,
  })  : _apiClient = ApiClient(basePath: baseUrl),
        _pollingApi = MarketDataPollingApi(ApiClient(basePath: baseUrl)),
        _indicesApi = MarketIndicesApi(ApiClient(basePath: baseUrl)),
        // WS URL typically replaces http with ws and appends /ws/market-data-stream
        _wsClient = AMMarketDataStreamingAPI(
          url: baseUrl.replaceFirst('http', 'ws') + '/ws/market-data-stream'
        ) {
    _initializeWebSocket();
  }

  void _initializeWebSocket() {
    _wsClient.registerMessageHandler((message) {
      try {
        final jsonMap = jsonDecode(message);
        if (jsonMap is Map<String, dynamic>) {
           // Provide fallback for missing 'timestamp' or fields if needed
           final update = MarketDataUpdateV1.fromJson(jsonMap);
           if (update != null) {
             _quoteStreamController.add(SdkMapper.mapUpdateToQuote(update));
           }
        }
      } catch (e) {
        print('Error parsing WS message: $e');
      }
    });
  }

  @override
  Future<List<SecurityQuote>> getQuotes(List<String> symbols) async {
    try {
      final response = await _pollingApi.getQuotesV1(QuotesRequest(symbols: symbols));
      if (response == null) return [];
      
      final List<SecurityQuote> quotes = [];
      response.forEach((key, value) {
         // value is already MarketDataUpdateV1 in the generated map
         quotes.add(SdkMapper.mapUpdateToQuote(value));
      });
      return quotes;
    } catch (e) {
      print('Error fetching quotes: $e');
      return [];
    }
  }

  @override
  Future<MarketIndex> getIndexDetails(String symbol) async {
    try {
      final response = await _indicesApi.getIndexDataV1(symbol);
      if (response == null) throw Exception("Failed to fetch index data");
      return SdkMapper.mapIndexToDomain(response);
    } catch (e) {
      rethrow;
    }
  }

  @override
  Stream<SecurityQuote> streamQuotes(List<String> symbols) {
    // Lazy connect
    if (!_isWsConnected) {
      _wsClient.connect().then((_) => _isWsConnected = true).catchError((e) {
         print('WS Connection failed: $e');
      });
    }

    return _quoteStreamController.stream.where((quote) => symbols.contains(quote.symbol));
  }

  @override
  Future<Map<IndexCategory, List<String>>> getAvailableIndices() async {
    try {
      final response = await _indicesApi.getAvailableIndicesV1();
      if (response == null) return {};
      
      return {
        IndexCategory.build: response.broad ?? [],
        IndexCategory.sectoral: response.sector ?? [],
        IndexCategory.thematic: response.thematic ?? [],
        IndexCategory.strategy: response.strategy ?? [],
      };
    } catch (e) {
      return {};
    }
  }

  @override
  Future<List<Candle>> getHistoricalData(String symbol, String range) async {
    try {
      final response = await _apiClient.marketDataApi.getHistoricalCharts(
        symbol: symbol,
        range: range,
      );
      if (response == null) return [];
      return SdkMapper.mapHistoricalDataToCandles(response);
    } catch (e) {
      print('Error fetching history: $e');
      return [];
    }
  }

  @override
  Future<List<SecurityQuote>> searchSecurities(String query) async {
    try {
      final request = SecuritySearchRequestV1(query: query);
      final response = await _apiClient.securityMetadataApi.searchSecurities(
        securitySearchRequestV1: request,
      );
      if (response == null) return [];
      return response.map((s) => SdkMapper.mapSecurityToQuote(s)).toList();
    } catch (e) {
      print('Error searching securities: $e');
      return [];
    }
  }

  @override
  Future<List<SecurityQuote>> getMarketMovers(String type) async {
    try {
      final response = await _apiClient.marketAnalyticsApi.getMovers(
        type: type,
        limit: 10,
      );
      if (response == null) return [];
      return response.map((m) => SdkMapper.mapMarketMoverToQuote(m)).toList();
    } catch (e) {
      print('Error fetching movers: $e');
      return [];
    }
  }
}
