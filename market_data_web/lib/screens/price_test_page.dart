import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/api_service.dart';

class PriceTestPage extends StatefulWidget {
  const PriceTestPage({super.key});

  @override
  State<PriceTestPage> createState() => _PriceTestPageState();
}

class _PriceTestPageState extends State<PriceTestPage> with SingleTickerProviderStateMixin {
  final TextEditingController _symbolController = TextEditingController();
  final ApiService _apiService = ApiService();
  
  bool _isLoading = false;
  Map<String, dynamic>? _result;
  String? _error;
  bool _isHistoricalMode = false;
  bool _showFilters = false;
  bool _useIndexDropdown = true; // Toggle between text input and dropdown
  
  Set<String> _expandedCards = {};
  
  String _selectedInterval = '1D';
  DateTime? _fromDate;
  DateTime? _toDate;
  
  bool _isIndexSymbol = false;
  bool _forceRefresh = false;
  bool _continuous = false;
  String _instrumentType = 'STOCK';
  
  // Index selection
  Set<String> _selectedIndices = {};
  
  final List<String> _intervals = ['1m', '5m', '15m', '30m', '1H', '1D', '1W', '1M'];
  final List<String> _instrumentTypes = ['STOCK', 'INDEX', 'OPTION', 'FUTURE', 'COMMODITY'];
  
  // Popular indices
  final List<String> _availableIndices = [
    'NIFTY 50',
    'NIFTY BANK',
    'NIFTY IT',
    'NIFTY AUTO',
    'NIFTY PHARMA',
    'NIFTY FMCG',
    'NIFTY METAL',
    'NIFTY REALTY',
    'NIFTY ENERGY',
    'NIFTY INFRA',
  ];

  late AnimationController _filterAnimationController;
  late Animation<double> _filterAnimation;

  @override
  void initState() {
    super.initState();
    _filterAnimationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _filterAnimation = CurvedAnimation(
      parent: _filterAnimationController,
      curve: Curves.easeInOut,
    );
  }

  @override
  void dispose() {
    _filterAnimationController.dispose();
    _symbolController.dispose();
    super.dispose();
  }

  void _toggleFilters() {
    setState(() {
      _showFilters = !_showFilters;
      if (_showFilters) {
        _filterAnimationController.forward();
      } else {
        _filterAnimationController.reverse();
      }
    });
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
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.dark(
              primary: Colors.blueAccent,
              surface: Color(0xFF1E1E1E),
            ),
          ),
          child: child!,
        );
      },
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
      backgroundColor: const Color(0xFF0A0E27),
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
                child: CircularProgressIndicator(color: Colors.blueAccent),
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
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            const Color(0xFF1E3A8A).withOpacity(0.2),
            const Color(0xFF0A0E27),
          ],
        ),
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
              Text('Price Verification Test', style: TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold)),
              Text('Real-time & Historical Market Data', style: TextStyle(color: Colors.white60, fontSize: 12)),
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
          color: const Color(0xFF1E1E2E),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.white.withOpacity(0.1)),
        ),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Toggle between text input and index dropdown
              Row(
                children: [
                  Expanded(
                    child: SegmentedButton<bool>(
                      segments: const [
                        ButtonSegment(value: false, label: Text('Symbol', style: TextStyle(fontSize: 12)), icon: Icon(Icons.edit, size: 16)),
                        ButtonSegment(value: true, label: Text('Indices', style: TextStyle(fontSize: 12)), icon: Icon(Icons.list, size: 16)),
                      ],
                      selected: {_useIndexDropdown},
                      onSelectionChanged: (Set<bool> selection) {
                        setState(() => _useIndexDropdown = selection.first);
                      },
                      style: ButtonStyle(
                        backgroundColor: MaterialStateProperty.resolveWith((states) {
                          if (states.contains(MaterialState.selected)) {
                            return Colors.blueAccent;
                          }
                          return const Color(0xFF0F1419);
                        }),
                        foregroundColor: MaterialStateProperty.resolveWith((states) {
                          if (states.contains(MaterialState.selected)) {
                            return Colors.white;
                          }
                          return Colors.white60;
                        }),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              
              // Symbol input or Index selector
              if (!_useIndexDropdown)
                _buildTextField()
              else
                _buildIndexSelector(),
              
              const SizedBox(height: 16),
              
              // Compact row: Interval + Dates
              Row(
                children: [
                  Expanded(child: _buildIntervalSelector()),
                  const SizedBox(width: 12),
                  Expanded(child: _buildDateButton(context, label: 'From', date: _fromDate, onTap: () => _selectDate(context, true))),
                  const SizedBox(width: 12),
                  Expanded(child: _buildDateButton(context, label: 'To', date: _toDate, onTap: () => _selectDate(context, false))),
                ],
              ),
              
              if (_fromDate != null || _toDate != null)
                Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: TextButton.icon(
                    onPressed: () => setState(() {
                      _fromDate = null;
                      _toDate = null;
                    }),
                    icon: const Icon(Icons.clear, color: Colors.redAccent, size: 16),
                    label: const Text('Clear', style: TextStyle(color: Colors.redAccent, fontSize: 12)),
                  ),
                ),
              
              const SizedBox(height: 16),
              _buildCollapsibleFilters(),
              const SizedBox(height: 16),
              _buildFetchButton(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTextField() {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF0F1419),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: TextField(
        controller: _symbolController,
        style: const TextStyle(color: Colors.white, fontSize: 14),
        decoration: const InputDecoration(
          hintText: 'e.g., RELIANCE, TCS',
          hintStyle: TextStyle(color: Colors.white30, fontSize: 14),
          prefixIcon: Icon(Icons.search, color: Colors.blueAccent, size: 20),
          border: InputBorder.none,
          contentPadding: EdgeInsets.all(14),
        ),
      ),
    );
  }

  Widget _buildIndexSelector() {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF0F1419),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            _selectedIndices.isEmpty ? 'Select Indices' : '${_selectedIndices.length} selected',
            style: const TextStyle(color: Colors.white60, fontSize: 12),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 6,
            runSpacing: 6,
            children: _availableIndices.map((index) {
              final isSelected = _selectedIndices.contains(index);
              return InkWell(
                onTap: () {
                  setState(() {
                    if (isSelected) {
                      _selectedIndices.remove(index);
                    } else {
                      _selectedIndices.add(index);
                    }
                  });
                },
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    color: isSelected ? Colors.blueAccent : const Color(0xFF1E1E2E),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(
                      color: isSelected ? Colors.blueAccent : Colors.white.withOpacity(0.1),
                    ),
                  ),
                  child: Text(
                    index,
                    style: TextStyle(
                      color: isSelected ? Colors.white : Colors.white60,
                      fontSize: 11,
                      fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildIntervalSelector() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFF0F1419),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: _selectedInterval,
          isExpanded: true,
          dropdownColor: const Color(0xFF1E1E2E),
          style: const TextStyle(color: Colors.white, fontSize: 13),
          icon: const Icon(Icons.arrow_drop_down, color: Colors.blueAccent, size: 20),
          items: _intervals.map((interval) => DropdownMenuItem(
            value: interval,
            child: Text(interval, style: const TextStyle(fontSize: 13)),
          )).toList(),
          onChanged: (val) => setState(() => _selectedInterval = val!),
        ),
      ),
    );
  }

  Widget _buildDateButton(BuildContext context, {required String label, required DateTime? date, required VoidCallback onTap}) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: const Color(0xFF0F1419),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: date != null ? Colors.blueAccent.withOpacity(0.5) : Colors.white.withOpacity(0.1)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label, style: const TextStyle(color: Colors.white60, fontSize: 10)),
            const SizedBox(height: 4),
            Text(
              date != null ? DateFormat('dd MMM').format(date) : 'Select',
              style: TextStyle(color: date != null ? Colors.white : Colors.white30, fontSize: 12),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCollapsibleFilters() {
    return Column(
      children: [
        InkWell(
          onTap: _toggleFilters,
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
            decoration: BoxDecoration(
              color: const Color(0xFF0F1419),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.white.withOpacity(0.1)),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Row(
                  children: [
                    Icon(Icons.tune, color: Colors.blueAccent, size: 18),
                    SizedBox(width: 10),
                    Text('Filters', style: TextStyle(color: Colors.white70, fontSize: 13)),
                  ],
                ),
                AnimatedRotation(
                  turns: _showFilters ? 0.5 : 0,
                  duration: const Duration(milliseconds: 300),
                  child: const Icon(Icons.expand_more, color: Colors.white60, size: 20),
                ),
              ],
            ),
          ),
        ),
        
        SizeTransition(
          sizeFactor: _filterAnimation,
          child: Container(
            margin: const EdgeInsets.only(top: 12),
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: const Color(0xFF0F1419),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.white.withOpacity(0.1)),
            ),
            child: Column(
              children: [
                _buildCompactToggle('Index Symbol', _isIndexSymbol, (val) => setState(() => _isIndexSymbol = val)),
                _buildCompactToggle('Force Refresh', _forceRefresh, (val) => setState(() => _forceRefresh = val)),
                _buildCompactToggle('Continuous', _continuous, (val) => setState(() => _continuous = val)),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildCompactToggle(String label, bool value, ValueChanged<bool> onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Colors.white70, fontSize: 12)),
          Transform.scale(
            scale: 0.7,
            child: Switch(
              value: value,
              onChanged: onChanged,
              activeColor: Colors.blueAccent,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFetchButton() {
    return Container(
      width: double.infinity,
      height: 44,
      decoration: BoxDecoration(
        gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)]),
        borderRadius: BorderRadius.circular(12),
      ),
      child: ElevatedButton(
        onPressed: _isLoading ? null : _fetchPrices,
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.transparent,
          shadowColor: Colors.transparent,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
        child: _isLoading
            ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
            : const Text('Get Prices', style: TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w600)),
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
                child: _buildCompactStockCard(symbol, priceData, isLive: true),
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
                child: _buildCompactStockCard(symbol, stockData, isLive: false),
              );
            },
            childCount: symbols.length,
          ),
        ),
      );
    }
  }

  Widget _buildCompactStockCard(String symbol, dynamic data, {required bool isLive}) {
    final isExpanded = _expandedCards.contains(symbol);
    
    // Determine if it's an index (contains common index keywords)
    final isIndex = symbol.toUpperCase().contains('NIFTY') || 
                    symbol.toUpperCase().contains('SENSEX') ||
                    symbol.toUpperCase().contains('INDEX');
    
    // Extract actual date range from data points
    DateTime? actualFromDate;
    DateTime? actualToDate;
    
    if (!isLive && data['dataPoints'] != null && (data['dataPoints'] as List).isNotEmpty) {
      final dataPoints = data['dataPoints'] as List;
      
      // Get first and last timestamps
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
      } catch (e) {
        // Ignore parsing errors
      }
    }
    
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF1E1E2E),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
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
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // First Row: Icon, Symbol, Type Badge, Price
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
                                    style: const TextStyle(color: Colors.white, fontSize: 15, fontWeight: FontWeight.bold),
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                const SizedBox(width: 8),
                                // INDEX/STOCK Badge
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: isIndex ? Colors.purple.withOpacity(0.2) : Colors.blue.withOpacity(0.2),
                                    borderRadius: BorderRadius.circular(4),
                                    border: Border.all(
                                      color: isIndex ? Colors.purpleAccent : Colors.blueAccent,
                                      width: 1,
                                    ),
                                  ),
                                  child: Text(
                                    isIndex ? 'INDEX' : 'STOCK',
                                    style: TextStyle(
                                      color: isIndex ? Colors.purpleAccent : Colors.blueAccent,
                                      fontSize: 9,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 2),
                            Row(
                              children: [
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: isLive ? Colors.green.withOpacity(0.2) : Colors.orange.withOpacity(0.2),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    isLive ? 'LIVE' : 'HISTORICAL',
                                    style: TextStyle(
                                      color: isLive ? Colors.greenAccent : Colors.orangeAccent,
                                      fontSize: 9,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                      if (isLive && data['lastPrice'] != null)
                        Text(
                          '₹${_formatNumber(data['lastPrice'])}',
                          style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                      const SizedBox(width: 12),
                      AnimatedRotation(
                        turns: isExpanded ? 0.5 : 0,
                        duration: const Duration(milliseconds: 300),
                        child: const Icon(Icons.expand_more, color: Colors.white60, size: 20),
                      ),
                    ],
                  ),
                  
                  // Second Row: Actual Date Range from data points
                  if (!isLive && actualFromDate != null && actualToDate != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 8, left: 42),
                      child: Row(
                        children: [
                          Icon(Icons.calendar_today, color: Colors.white.withOpacity(0.4), size: 12),
                          const SizedBox(width: 6),
                          Text(
                            '${DateFormat('dd MMM yyyy').format(actualFromDate)} - ${DateFormat('dd MMM yyyy').format(actualToDate)}',
                            style: const TextStyle(
                              color: Colors.white60,
                              fontSize: 11,
                            ),
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
              padding: const EdgeInsets.fromLTRB(14, 0, 14, 14),
              child: Column(
                children: [
                  const Divider(color: Colors.white10, height: 1),
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
      border: TableBorder.all(color: Colors.white.withOpacity(0.1), width: 1),
      columnWidths: const {
        0: FlexColumnWidth(1),
        1: FlexColumnWidth(1),
      },
      children: [
        _buildTableRow('Open', '₹${_formatNumber(data['ohlc']?['open'])}'),
        _buildTableRow('High', '₹${_formatNumber(data['ohlc']?['high'])}'),
        _buildTableRow('Low', '₹${_formatNumber(data['ohlc']?['low'])}'),
        _buildTableRow('Close', '₹${_formatNumber(data['ohlc']?['close'])}'),
        if (data['volume'] != null)
          _buildTableRow('Volume', _formatNumber(data['volume'])),
      ],
    );
  }

  Widget _buildHistoricalDataTable(dynamic data) {
    if (data['dataPoints'] == null || (data['dataPoints'] as List).isEmpty) {
      return const Text('No data', style: TextStyle(color: Colors.white60, fontSize: 12));
    }

    final dataPoints = data['dataPoints'] as List;
    
    return Column(
      children: [
        Text('${dataPoints.length} Points', style: const TextStyle(color: Colors.white70, fontSize: 12, fontWeight: FontWeight.w600)),
        const SizedBox(height: 8),
        Container(
          constraints: const BoxConstraints(maxHeight: 300),
          child: SingleChildScrollView(
            child: Table(
              border: TableBorder.all(color: Colors.white.withOpacity(0.1), width: 1),
              columnWidths: const {
                0: FlexColumnWidth(2),
                1: FlexColumnWidth(1),
                2: FlexColumnWidth(1),
                3: FlexColumnWidth(1),
                4: FlexColumnWidth(1),
              },
              children: [
                TableRow(
                  decoration: const BoxDecoration(color: Color(0xFF0F1419)),
                  children: ['Time', 'O', 'H', 'L', 'C'].map((h) => 
                    Padding(
                      padding: const EdgeInsets.all(6),
                      child: Text(h, style: const TextStyle(color: Colors.white70, fontSize: 10, fontWeight: FontWeight.bold), textAlign: TextAlign.center),
                    )
                  ).toList(),
                ),
                ...dataPoints.map((point) => TableRow(
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(6),
                      child: Text(_formatTimestamp(point['time']), style: const TextStyle(color: Colors.white60, fontSize: 10)),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(6),
                      child: Text(_formatNumber(point['open']), style: const TextStyle(color: Colors.white, fontSize: 10), textAlign: TextAlign.right),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(6),
                      child: Text(_formatNumber(point['high']), style: const TextStyle(color: Colors.greenAccent, fontSize: 10), textAlign: TextAlign.right),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(6),
                      child: Text(_formatNumber(point['low']), style: const TextStyle(color: Colors.redAccent, fontSize: 10), textAlign: TextAlign.right),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(6),
                      child: Text(_formatNumber(point['close']), style: const TextStyle(color: Colors.white, fontSize: 10), textAlign: TextAlign.right),
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

  TableRow _buildTableRow(String label, String value) {
    return TableRow(
      children: [
        Padding(
          padding: const EdgeInsets.all(8),
          child: Text(label, style: const TextStyle(color: Colors.white60, fontSize: 12)),
        ),
        Padding(
          padding: const EdgeInsets.all(8),
          child: Text(value, style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w600), textAlign: TextAlign.right),
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
          color: Colors.redAccent.withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.redAccent.withOpacity(0.3)),
        ),
        child: Row(
          children: [
            const Icon(Icons.error_outline, color: Colors.redAccent, size: 24),
            const SizedBox(width: 12),
            Expanded(child: Text(_error ?? 'Error', style: const TextStyle(color: Colors.redAccent, fontSize: 12))),
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
          Icon(Icons.analytics_outlined, size: 60, color: Colors.white.withOpacity(0.2)),
          const SizedBox(height: 16),
          Text('Select symbols to get started', style: TextStyle(color: Colors.white.withOpacity(0.4), fontSize: 14)),
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
