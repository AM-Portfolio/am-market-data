import 'dart:async';
import 'dart:convert';
import 'package:market_data_client/api.dart';
// Accessing src directly as generated API doesn't export custom WebSocket client
import 'package:market_data_client/src/websocket/client.dart';
import '../../domain/models/security_quote.dart';
import '../../domain/models/market_index.dart';
import '../../domain/models/candle.dart';
import '../../domain/models/sector_performance.dart';
import '../../domain/repository/market_data_repository.dart';
import '../mappers/sdk_mapper.dart';
import '../../domain/models/security_search_request.dart';
import '../../utils/app_logger.dart';
// Note: Sdk models like HistoricalDataRequest are in package:market_data_client/api.dart which is already imported

/// Concrete implementation of MarketDataRepository using the generated SDK.
class MarketDataRepositoryImpl implements MarketDataRepository {
  final ApiClient _apiClient;
  final MarketIndicesApi _indicesApi;
  final StockIndicesApi _stockIndicesApi;
  final SecurityMetadataApi _securityApi;
  final MarketAnalyticsApi _analyticsApi;
  final MarketDataApi _marketDataApi;
  final AMMarketDataStreamingAPI _wsClient;
  
  // Broadcast controller to share stream across multiple subscribers
  // Broadcast controller to share stream across multiple subscribers
  final StreamController<SecurityQuote> _quoteStreamController = StreamController<SecurityQuote>.broadcast();
  
  @override
  Stream<SecurityQuote> get anyQuoteStream => _quoteStreamController.stream;

  @override
  Stream<SecurityQuote> streamQuotes(List<String> symbols) {
    // Optionally handle subscription if needed, but for now return the stream
    return _quoteStreamController.stream;
  }
  bool _isWsConnected = false;

  MarketDataRepositoryImpl({
    required String baseUrl,
  })  : _apiClient = ApiClient(basePath: baseUrl),
        _indicesApi = MarketIndicesApi(ApiClient(basePath: baseUrl)),
        _stockIndicesApi = StockIndicesApi(ApiClient(basePath: baseUrl)),
        _securityApi = SecurityMetadataApi(ApiClient(basePath: baseUrl)),
        _analyticsApi = MarketAnalyticsApi(ApiClient(basePath: baseUrl)),
        _marketDataApi = MarketDataApi(ApiClient(basePath: baseUrl)),
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
      final response = await _marketDataApi.getQuotes(symbols.join(','));
      if (response == null) return [];
      
      final List<SecurityQuote> quotes = [];
      response.forEach((key, value) {
         if (value is MarketDataUpdateV1) {
           quotes.add(SdkMapper.mapUpdateToQuote(value));
         } else if (value is Map<String, dynamic>) {
           quotes.add(SdkMapper.mapUpdateToQuote(MarketDataUpdateV1.fromJson(value)!));
         }
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
      final response = await _stockIndicesApi.getStockIndices(symbol);
      if (response == null) throw Exception("Failed to fetch index data");
      return SdkMapper.mapIndexToDomain(response);
    } catch (e) {
      rethrow;
    }
  }

  @override
  Future<Map<IndexCategory, List<String>>> getAvailableIndices() async {
    try {
      final response = await _stockIndicesApi.getAvailableIndices();
      if (response == null) return {};
      
      // The SDK returns Map<String, List<String>>
      return {
        IndexCategory.build: response['broad'] ?? [],
        IndexCategory.sectoral: response['sectoral'] ?? [],
        IndexCategory.thematic: response['thematic'] ?? [],
        IndexCategory.strategy: response['strategy'] ?? [],
      };
    } catch (e) {
      return {};
    }
  }

  @override
  Future<List<Candle>> getHistoricalData(String symbol, String range) async {
    try {
      // Calculate from/to based on range using a helper or simple logic
      final now = DateTime.now();
      DateTime fromDate;
      String timeframe = "1D";

      switch (range) {
        case "1D":
          fromDate = now.subtract(const Duration(days: 1));
          timeframe = "1min"; // intraday
          break;
        case "1W":
          fromDate = now.subtract(const Duration(days: 7));
          timeframe = "30min";
          break;
        case "1M":
          fromDate = now.subtract(const Duration(days: 30));
          timeframe = "1D";
          break;
        case "1Y":
          fromDate = now.subtract(const Duration(days: 365));
          timeframe = "1D";
          break;
        case "5Y":
          fromDate = now.subtract(const Duration(days: 365 * 5));
          timeframe = "1W";
          break;
        default:
          fromDate = now.subtract(const Duration(days: 30));
          timeframe = "1D";
      }

      final request = HistoricalDataRequest(
        symbols: symbol,
        from: fromDate.toIso8601String().split('T')[0],
        to: now.toIso8601String().split('T')[0],
        interval: _mapRangeToInterval(range),
        isIndexSymbol: false,
      );

      final response = await _marketDataApi.getHistoricalData(request);
      if (response == null || response.data == null) return [];
      
      return SdkMapper.mapHistoricalDataToCandles(response);
    } catch (e) {
      print('Error fetching historical data: $e');
      return [];
    }
  }

  HistoricalDataRequestIntervalEnum _mapRangeToInterval(String range) {
    switch (range) {
      case "1D": return HistoricalDataRequestIntervalEnum.MINUTE;
      case "1W": return HistoricalDataRequestIntervalEnum.THIRTY_MINUTE;
      case "1M": return HistoricalDataRequestIntervalEnum.DAY;
      case "1Y": return HistoricalDataRequestIntervalEnum.DAY;
      case "5Y": return HistoricalDataRequestIntervalEnum.WEEK;
      default: return HistoricalDataRequestIntervalEnum.DAY;
    }
  }

  @override
  Future<List<SecurityQuote>> searchSecurities(String query) async {
    try {
      final request = SecuritySearchRequestV1(query: query);
      final response = await _securityApi.search(request);
      if (response == null) return [];
      return response.map((s) => SdkMapper.mapSecurityDtoToQuote(s)).toList();
    } catch (e) {
      print('Error searching securities: $e');
      return [];
    }
  }

  @override
  Future<List<SecurityQuote>> searchSecuritiesAdvanced(SecuritySearchRequest request) async {
    try {
      // SecuritySearchRequestV1 expects specific fields
      final sdkRequest = SecuritySearchRequestV1(
        query: request.queries?.join(' '),
        isin: request.isins?.isNotEmpty == true ? request.isins!.first : null,
        // Add other fields if supported by SecuritySearchRequestV1 or domain model
      );

      final response = await _securityApi.search(sdkRequest);
      if (response == null) return [];
      return response.map((s) => SdkMapper.mapSecurityDtoToQuote(s)).toList();
    } catch (e) {
      print('Error searching securities advanced: $e');
      return [];
    }
  }

  @override
  Future<String?> getLoginUrl(String provider) async {
    try {
      final response = await _marketDataApi.getLoginUrl(provider: provider);
      // SDK response handling
      if (response == null) return null;
      // Assuming response is Map or String. 
      // If generated client typed it, use accessor.
      // If dynamic/Object, cast to map.
      // TODO: Verify actual generated type. 
      // For now assume Map<String, dynamic> from generic object
      if (response is Map<String, dynamic>) {
         return response['loginUrl'] ?? response['url'];
      }
      return response.toString();
    } catch (e) {
      print('Error fetching login URL: $e');
      return null;
    }
  }

  @override
  Future<bool> connectStream(List<String> symbols, String provider, {bool isIndexSymbol = false}) async {
    try {
       // Using subscribe from Polling API as ConnectStreamRequestV1 seems missing
       final pollingApi = MarketDataPollingApi(_apiClient);
       await pollingApi.subscribe(symbols);
       return true;
    } catch (e) {
      print('Error connecting stream: $e');
      return false;
    }
  }

  @override
  Future<bool> disconnectStream(String provider) async {
    try {
       final pollingApi = MarketDataPollingApi(_apiClient);
       await pollingApi.unsubscribe([]); // Unsubscribe all for now or track symbols
       return true;
    } catch (e) {
      print('Error disconnecting stream: $e');
      return false;
    }
  }

  @override
  /// FEATURE: Sector Performance Tracking
  /// This feature provides a high-level overview of how different market sectors 
  /// (e.g., IT, Banking, Energy) are performing in real-time.
  /// 
  /// IMPLEMENTATION: MarketDataRepository.getSectorPerformance
  /// Fetches sector metrics from the Analytics API and maps them to domain models.
  /// Uses a safe iteration strategy to handle the polymorphic nature of the API response
  /// (which can be a List or a Map) and ensures strict type safety for the Flutter Web compiler.
  Future<List<SectorPerformance>> getSectorPerformance(String? indexSymbol) async {
    try {
      final dynamic response = await _analyticsApi.getSectorPerformance();
      final List<SectorPerformance> result = [];
      if (response == null) return result;
      
      final List<dynamic> items = [];
      if (response is List) {
        items.addAll(response);
      } else if (response is Map) {
        items.addAll(response.values);
      }
          
      for (final dynamic item in items) {
        if (item is Map<String, dynamic>) {
          result.add(SdkMapper.mapSectorPerformance(item));
        } else if (item is Map) {
          result.add(SdkMapper.mapSectorPerformance(Map<String, dynamic>.from(item)));
        }
      }
      return result;
    } catch (e) {
      print('Error fetching sector performance: $e');
      return [];
    }
  }

  @override
  /// FEATURE: Market Movers (Gainers & Losers)
  /// Identifies top trending securities based on price action (Top Gainers and Top Losers).
  /// Essential for technical analysis and identifying immediate market opportunities.
  /// 
  /// IMPLEMENTATION: MarketDataRepository.getMarketMovers
  /// Aggregates mover data from the Analytics API. The implementation logic mirrors
  /// the robust mapping used in sector performance to maintain stability across platforms,
  /// specifically targeting compatibility with dart2js intensive optimizations.
  Future<List<SecurityQuote>> getMarketMovers(String type) async {
    try {
      final dynamic response = await _analyticsApi.getTopMovers();
      final List<SecurityQuote> result = [];
      if (response == null) return result;
      
      final List<dynamic> items = [];
      if (response is List) {
        items.addAll(response);
      } else if (response is Map) {
        items.addAll(response.values);
      }
          
      for (final dynamic item in items) {
        if (item is Map<String, dynamic>) {
          result.add(SdkMapper.mapMarketMoverToQuote(item));
        } else if (item is Map) {
          result.add(SdkMapper.mapMarketMoverToQuote(Map<String, dynamic>.from(item)));
        }
      }
      return result;
    } catch (e) {
       print('Error fetching market movers: $e');
       return [];
    }
  }
}
