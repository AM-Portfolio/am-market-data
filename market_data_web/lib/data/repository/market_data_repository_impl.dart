import 'package:market_data_client/api.dart';
import '../../domain/models/security_quote.dart';
import '../../domain/models/market_index.dart';
import '../../domain/repository/market_data_repository.dart';
import '../mappers/sdk_mapper.dart';

/// Concrete implementation of MarketDataRepository using the generated SDK.
class MarketDataRepositoryImpl implements MarketDataRepository {
  final ApiClient _apiClient;
  final MarketDataPollingApi _pollingApi;
  final MarketIndicesApi _indicesApi;
  final MarketDataWebSocketClient _wsClient;

  MarketDataRepositoryImpl({
    required String baseUrl,
  })  : _apiClient = ApiClient(basePath: baseUrl),
        _pollingApi = MarketDataPollingApi(ApiClient(basePath: baseUrl)),
        _indicesApi = MarketIndicesApi(ApiClient(basePath: baseUrl)),
        _wsClient = MarketDataWebSocketClient(baseUrl: baseUrl.replaceFirst('http', 'ws'));

  @override
  Future<List<SecurityQuote>> getQuotes(List<String> symbols) async {
    try {
      // In MarketDataPollingApi, it returns a Map<String, Object> via deserialize
      // but the underlying Spring API returns a Map<String, MarketDataUpdateV1>
      final response = await _pollingApi.getQuotesV1(QuotesRequest(symbols: symbols));
      if (response == null) return [];
      
      final List<SecurityQuote> quotes = [];
      response.forEach((key, value) {
        if (value is Map<String, dynamic>) {
          quotes.add(SdkMapper.mapUpdateToQuote(MarketDataUpdateV1.fromJson(value)!));
        }
      });
      return quotes;
    } catch (e) {
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
    // Connect if not connected
    if (!_wsClient.isConnected) {
      _wsClient.connect();
    }

    return _wsClient.updates
        .where((update) => symbols.contains(update.symbol))
        .map((update) => SdkMapper.mapUpdateToQuote(update));
  }

  @override
  Future<Map<IndexCategory, List<String>>> getAvailableIndices() async {
    try {
      final response = await _indicesApi.getAvailableIndicesV1();
      if (response == null) return {};
      
      return {
        IndexCategory.build: response.broad ?? [],
        IndexCategory.sectoral: response.sector ?? [],
      };
    } catch (e) {
      return {};
    }
  }
}
