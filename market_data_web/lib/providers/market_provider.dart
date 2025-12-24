import 'package:flutter/material.dart';
import '../models/market_data.dart';
import '../services/api_service.dart';

class MarketProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  AvailableIndices? _availableIndices;
  StockIndicesMarketData? _currentIndexData;
  String? _selectedIndex;
  bool _isLoading = false;
  String? _error;

  AvailableIndices? get availableIndices => _availableIndices;
  StockIndicesMarketData? get currentIndexData => _currentIndexData;
  String? get selectedIndex => _selectedIndex;
  bool get isLoading => _isLoading;
  String? get error => _error;

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
    await refreshIndexData();
  }

  Future<void> refreshIndexData({bool force = false}) async {
    if (_selectedIndex == null) return;
    
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _currentIndexData = await _apiService.fetchIndexData(_selectedIndex!, forceRefresh: force);
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
