import 'dart:async';
import 'package:flutter/material.dart';
import '../models/market_data.dart';
import '../services/api_service.dart';
import '../utils/app_logger.dart';

class MarketProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();
  // Logger imported via utils/app_logger.dart (need to add import)

  AvailableIndices? _availableIndices;
  StockIndicesMarketData? _currentIndexData;
  List<StockIndicesMarketData> _allIndicesData = []; // For Market Overview
  
  String? _selectedIndex;
  bool _isLoading = false;
  String? _error;
  bool _forceRefresh = false; // "Force Refresh" toggle state
  bool _indexSymbol = true; // True = fetch index data only, False = expand to constituents

  Map<String, Map<String, dynamic>> _livePrices = {}; // Global live price cache
  final StreamController<Map<String, dynamic>> _livePriceController = StreamController<Map<String, dynamic>>.broadcast();

  AvailableIndices? get availableIndices => _availableIndices;
  StockIndicesMarketData? get currentIndexData => _currentIndexData;
  List<StockIndicesMarketData> get allIndicesData => _allIndicesData;
  Map<String, Map<String, dynamic>> get livePrices => _livePrices;
  Stream<Map<String, dynamic>> get livePriceStream => _livePriceController.stream;
  
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
      _availableIndices = await _apiService.fetchAvailableIndices();
      AppLogger.info("MarketProvider.loadIndices", "Fetched available indices: ${_availableIndices?.broad.length ?? 0} broad, ${_availableIndices?.sectoral.length ?? 0} sectoral");
      if (_availableIndices?.broad.isNotEmpty ?? false) {
        // Auto-select "All Indices" by default to show overview
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
      _currentIndexData = await _apiService.fetchIndexData(_selectedIndex!, forceRefresh: _forceRefresh);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loadAllIndicesData() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      List<String> allSymbols = await _apiService.fetchAllIndices();
      AppLogger.info("MarketProvider.loadAllIndicesData", "Fetched ${allSymbols.length} index symbols");
      
      // User Request: Call live-prices with list of symbols instead of batch loop
      // Pass indexSymbol parameter to control whether to fetch index-level or constituent data
      final Map<String, dynamic> response = await _apiService.fetchLivePrices(allSymbols, _indexSymbol);
      AppLogger.info("MarketProvider.loadAllIndicesData", "Response keys: ${response.keys.toList()}");
      
      // Extract the actual prices - it's a List, not a Map
      final List<dynamic> pricesList = response['prices'] as List<dynamic>? ?? [];
      AppLogger.info("MarketProvider.loadAllIndicesData", "Prices list size: ${pricesList.length}");
      
      // Convert list to map by UPPERCASE symbol for case-insensitive lookup
      final Map<String, dynamic> pricesMap = {};
      for (var item in pricesList) {
        if (item is Map<String, dynamic> && item.containsKey('symbol')) {
          String symbol = item['symbol'].toString().toUpperCase();
          pricesMap[symbol] = item;
        }
      }
      AppLogger.info("MarketProvider.loadAllIndicesData", "Prices map keys: ${pricesMap.keys.toList()}");
      
      List<StockIndicesMarketData> results = [];
      
      for (String sym in allSymbols) {
          // Use uppercase for lookup
          String lookupKey = sym.toUpperCase();
          if (pricesMap.containsKey(lookupKey)) {
             try {
               final item = pricesMap[lookupKey];
               AppLogger.debug("MarketProvider.loadAllIndicesData", "Processing $sym with data: $item");
               
               double ltp = (item['lastPrice'] as num).toDouble();
               double change = (item['change'] as num?)?.toDouble() ?? 0.0;
               double pChange = (item['changePercent'] as num?)?.toDouble() ?? 0.0;
               
               final indexData = StockIndicesMarketData(
                 indexSymbol: sym,
                 lastPrice: ltp,
                 change: change,
                 pChange: pChange,
                 stocks: [],
               );
               
               results.add(indexData);
               AppLogger.debug("MarketProvider.loadAllIndicesData", "Successfully added $sym to results");
             } catch (e) {
                AppLogger.warning("MarketProvider.loadAllIndicesData", "Error parsing $sym: $e", e);
             }
          } else {
             AppLogger.warning("MarketProvider.loadAllIndicesData", "Symbol $sym (lookup: $lookupKey) not found in prices map");
          }
      }
      
      AppLogger.info("MarketProvider.loadAllIndicesData", "Total results parsed: ${results.length}");
      _allIndicesData = results;
    } catch (e) {
      AppLogger.error("MarketProvider.loadAllIndicesData", "Error loading all indices data", e);
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
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

