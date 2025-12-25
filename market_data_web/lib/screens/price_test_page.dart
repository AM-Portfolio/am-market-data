import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/api_service.dart';

class PriceTestPage extends StatefulWidget {
  const PriceTestPage({super.key});

  @override
  State<PriceTestPage> createState() => _PriceTestPageState();
}

class _PriceTestPageState extends State<PriceTestPage> {
  final TextEditingController _symbolController = TextEditingController();
  final ApiService _apiService = ApiService();
  
  bool _isLoading = false;
  Map<String, dynamic>? _result;
  String? _error;
  bool _isHistoricalMode = false;
  bool _showFilters = false;
  bool _showDateRange = false; // Date range is optional
  bool _useIndexDropdown = true;
  
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
  void dispose() {
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
      _result = null;
      _expandedCards.clear();
    });

    try {
      final hasDateRange = _fromDate != null && _toDate != null;
      
      if (hasDateRange) {
        await _fetchHistoricalData(symbol);
      } else {
        await _fetchLiveData(symbol);
      }
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _fetchLiveData(String symbol) async {
    try {
      final data = await _apiService.fetchLivePrices([symbol], _isIndexSymbol);
      
      setState(() {
        _isHistoricalMode = false;
        if (data.containsKey('prices') && data['prices'] is List) {
          final prices = data['prices'] as List;
          if (prices.isNotEmpty) {
            _result = {'mode': 'live', 'data': prices, 'timestamp': data['timestamp']};
          } else {
            _error = 'No data found';
          }
        } else {
          _error = 'Unexpected response format';
        }
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Error: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _fetchHistoricalData(String symbol) async {
    try {
      final dateFormat = DateFormat('yyyy-MM-dd');
      final fromStr = dateFormat.format(_fromDate!);
      final toStr = dateFormat.format(_toDate!);

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

      setState(() {
        _isHistoricalMode = true;
        if (data.containsKey('data')) {
          _result = {
            'mode': 'historical',
            'data': data['data'],
            'metadata': {
              'from': fromStr,
              'to': toStr,
              'interval': _selectedInterval,
              'count': data['count'] ?? 0,
            }
          };
        } else if (data.containsKey('error')) {
          _error = data['error'].toString();
        } else {
          _error = 'No data found';
        }
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Error: $e';
        _isLoading = false;
      });
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(child: _buildHeader()),
          SliverToBoxAdapter(child: _buildInputSection()),
          
          if (_result != null)
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
              // Symbol/Indices Dropdown
              _buildSymbolSelector(),
              const SizedBox(height: 16),
              
              // Interval
              _buildIntervalDropdown(),
              const SizedBox(height: 16),
              
              // Optional Date Range Toggle
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
                          Text(
                            'Date Range (Optional)',
                            style: TextStyle(color: Colors.grey[700], fontSize: 13),
                          ),
                        ],
                      ),
                      Icon(
                        _showDateRange ? Icons.expand_less : Icons.expand_more,
                        color: Colors.grey[600],
                      ),
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
              
              // Advanced Filters
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
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(
              child: _useIndexDropdown
                  ? _buildIndexDropdown()
                  : _buildTextInput(),
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
          Switch(
            value: value,
            onChanged: onChanged,
            activeColor: Colors.blue,
          ),
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
    if (_result == null) return const SliverToBoxAdapter(child: SizedBox.shrink());

    final mode = _result!['mode'];
    final data = _result!['data'];

    if (mode == 'live') {
      final prices = data as List;
      return SliverPadding(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
        sliver: SliverList(
          delegate: SliverChildBuilderDelegate(
            (context, index) {
              final priceData = prices[index];
              final symbol = priceData['tradingSymbol'] ?? priceData['symbol'] ?? 'Unknown';
              return Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: _buildStockCard(symbol, priceData, isLive: true),
              );
            },
            childCount: prices.length,
          ),
        ),
      );
    } else {
      final historicalData = data as Map<String, dynamic>;
      final symbols = historicalData.keys.toList();
      
      return SliverPadding(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
        sliver: SliverList(
          delegate: SliverChildBuilderDelegate(
            (context, index) {
              final symbol = symbols[index];
              final stockData = historicalData[symbol];
              return Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: _buildStockCard(symbol, stockData, isLive: false),
              );
            },
            childCount: symbols.length,
          ),
        ),
      );
    }
  }

  Widget _buildStockCard(String symbol, dynamic data, {required bool isLive}) {
    final isExpanded = _expandedCards.contains(symbol);
    
    final isIndex = symbol.toUpperCase().contains('NIFTY') || 
                    symbol.toUpperCase().contains('SENSEX') ||
                    symbol.toUpperCase().contains('INDEX');
    
    DateTime? actualFromDate;
    DateTime? actualToDate;
    
    if (!isLive && data['dataPoints'] != null && (data['dataPoints'] as List).isNotEmpty) {
      final dataPoints = data['dataPoints'] as List;
      try {
        final firstPoint = dataPoints.first;
        final lastPoint = dataPoints.last;
        
        if (firstPoint['time'] != null) {
          if (firstPoint['time'] is int) {
            actualFromDate = DateTime.fromMillisecondsSinceEpoch(firstPoint['time']);
          } else if (firstPoint['time'] is String) {
            actualFromDate = DateTime.parse(firstPoint['time']);
          }
        }
        
        if (lastPoint['time'] != null) {
          if (lastPoint['time'] is int) {
            actualToDate = DateTime.fromMillisecondsSinceEpoch(lastPoint['time']);
          } else if (lastPoint['time'] is String) {
            actualToDate = DateTime.parse(lastPoint['time']);
          }
        }
      } catch (e) {}
    }
    
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
            onTap: () {
              setState(() {
                if (isExpanded) {
                  _expandedCards.remove(symbol);
                } else {
                  _expandedCards.add(symbol);
                }
              });
            },
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)]),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Icon(isLive ? Icons.show_chart : Icons.bar_chart, color: Colors.white, size: 18),
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
                                color: isLive ? Colors.green[50] : Colors.orange[50],
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                isLive ? 'LIVE' : 'HISTORICAL',
                                style: TextStyle(
                                  color: isLive ? Colors.green[700] : Colors.orange[700],
                                  fontSize: 9,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                      if (isLive && data['lastPrice'] != null)
                        Text('₹${_formatNumber(data['lastPrice'])}', style: const TextStyle(color: Colors.black87, fontSize: 16, fontWeight: FontWeight.bold)),
                      const SizedBox(width: 12),
                      Icon(isExpanded ? Icons.expand_less : Icons.expand_more, color: Colors.grey[600]),
                    ],
                  ),
                  
                  if (!isLive && actualFromDate != null && actualToDate != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 8, left: 42),
                      child: Row(
                        children: [
                          Icon(Icons.calendar_today, color: Colors.grey[600], size: 12),
                          const SizedBox(width: 6),
                          Text(
                            '${DateFormat('dd MMM yyyy').format(actualFromDate)} - ${DateFormat('dd MMM yyyy').format(actualToDate)}',
                            style: TextStyle(color: Colors.grey[600], fontSize: 11),
                          ),
                        ],
                      ),
                    ),
                ],
              ),
            ),
          ),
          
          if (isExpanded)
            Container(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
              child: Column(
                children: [
                  Divider(color: Colors.grey[200], height: 1),
                  const SizedBox(height: 12),
                  if (isLive)
                    _buildLiveDataTable(data)
                  else
                    _buildHistoricalDataTable(data),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildLiveDataTable(dynamic data) {
    return Table(
      border: TableBorder.all(color: Colors.grey[300]!),
      columnWidths: const {
        0: FlexColumnWidth(1),
        1: FlexColumnWidth(1),
      },
      children: [
        _buildTableRow('Open', '₹${_formatNumber(data['ohlc']?['open'])}', isHeader: false),
        _buildTableRow('High', '₹${_formatNumber(data['ohlc']?['high'])}', isHeader: false),
        _buildTableRow('Low', '₹${_formatNumber(data['ohlc']?['low'])}', isHeader: false),
        _buildTableRow('Close', '₹${_formatNumber(data['ohlc']?['close'])}', isHeader: false),
        if (data['volume'] != null)
          _buildTableRow('Volume', _formatNumber(data['volume']), isHeader: false),
      ],
    );
  }

  Widget _buildHistoricalDataTable(dynamic data) {
    if (data['dataPoints'] == null || (data['dataPoints'] as List).isEmpty) {
      return Text('No data', style: TextStyle(color: Colors.grey[600], fontSize: 12));
    }

    final dataPoints = data['dataPoints'] as List;
    
    return Column(
      children: [
        Text('${dataPoints.length} Data Points', style: const TextStyle(color: Colors.black87, fontSize: 12, fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        Container(
          constraints: const BoxConstraints(maxHeight: 300),
          child: SingleChildScrollView(
            child: Table(
              border: TableBorder.all(color: Colors.grey[300]!),
              columnWidths: const {
                0: FlexColumnWidth(2.5),
                1: FlexColumnWidth(1),
                2: FlexColumnWidth(1),
                3: FlexColumnWidth(1),
                4: FlexColumnWidth(1),
              },
              children: [
                TableRow(
                  decoration: BoxDecoration(color: Colors.grey[100]),
                  children: ['Time', 'Open', 'High', 'Low', 'Close'].map((h) => 
                    Padding(
                      padding: const EdgeInsets.all(8),
                      child: Text(h, style: const TextStyle(color: Colors.black87, fontSize: 11, fontWeight: FontWeight.bold), textAlign: TextAlign.center),
                    )
                  ).toList(),
                ),
                ...dataPoints.map((point) => TableRow(
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(8),
                      child: Text(_formatTimestamp(point['time']), style: TextStyle(color: Colors.grey[700], fontSize: 10)),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8),
                      child: Text(_formatNumber(point['open']), style: const TextStyle(color: Colors.black87, fontSize: 10), textAlign: TextAlign.right),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8),
                      child: Text(_formatNumber(point['high']), style: TextStyle(color: Colors.green[700], fontSize: 10), textAlign: TextAlign.right),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8),
                      child: Text(_formatNumber(point['low']), style: TextStyle(color: Colors.red[700], fontSize: 10), textAlign: TextAlign.right),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8),
                      child: Text(_formatNumber(point['close']), style: const TextStyle(color: Colors.black87, fontSize: 10), textAlign: TextAlign.right),
                    ),
                  ],
                )).toList(),
              ],
            ),
          ),
        ),
      ],
    );
  }

  TableRow _buildTableRow(String label, String value, {bool isHeader = false}) {
    return TableRow(
      decoration: isHeader ? BoxDecoration(color: Colors.grey[100]) : null,
      children: [
        Padding(
          padding: const EdgeInsets.all(8),
          child: Text(label, style: TextStyle(color: Colors.grey[700], fontSize: 12, fontWeight: isHeader ? FontWeight.bold : FontWeight.normal)),
        ),
        Padding(
          padding: const EdgeInsets.all(8),
          child: Text(value, style: const TextStyle(color: Colors.black87, fontSize: 12), textAlign: TextAlign.right),
        ),
      ],
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

  String _formatTimestamp(dynamic timestamp) {
    if (timestamp == null) return '-';
    try {
      if (timestamp is int) {
        return DateFormat('dd/MM HH:mm').format(DateTime.fromMillisecondsSinceEpoch(timestamp));
      }
      if (timestamp is String) {
        return DateFormat('dd/MM HH:mm').format(DateTime.parse(timestamp));
      }
    } catch (e) {}
    return timestamp.toString();
  }
}
