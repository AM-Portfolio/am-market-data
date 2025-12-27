import 'dart:async';
import 'package:flutter/material.dart';
import '../domain/repository/market_data_repository.dart';
import '../domain/models/market_index.dart';
import '../domain/models/security_quote.dart';
import '../utils/app_logger.dart';

class MarketProvider with ChangeNotifier {
  final MarketDataRepository repository;

  // Domain Models State
  Map<IndexCategory, List<String>> _availableIndices = {};
  MarketIndex? _currentIndexData;
  List<MarketIndex> _allIndicesData = []; 
  
  String? _selectedIndex;
  bool _isLoading = false;
  String? _error;
  bool _forceRefresh = false;
  bool _indexSymbol = true;

  // Live Prices cache (Key: Symbol, Value: SecurityQuote)
  final Map<String, SecurityQuote> _livePrices = {};
  final StreamController<SecurityQuote> _livePriceController = StreamController<SecurityQuote>.broadcast();

  // Subscription management
  StreamSubscription<SecurityQuote>? _subscription;

  MarketProvider({required this.repository});

  Map<IndexCategory, List<String>> get availableIndices => _availableIndices;
  MarketIndex? get currentIndexData => _currentIndexData;
  List<MarketIndex> get allIndicesData => _allIndicesData;
  Map<String, SecurityQuote> get livePrices => _livePrices;
  Stream<SecurityQuote> get livePriceStream => _livePriceController.stream;
  
  String? get selectedIndex => _selectedIndex;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get forceRefresh => _forceRefresh;
  bool get indexSymbol => _indexSymbol;

  void toggleForceRefresh(bool value) {
    _forceRefresh = value;
    notifyListeners();
  }

  void toggleIndexSymbol(bool value) {
    _indexSymbol = value;
    notifyListeners();
    // Auto-reload data when toggle changes
    if (_selectedIndex == "All Indices") {
      loadAllIndicesData();
    }
  }

  void updateLivePrice(Map<String, dynamic> data) {
    if (data.containsKey('symbol')) {
      final String rawSymbol = data['symbol'];
      
      // 1. Store with raw key (e.g., "NSE_EQ:TCS")
      _livePrices[rawSymbol] = data;

      // 2. Store with base key (e.g., "TCS") if a prefix exists
      if (rawSymbol.contains(':')) {
        final baseSymbol = rawSymbol.split(':').last;
        _livePrices[baseSymbol] = data;
      }

      // Emit event to stream instead of global notifyListeners
      _livePriceController.add(data);
    }
  }

  @override
  void dispose() {
    _livePriceController.close();
    super.dispose();
  }

  Future<void> loadIndices() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _availableIndices = await repository.getAvailableIndices();
      int broadCount = _availableIndices[IndexCategory.build]?.length ?? 0;
      int sectorCount = _availableIndices[IndexCategory.sectoral]?.length ?? 0;
      
      AppLogger.info("MarketProvider.loadIndices", "Fetched available indices: $broadCount broad, $sectorCount sectoral");
      
      if (broadCount > 0) {
        selectIndex("All Indices");
      }
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> selectIndex(String indexSymbol) async {
    _selectedIndex = indexSymbol;
    if (_selectedIndex == "All Indices") {
      await loadAllIndicesData();
    } else if (_selectedIndex == "Streamer") {
      // Do nothing, just update selection
      notifyListeners();
    } else {
      await refreshIndexData();
    }
  }

  Future<void> refreshIndexData() async {
    if (_selectedIndex == null || _selectedIndex == "All Indices" || _selectedIndex == "Streamer") return;
    
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentIndexData = await repository.getIndexDetails(_selectedIndex!);
      
      // Auto-subscribe to constituents for real-time updates
      _subscribeToLiveUpdates(_currentIndexData!.constituents.map((s) => s.symbol).toList());
      
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void _subscribeToLiveUpdates(List<String> symbols) {
     _subscription?.cancel();
     _subscription = repository.streamQuotes(symbols).listen((quote) {
        _livePrices[quote.symbol] = quote;
        _livePriceController.add(quote);
        // Also update components within current index data if matches?
        // Ideally UI listens to livePriceStream
     });
  }

  Future<void> loadAllIndicesData() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      // 1. Get all available indices
      final indicesMap = await repository.getAvailableIndices();
      final allSymbols = indicesMap.values.expand((element) => element).toSet().toList();
      
      AppLogger.info("MarketProvider.loadAllIndicesData", "Fetched ${allSymbols.length} index symbols");
      if (allSymbols.isEmpty) return;

      // 2. Fetch quotes for all indices
      final quotes = await repository.getQuotes(allSymbols);
      
      // 3. Map to MarketIndex domain models (lightweight, no constituents)
      _allIndicesData = quotes.map((quote) => MarketIndex(
        symbol: quote.symbol,
        name: quote.symbol, // Use symbol as name if name unavailable in quote
        category: _getCategoryForSymbol(quote.symbol, indicesMap),
        quote: quote,
        constituents: [],
      )).toList();
      
      AppLogger.info("MarketProvider.loadAllIndicesData", "Total results parsed: ${_allIndicesData.length}");
      
    } catch (e) {
      AppLogger.error("MarketProvider.loadAllIndicesData", "Error loading all indices data", e);
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  IndexCategory _getCategoryForSymbol(String symbol, Map<IndexCategory, List<String>> map) {
    for (var entry in map.entries) {
      if (entry.value.contains(symbol)) return entry.key;
    }
    return IndexCategory.sectoral; // Default fallback
  }

  Future<void> refreshCookies() async {
     // Cookie management is now handled by the SDK/Client internally.
     // If explicit refresh is needed, expose via Repository.
     // For now, trigger a reload.
     if (_selectedIndex == "All Indices") {
        await loadAllIndicesData();
      } else if (_selectedIndex != null) {
        await refreshIndexData();
      }
  }
}
  }

  Future<void> refreshCookies() async {
    bool success = await _apiService.refreshCookies();
    if (success) {
      // Re-fetch current view data
      if (_selectedIndex == "All Indices") {
        await loadAllIndicesData();
      } else if (_selectedIndex != null) {
        await refreshIndexData();
      }
    } else {
      _error = "Failed to refresh cookies";
      notifyListeners();
    }
  }
}

