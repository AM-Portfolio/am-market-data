import 'dart:async';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/api_service.dart';
import '../services/stream_service.dart';
import '../widgets/stock_chart.dart';
import '../utils/app_logger.dart';

class PriceTestPage extends StatefulWidget {
  const PriceTestPage({super.key});

  @override
  State<PriceTestPage> createState() => _PriceTestPageState();
}

class _PriceTestPageState extends State<PriceTestPage> {
  final TextEditingController _symbolController = TextEditingController();
  final ApiService _apiService = ApiService();
  final StreamService _streamService = StreamService();
  StreamSubscription? _streamSubscription;
  
  bool _isLoading = false;
  String? _error;
  bool _showDateRange = false;
  bool _showFilters = false;
  bool _useIndexDropdown = true;
  
  // Live data for all symbols
  Map<String, dynamic> _liveDataMap = {};
  
  // Historical data cache per symbol
  Map<String, List<Map<String, dynamic>>> _historicalDataCache = {};
  
  // Expanded cards tracking
  Set<String> _expandedCards = {};
  
  String _selectedInterval = '1D';
  DateTime? _fromDate;
  DateTime? _toDate;
  
  bool _isIndexSymbol = false;
  bool _forceRefresh = false;
  bool _continuous = false;
  String _instrumentType = 'STOCK';
  
  Set<String> _selectedIndices = {};
  
  final List<String> _intervals = ['1m', '5m', '15m', '30m', '1H', '1D', '1W', '1M'];
  final List<String> _instrumentTypes = ['STOCK', 'INDEX', 'OPTION', 'FUTURE', 'COMMODITY'];
  
  final List<String> _availableIndices = [
    'NIFTY 50', 'NIFTY BANK', 'NIFTY IT', 'NIFTY AUTO', 'NIFTY PHARMA',
    'NIFTY FMCG', 'NIFTY METAL', 'NIFTY REALTY', 'NIFTY ENERGY', 'NIFTY INFRA',
  ];

  @override
  void initState() {
    super.initState();
    _streamService.connect();
    
    // Listen to WebSocket stream for real-time price updates
    _streamSubscription = _streamService.stream.listen((message) {
      if (!mounted) return;
      
      if (message.containsKey('quotes')) {
        setState(() {
          final newQuotes = message['quotes'] as Map<String, dynamic>;
          
          // Update live data map with streaming prices
          newQuotes.forEach((symbol, quoteData) {
            if (_liveDataMap.containsKey(symbol)) {
              // Update existing entry with new price
              _liveDataMap[symbol] = {
                ..._liveDataMap[symbol],
                ...quoteData,
                'lastPrice': quoteData['lastPrice'],
                'change': quoteData['change'],
                'changePercent': quoteData['changePercent'],
              };
            }
          });
        });
      }
    });
    
    AppLogger.info("PriceTestPage.initState", "StreamService connected and listening");
  }

  @override
  void dispose() {
    _streamSubscription?.cancel();
    _streamService.dispose();
    _symbolController.dispose();
    super.dispose();
  }

  Future<void> _fetchPrices() async {
    String symbol;
    
    if (_useIndexDropdown && _selectedIndices.isNotEmpty) {
      symbol = _selectedIndices.join(',');
      _isIndexSymbol = true;
    } else {
      symbol = _symbolController.text.trim();
      if (symbol.isEmpty) {
        setState(() => _error = 'Please enter a symbol or select indices');
        return;
      }
    }

    setState(() {
      _isLoading = true;
      _error = null;
      _liveDataMap = {};
      _historicalDataCache = {};
      _expandedCards.clear();
    });

    try {
      AppLogger.info("PriceTestPage.fetchPrices", "Fetching data for symbols: $symbol");
      
      // Check if we have a complete date range
      final hasCompleteDateRange = _fromDate != null && _toDate != null;
      
      if (!hasCompleteDateRange) {
        // Only fetch live data if no date range is selected
        await _fetchLiveData(symbol);
      } else {
        // Fetch both live and historical data when date range is provided
        await _fetchLiveData(symbol);
        await _fetchHistoricalData(symbol);
      }
      
      setState(() => _isLoading = false);
    } catch (e) {
      AppLogger.error("PriceTestPage.fetchPrices", "Error fetching data", e);
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _fetchLiveData(String symbol) async {
    try {
      AppLogger.info("PriceTestPage.fetchLiveData", "Fetching live data for: $symbol");
      final data = await _apiService.fetchLivePrices([symbol], _isIndexSymbol);
      
      if (data.containsKey('prices') && data['prices'] is List) {
        final prices = data['prices'] as List;
        final Map<String, dynamic> liveMap = {};
        
        for (var price in prices) {
          final sym = price['tradingSymbol'] ?? price['symbol'] ?? 'Unknown';
          liveMap[sym] = price;
        }
        
        setState(() => _liveDataMap = liveMap);
        AppLogger.info("PriceTestPage.fetchLiveData", "Loaded live data for ${liveMap.length} symbols");
        
        // Subscribe to streaming for these symbols
        await _subscribeToStreaming(liveMap.keys.toList());
      }
    } catch (e) {
      AppLogger.error("PriceTestPage.fetchLiveData", "Error fetching live data", e);
      throw Exception('Error fetching live data: $e');
    }
  }

  Future<void> _subscribeToStreaming(List<String> symbols) async {
    if (symbols.isEmpty) return;
    
    try {
      AppLogger.info("PriceTestPage.subscribeToStreaming", "Subscribing to streaming for ${symbols.length} symbols: ${symbols.join(', ')}");
      
      // Use UPSTOX as default provider (can be made configurable)
      final success = await _apiService.connectStream(symbols, 'UPSTOX');
      
      if (success) {
        AppLogger.info("PriceTestPage.subscribeToStreaming", "Successfully subscribed to streaming");
      } else {
        AppLogger.error("PriceTestPage.subscribeToStreaming", "Failed to subscribe to streaming", null);
      }
    } catch (e) {
      AppLogger.error("PriceTestPage.subscribeToStreaming", "Error subscribing to streaming", e);
    }
  }

  Future<void> _fetchHistoricalData(String symbol) async {
    try {
      final dateFormat = DateFormat('yyyy-MM-dd');
      final fromStr = dateFormat.format(_fromDate!);
      final toStr = dateFormat.format(_toDate!);

      AppLogger.info("PriceTestPage.fetchHistoricalData", "Fetching historical data from $fromStr to $toStr");
      
      final data = await _apiService.fetchHistoricalData(
        symbols: [symbol],
        from: fromStr,
        to: toStr,
        interval: _selectedInterval,
        forceRefresh: _forceRefresh,
        isIndexSymbol: _isIndexSymbol,
        instrumentType: _instrumentType,
        continuous: _continuous,
      );

      if (data.containsKey('data')) {
        final historicalData = data['data'] as Map<String, dynamic>;
        final Map<String, List<Map<String, dynamic>>> cache = {};
        
        AppLogger.info("PriceTestPage.fetchHistoricalData", "Processing ${historicalData.length} symbols from response");
        
        historicalData.forEach((sym, stockData) {
          AppLogger.info("PriceTestPage.fetchHistoricalData", "Processing symbol: $sym");
          
          if (stockData is Map && stockData['dataPoints'] != null) {
            final dataPoints = (stockData['dataPoints'] as List)
                .map((e) => Map<String, dynamic>.from(e))
                .toList();
            
            if (dataPoints.isNotEmpty) {
              cache[sym] = dataPoints;
              AppLogger.info("PriceTestPage.fetchHistoricalData", "Cached ${dataPoints.length} data points for $sym");
            }
          }
        });
        
        setState(() => _historicalDataCache = cache);
        AppLogger.info("PriceTestPage.fetchHistoricalData", "Loaded historical data for ${cache.length} symbols: ${cache.keys.join(', ')}");
      }
    } catch (e) {
      AppLogger.error("PriceTestPage.fetchHistoricalData", "Error fetching historical data", e);
      // Don't throw - historical data is optional
    }
  }

  Future<void> _selectDate(BuildContext context, bool isFromDate) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: isFromDate 
          ? (_fromDate ?? DateTime.now().subtract(const Duration(days: 7)))
          : (_toDate ?? DateTime.now()),
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
    );

    if (picked != null) {
      setState(() {
        if (isFromDate) {
          _fromDate = picked;
        } else {
          _toDate = picked;
        }
      });
    }
  }

  void _toggleCard(String symbol) {
    setState(() {
      if (_expandedCards.contains(symbol)) {
        _expandedCards.remove(symbol);
      } else {
        _expandedCards.add(symbol);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(child: _buildHeader()),
          SliverToBoxAdapter(child: _buildInputSection()),
          
          if (_liveDataMap.isNotEmpty)
            _buildResultsSection()
          else if (_error != null)
            SliverToBoxAdapter(child: _buildErrorCard())
          else if (!_isLoading)
            SliverToBoxAdapter(child: _buildEmptyState()),
          
          if (_isLoading)
            const SliverToBoxAdapter(child: Center(
              child: Padding(
                padding: EdgeInsets.all(48),
                child: CircularProgressIndicator(),
              ),
            )),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)]),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(Icons.analytics, color: Colors.white, size: 24),
          ),
          const SizedBox(width: 12),
          const Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Price Verification Test', style: TextStyle(color: Colors.black87, fontSize: 22, fontWeight: FontWeight.bold)),
              Text('Real-time & Historical Market Data', style: TextStyle(color: Colors.black54, fontSize: 12)),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildInputSection() {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.05),
              blurRadius: 10,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildSymbolSelector(),
              const SizedBox(height: 16),
              _buildIntervalDropdown(),
              const SizedBox(height: 16),
              
              // Date Range Toggle
              InkWell(
                onTap: () => setState(() => _showDateRange = !_showDateRange),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  decoration: BoxDecoration(
                    color: Colors.grey[100],
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.grey[300]!),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Icon(Icons.calendar_today, color: Colors.grey[700], size: 18),
                          const SizedBox(width: 10),
                          Text('Date Range (Optional)', style: TextStyle(color: Colors.grey[700], fontSize: 13)),
                        ],
                      ),
                      Icon(_showDateRange ? Icons.expand_less : Icons.expand_more, color: Colors.grey[600]),
                    ],
                  ),
                ),
              ),
              
              if (_showDateRange) ...[
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(child: _buildDateField(context, 'From', _fromDate, () => _selectDate(context, true))),
                    const SizedBox(width: 12),
                    Expanded(child: _buildDateField(context, 'To', _toDate, () => _selectDate(context, false))),
                  ],
                ),
                if (_fromDate != null || _toDate != null)
                  TextButton.icon(
                    onPressed: () => setState(() {
                      _fromDate = null;
                      _toDate = null;
                    }),
                    icon: const Icon(Icons.clear, size: 16),
                    label: const Text('Clear', style: TextStyle(fontSize: 12)),
                  ),
              ],
              
              const SizedBox(height: 16),
              _buildAdvancedFilters(),
              const SizedBox(height: 20),
              _buildFetchButton(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSymbolSelector() {
    return Row(
      children: [
        Expanded(
          child: _useIndexDropdown ? _buildIndexDropdown() : _buildTextInput(),
        ),
        const SizedBox(width: 12),
        IconButton(
          onPressed: () => setState(() => _useIndexDropdown = !_useIndexDropdown),
          icon: Icon(_useIndexDropdown ? Icons.edit : Icons.list),
          tooltip: _useIndexDropdown ? 'Switch to text input' : 'Switch to index selector',
          style: IconButton.styleFrom(
            backgroundColor: Colors.grey[100],
            foregroundColor: Colors.grey[700],
          ),
        ),
      ],
    );
  }

  Widget _buildIndexDropdown() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[300]!),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          hint: Text('Select Indices', style: TextStyle(color: Colors.grey[600], fontSize: 14)),
          value: _selectedIndices.isEmpty ? null : _selectedIndices.first,
          isExpanded: true,
          dropdownColor: Colors.white,
          style: const TextStyle(color: Colors.black87, fontSize: 14),
          items: _availableIndices.map((index) => DropdownMenuItem(
            value: index,
            child: Row(
              children: [
                Checkbox(
                  value: _selectedIndices.contains(index),
                  onChanged: (bool? value) {
                    setState(() {
                      if (value == true) {
                        _selectedIndices.add(index);
                      } else {
                        _selectedIndices.remove(index);
                      }
                    });
                  },
                ),
                Text(index),
              ],
            ),
          )).toList(),
          onChanged: (val) {
            if (val != null) {
              setState(() {
                if (_selectedIndices.contains(val)) {
                  _selectedIndices.remove(val);
                } else {
                  _selectedIndices.add(val);
                }
              });
            }
          },
        ),
      ),
    );
  }

  Widget _buildTextInput() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[300]!),
      ),
      child: TextField(
        controller: _symbolController,
        style: const TextStyle(color: Colors.black87, fontSize: 14),
        decoration: InputDecoration(
          hintText: 'Enter symbol (e.g., RELIANCE, TCS)',
          hintStyle: TextStyle(color: Colors.grey[400], fontSize: 14),
          prefixIcon: Icon(Icons.search, color: Colors.grey[600], size: 20),
          border: InputBorder.none,
          contentPadding: const EdgeInsets.all(14),
        ),
      ),
    );
  }

  Widget _buildIntervalDropdown() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[300]!),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: _selectedInterval,
          isExpanded: true,
          dropdownColor: Colors.white,
          style: const TextStyle(color: Colors.black87, fontSize: 14),
          items: _intervals.map((interval) => DropdownMenuItem(
            value: interval,
            child: Text('Interval: $interval'),
          )).toList(),
          onChanged: (val) => setState(() => _selectedInterval = val!),
        ),
      ),
    );
  }

  Widget _buildDateField(BuildContext context, String label, DateTime? date, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.grey[100],
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: date != null ? Colors.blue[300]! : Colors.grey[300]!),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label, style: TextStyle(color: Colors.grey[600], fontSize: 11)),
            const SizedBox(height: 4),
            Text(
              date != null ? DateFormat('dd MMM yyyy').format(date) : 'Select',
              style: TextStyle(color: date != null ? Colors.black87 : Colors.grey[400], fontSize: 13),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAdvancedFilters() {
    return Column(
      children: [
        InkWell(
          onTap: () => setState(() => _showFilters = !_showFilters),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
            decoration: BoxDecoration(
              color: Colors.grey[100],
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.grey[300]!),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Icon(Icons.tune, color: Colors.grey[700], size: 18),
                    const SizedBox(width: 10),
                    Text('Advanced Filters', style: TextStyle(color: Colors.grey[700], fontSize: 13)),
                  ],
                ),
                Icon(_showFilters ? Icons.expand_less : Icons.expand_more, color: Colors.grey[600]),
              ],
            ),
          ),
        ),
        
        if (_showFilters)
          Container(
            margin: const EdgeInsets.only(top: 12),
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: Colors.grey[50],
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Colors.grey[200]!),
            ),
            child: Column(
              children: [
                _buildToggle('Index Symbol', _isIndexSymbol, (val) => setState(() => _isIndexSymbol = val)),
                _buildToggle('Force Refresh', _forceRefresh, (val) => setState(() => _forceRefresh = val)),
                _buildToggle('Continuous', _continuous, (val) => setState(() => _continuous = val)),
              ],
            ),
          ),
      ],
    );
  }

  Widget _buildToggle(String label, bool value, ValueChanged<bool> onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Colors.black87, fontSize: 13)),
          Switch(value: value, onChanged: onChanged, activeColor: Colors.blue),
        ],
      ),
    );
  }

  Widget _buildFetchButton() {
    return SizedBox(
      width: double.infinity,
      height: 44,
      child: ElevatedButton(
        onPressed: _isLoading ? null : _fetchPrices,
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.blue,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          elevation: 0,
        ),
        child: _isLoading
            ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
            : const Text('Get Prices', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
      ),
    );
  }

  Widget _buildResultsSection() {
    // Combine symbols from both live and historical data
    final allSymbols = <String>{
      ..._liveDataMap.keys,
      ..._historicalDataCache.keys,
    }.toList();
    
    AppLogger.info("PriceTestPage.buildResultsSection", "Displaying ${allSymbols.length} symbols");
    
    return SliverPadding(
      padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
      sliver: SliverList(
        delegate: SliverChildBuilderDelegate(
          (context, index) {
            final symbol = allSymbols[index];
            final liveData = _liveDataMap[symbol];
            final historicalData = _historicalDataCache[symbol];
            
            return Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: _buildStockCard(symbol, liveData, historicalData),
            );
          },
          childCount: allSymbols.length,
        ),
      ),
    );
  }

  Widget _buildStockCard(String symbol, dynamic liveData, List<Map<String, dynamic>>? historicalData) {
    final isExpanded = _expandedCards.contains(symbol);
    final isIndex = symbol.toUpperCase().contains('NIFTY') || 
                    symbol.toUpperCase().contains('SENSEX') ||
                    symbol.toUpperCase().contains('INDEX');
    
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey[200]!),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        children: [
          InkWell(
            onTap: () => _toggleCard(symbol),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)]),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Icon(Icons.show_chart, color: Colors.white, size: 18),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Flexible(
                              child: Text(
                                symbol,
                                style: const TextStyle(color: Colors.black87, fontSize: 15, fontWeight: FontWeight.bold),
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                            const SizedBox(width: 8),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                              decoration: BoxDecoration(
                                color: isIndex ? Colors.purple[50] : Colors.blue[50],
                                borderRadius: BorderRadius.circular(4),
                                border: Border.all(color: isIndex ? Colors.purple : Colors.blue),
                              ),
                              child: Text(
                                isIndex ? 'INDEX' : 'STOCK',
                                style: TextStyle(
                                  color: isIndex ? Colors.purple : Colors.blue,
                                  fontSize: 9,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: Colors.green[50],
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: Text(
                            'LIVE',
                            style: TextStyle(color: Colors.green[700], fontSize: 9, fontWeight: FontWeight.w600),
                          ),
                        ),
                      ],
                    ),
                  ),
                // Always show price section (even if null, show loading)
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    if (liveData != null && liveData['lastPrice'] != null)
                      Text(
                        '₹${_formatNumber(liveData['lastPrice'])}',
                        style: const TextStyle(color: Colors.black87, fontSize: 18, fontWeight: FontWeight.bold),
                      )
                    else
                      Text(
                        '---',
                        style: TextStyle(color: Colors.grey[400], fontSize: 16, fontWeight: FontWeight.bold),
                      ),
                    const SizedBox(height: 4),
                    if (liveData != null && liveData['changePercent'] != null)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: (liveData['changePercent'] >= 0) ? Colors.green[50] : Colors.red[50],
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          '${liveData['changePercent'] >= 0 ? '+' : ''}${_formatNumber(liveData['changePercent'])}%',
                          style: TextStyle(
                            color: (liveData['changePercent'] >= 0) ? Colors.green[700] : Colors.red[700],
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                  ],
                ),
                const SizedBox(width: 12),
                Icon(isExpanded ? Icons.expand_less : Icons.expand_more, color: Colors.grey[600]),
              ],
            ),
          ),
        ),
          
          if (isExpanded && historicalData != null && historicalData.isNotEmpty)
            Container(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
              child: Column(
                children: [
                  Divider(color: Colors.grey[200], height: 1),
                  const SizedBox(height: 12),
                  SizedBox(
                    height: 300,
                    child: StockChart(
                      chartData: historicalData,
                      isLoading: false,
                    ),
                  ),
                ],
              ),
            )
          else if (isExpanded && (historicalData == null || historicalData.isEmpty))
            Container(
              padding: const EdgeInsets.all(16),
              child: Text(
                'No historical data available. Please select a date range.',
                style: TextStyle(color: Colors.grey[600], fontSize: 12),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildErrorCard() {
    return Padding(
      padding: const EdgeInsets.all(20),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.red[50],
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.red[200]!),
        ),
        child: Row(
          children: [
            Icon(Icons.error_outline, color: Colors.red[700], size: 24),
            const SizedBox(width: 12),
            Expanded(child: Text(_error ?? 'Error', style: TextStyle(color: Colors.red[700], fontSize: 12))),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Padding(
      padding: const EdgeInsets.all(40),
      child: Column(
        children: [
          Icon(Icons.analytics_outlined, size: 60, color: Colors.grey[400]),
          const SizedBox(height: 16),
          Text('Select symbols to get started', style: TextStyle(color: Colors.grey[600], fontSize: 14)),
        ],
      ),
    );
  }

  String _formatNumber(dynamic value) {
    if (value == null) return '-';
    if (value is num) return value.toStringAsFixed(2);
    return value.toString();
  }
}
