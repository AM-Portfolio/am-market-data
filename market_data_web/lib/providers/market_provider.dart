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

  void toggleForceRefresh(bool value) {
    _forceRefresh = value;
    notifyListeners();
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
        selectIndex(_availableIndices!.broad.first); // Auto-select first
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
      List<StockIndicesMarketData> results = [];
      
      // Fetch in parallel for better performance
      // Note: Backend might rate limit, but for local it's fine.
      // Batch size or sequential might be safer for large lists.
      for (String sym in allSymbols) {
          try {
             var data = await _apiService.fetchIndexData(sym, forceRefresh: _forceRefresh);
             results.add(data);
          } catch (e) {
             AppLogger.warning("MarketProvider.loadAllIndicesData", "Error loading $sym", e);
          }
      }
      _allIndicesData = results;
    } catch (e) {
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

