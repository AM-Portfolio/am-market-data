import 'package:flutter/material.dart';
import '../models/market_data.dart';
import '../services/api_service.dart';

class MarketProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  AvailableIndices? _availableIndices;
  StockIndicesMarketData? _currentIndexData;
  List<StockIndicesMarketData> _allIndicesData = []; // For Market Overview
  
  String? _selectedIndex;
  bool _isLoading = false;
  String? _error;
  bool _forceRefresh = false; // "Force Refresh" toggle state

  AvailableIndices? get availableIndices => _availableIndices;
  StockIndicesMarketData? get currentIndexData => _currentIndexData;
  List<StockIndicesMarketData> get allIndicesData => _allIndicesData;
  String? get selectedIndex => _selectedIndex;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get forceRefresh => _forceRefresh;

  void toggleForceRefresh(bool value) {
    _forceRefresh = value;
    notifyListeners();
  }

  Future<void> loadIndices() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _availableIndices = await _apiService.fetchAvailableIndices();
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
             print("Error loading $sym: $e");
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

