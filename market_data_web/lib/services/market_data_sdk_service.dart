import 'package:market_data_client/api.dart';
import '../utils/app_logger.dart';

/// Service class that wraps the generated Market Data SDK
/// Provides a clean interface for the Flutter web app to interact with the backend API
class MarketDataSdkService {
  static const String baseUrl = 'http://localhost:8092';
  
  // SDK API Clients
  late final ApiClient _apiClient;
  late final StockIndicesApi _stockIndicesApi;
  late final MarketDataApi _marketDataApi;
  late final MarketAnalyticsApi _marketAnalyticsApi;
  late final SecurityMetadataApi _securityApi;
  late final MarketDataPollingApi _pollingApi;
  
  MarketDataSdkService() {
    _initializeClients();
  }
  
  void _initializeClients() {
    _apiClient = ApiClient(basePath: baseUrl);
    _stockIndicesApi = StockIndicesApi(_apiClient);
    _marketDataApi = MarketDataApi(_apiClient);
    _marketAnalyticsApi = MarketAnalyticsApi(_apiClient);
    _securityApi = SecurityMetadataApi(_apiClient);
    _pollingApi = MarketDataPollingApi(_apiClient);
  }
  
  // ==================== Stock Indices Methods ====================
  
  /// Fetch available indices (broad and sector)
  Future<AvailableIndicesResponseV1?> fetchAvailableIndices() async {
    try {
      return await _stockIndicesApi.getAvailableIndices();
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchAvailableIndices", "Error fetching indices", e);
      rethrow;
    }
  }
  
  /// Fetch all available indices as a flat list
  Future<List<String>> fetchAllIndices() async {
    try {
      final response = await _stockIndicesApi.getAvailableIndices();
      if (response == null) return [];
      
      List<String> all = [];
      if (response.broad != null) all.addAll(response.broad!);
      if (response.sector != null) all.addAll(response.sector!);
      return all;
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchAllIndices", "Error fetching all indices", e);
      return [];
    }
  }
  
  /// Fetch stock indices data for specific indices
  Future<List<StockIndicesMarketDataV1>?> fetchStockIndicesBatch(
    List<String> indexSymbols, {
    bool forceRefresh = false,
  }) async {
    try {
      return await _stockIndicesApi.getStockIndicesBatch(
        indexSymbols: indexSymbols,
        forceRefresh: forceRefresh,
      );
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchStockIndicesBatch", 
        "Error fetching indices: $indexSymbols", e);
      rethrow;
    }
  }
  
  /// Fetch single index data
  Future<StockIndicesMarketDataV1?> fetchIndexData(
    String indexSymbol, {
    bool forceRefresh = false,
  }) async {
    try {
      final batch = await fetchStockIndicesBatch([indexSymbol], forceRefresh: forceRefresh);
      return (batch != null && batch.isNotEmpty) ? batch.first : null;
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchIndexData", 
        "Error fetching index: $indexSymbol", e);
      return null;
    }
  }
  
  // ==================== Market Data Methods ====================
  
  /// Fetch live prices for symbols
  Future<Map<String, OHLCQuoteV1>?> fetchLivePrices(
    List<String> symbols, {
    bool isIndexSymbol = true,
  }) async {
    try {
      return await _marketDataApi.getLivePrices(
        symbols: symbols.join(','),
        isIndexSymbol: isIndexSymbol,
      );
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchLivePrices", 
        "Error fetching live prices", e);
      return null;
    }
  }
  
  /// Fetch historical data
  Future<Map<String, HistoricalDataResponseV1>?> fetchHistoricalData({
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
      final request = HistoricalDataRequestV1(
        symbols: symbols.join(','),
        from: from,
        to: to,
        interval: interval,
        forceRefresh: forceRefresh,
        isIndexSymbol: isIndexSymbol,
        instrumentType: instrumentType,
        continuous: continuous,
      );
      
      return await _marketDataApi.getHistoricalData(
        historicalDataRequestV1: request,
      );
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchHistoricalData", 
        "Error fetching historical data", e);
      rethrow;
    }
  }
  
  /// Fetch historical chart data
  Future<Map<String, HistoricalDataResponseV1>?> fetchHistoricalCharts(
    String symbol,
    String range,
  ) async {
    try {
      return await _marketDataApi.getHistoricalCharts(
        symbol: symbol,
        range: range,
      );
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchHistoricalCharts", 
        "Error fetching charts for $symbol", e);
      return null;
    }
  }
  
  // ==================== Market Analytics Methods ====================
  
  /// Fetch market movers (gainers/losers)
  Future<List<MarketMoverV1>?> fetchMovers({
    String type = 'gainers',
    int limit = 10,
    String? indexSymbol,
  }) async {
    try {
      return await _marketAnalyticsApi.getMovers(
        type: type,
        limit: limit,
        indexSymbol: indexSymbol,
      );
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchMovers", 
        "Error fetching $type", e);
      return null;
    }
  }
  
  /// Fetch sector performance
  Future<List<SectorPerformanceV1>?> fetchSectorPerformance({
    String? indexSymbol,
  }) async {
    try {
      return await _marketAnalyticsApi.getSectorPerformance(
        indexSymbol: indexSymbol,
      );
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchSectorPerformance", 
        "Error fetching sector performance", e);
      return null;
    }
  }
  
  /// Fetch market cap analysis
  Future<MarketCapAnalysisV1?> fetchMarketCapAnalysis() async {
    try {
      return await _marketAnalyticsApi.getMarketCapAnalysis();
    } catch (e) {
      AppLogger.error("MarketDataSdkService.fetchMarketCapAnalysis", 
        "Error fetching market cap analysis", e);
      return null;
    }
  }
  
  // ==================== Security Search Methods ====================
  
  /// Search securities
  Future<List<SecurityV1>?> searchSecurities(String query) async {
    try {
      final request = SecuritySearchRequestV1(query: query);
      return await _securityApi.searchSecurities(
        securitySearchRequestV1: request,
      );
    } catch (e) {
      AppLogger.error("MarketDataSdkService.searchSecurities", 
        "Error searching securities", e);
      return null;
    }
  }
  
  // ==================== Streaming Methods ====================
  
  /// Connect to market data stream
  Future<bool> connectStream(
    List<String> symbols,
    String provider, {
    bool isIndexSymbol = false,
  }) async {
    try {
      final request = StreamConnectionRequestV1(
        instrumentKeys: symbols,
        mode: 'FULL',
        provider: provider,
        isIndexSymbol: isIndexSymbol,
      );
      
      await _pollingApi.connectStream(
        streamConnectionRequestV1: request,
      );
      return true;
    } catch (e) {
      AppLogger.error("MarketDataSdkService.connectStream", 
        "Error connecting stream", e);
      return false;
    }
  }
  
  /// Disconnect from market data stream
  Future<bool> disconnectStream(String provider) async {
    try {
      await _pollingApi.disconnectStream(provider: provider);
      return true;
    } catch (e) {
      AppLogger.error("MarketDataSdkService.disconnectStream", 
        "Error disconnecting stream", e);
      return false;
    }
  }
  
  /// Get authentication login URL
  Future<String?> getLoginUrl(String provider) async {
    try {
      final response = await _pollingApi.getLoginUrl(provider: provider);
      return response?.loginUrl;
    } catch (e) {
      AppLogger.error("MarketDataSdkService.getLoginUrl", 
        "Error getting login URL", e);
      return null;
    }
  }
}
