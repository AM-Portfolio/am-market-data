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
  
  // Historical data inputs
  String _selectedInterval = '1D';
  DateTime? _fromDate;
  DateTime? _toDate;
  
  // New filter options
  bool _isIndexSymbol = false;
  bool _forceRefresh = false;
  bool _continuous = false;
  String _instrumentType = 'STOCK';
  
  final List<String> _intervals = [
    '1m', '5m', '15m', '30m', '1H', '1D', '1W', '1M'
  ];
  
  final List<String> _instrumentTypes = [
    'STOCK', 'INDEX', 'OPTION', 'FUTURE', 'COMMODITY'
  ];

  Future<void> _fetchPrices() async {
    final symbol = _symbolController.text.trim();
    if (symbol.isEmpty) {
      setState(() {
        _error = 'Please enter a trading symbol';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _error = null;
      _result = null;
    });

    try {
      // Determine mode based on date inputs
      final hasDateRange = _fromDate != null && _toDate != null;
      
      if (hasDateRange) {
        // Historical mode
        await _fetchHistoricalData(symbol);
      } else {
        // Live mode
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
      final data = await _apiService.fetchLivePrices([symbol], false);
      
      setState(() {
        _isHistoricalMode = false;
        if (data.containsKey('prices') && data['prices'] is List) {
          final prices = data['prices'] as List;
          if (prices.isNotEmpty) {
            _result = {
              'mode': 'live',
              'data': prices[0],
              'timestamp': data['timestamp'],
            };
          } else {
            _error = 'No data found for $symbol';
          }
        } else {
          _error = 'Unexpected response format';
        }
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Error fetching live prices: $e';
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
          _error = 'No historical data found';
        }
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Error fetching historical data: $e';
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
              onPrimary: Colors.white,
              surface: Color(0xFF1E1E1E),
              onSurface: Colors.white,
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

  void _clearDates() {
    setState(() {
      _fromDate = null;
      _toDate = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0A0E27),
      appBar: AppBar(
        title: const Text("Price Verification Test"),
        backgroundColor: const Color(0xFF1A1F3A),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Info Card
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF1A1F3A),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.blueAccent.withOpacity(0.3)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.info_outline, color: Colors.blueAccent),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      _fromDate == null && _toDate == null
                          ? 'Live Mode: Enter symbol to get current prices'
                          : 'Historical Mode: Fetching price history for selected date range',
                      style: const TextStyle(color: Colors.white70, fontSize: 13),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Symbol Input
            TextField(
              controller: _symbolController,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                labelText: "Trading Symbol",
                labelStyle: const TextStyle(color: Colors.white70),
                hintText: "e.g. RELIANCE, INFY, TCS",
                hintStyle: const TextStyle(color: Colors.white38),
                prefixIcon: const Icon(Icons.search, color: Colors.blueAccent),
                filled: true,
                fillColor: const Color(0xFF1A1F3A),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide.none,
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide(color: Colors.white.withOpacity(0.1)),
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: Colors.blueAccent, width: 2),
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Timeframe Selector
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
              decoration: BoxDecoration(
                color: const Color(0xFF1A1F3A),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.white.withOpacity(0.1)),
              ),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  value: _selectedInterval,
                  isExpanded: true,
                  dropdownColor: const Color(0xFF1A1F3A),
                  style: const TextStyle(color: Colors.white, fontSize: 16),
                  icon: const Icon(Icons.arrow_drop_down, color: Colors.blueAccent),
                  items: _intervals.map((String interval) {
                    return DropdownMenuItem<String>(
                      value: interval,
                      child: Row(
                        children: [
                          const Icon(Icons.timeline, color: Colors.blueAccent, size: 20),
                          const SizedBox(width: 12),
                          Text('Interval: $interval'),
                        ],
                      ),
                    );
                  }).toList(),
                  onChanged: (String? newValue) {
                    if (newValue != null) {
                      setState(() {
                        _selectedInterval = newValue;
                      });
                    }
                  },
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Date Range Section
            Row(
              children: [
                Expanded(
                  child: _buildDateButton(
                    context,
                    label: 'From Date',
                    date: _fromDate,
                    onTap: () => _selectDate(context, true),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: _buildDateButton(
                    context,
                    label: 'To Date',
                    date: _toDate,
                    onTap: () => _selectDate(context, false),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Clear Dates Button
            if (_fromDate != null || _toDate != null)
              TextButton.icon(
                onPressed: _clearDates,
                icon: const Icon(Icons.clear, color: Colors.redAccent),
                label: const Text('Clear Dates (Switch to Live Mode)', 
                  style: TextStyle(color: Colors.redAccent)),
              ),
            const SizedBox(height: 24),

            // Additional Filters Section
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: const Color(0xFF2A2A2A),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.white10),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Additional Filters',
                    style: TextStyle(
                      color: Colors.white70,
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 16),
                  
                  // Instrument Type Dropdown
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E1E1E),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: Colors.white10),
                    ),
                    child: DropdownButtonHideUnderline(
                      child: DropdownButton<String>(
                        value: _instrumentType,
                        isExpanded: true,
                        dropdownColor: const Color(0xFF2A2A2A),
                        style: const TextStyle(color: Colors.white, fontSize: 14),
                        items: _instrumentTypes.map((String type) {
                          return DropdownMenuItem<String>(
                            value: type,
                            child: Text(type),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          if (newValue != null) {
                            setState(() {
                              _instrumentType = newValue;
                            });
                          }
                        },
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  
                  // Toggle Switches
                  _buildToggleSwitch(
                    label: 'Index Symbol',
                    value: _isIndexSymbol,
                    onChanged: (bool value) {
                      setState(() {
                        _isIndexSymbol = value;
                      });
                    },
                  ),
                  const SizedBox(height: 8),
                  _buildToggleSwitch(
                    label: 'Force Refresh',
                    value: _forceRefresh,
                    onChanged: (bool value) {
                      setState(() {
                        _forceRefresh = value;
                      });
                    },
                  ),
                  const SizedBox(height: 8),
                  _buildToggleSwitch(
                    label: 'Continuous',
                    value: _continuous,
                    onChanged: (bool value) {
                      setState(() {
                        _continuous = value;
                      });
                    },
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Fetch Button
            ElevatedButton(
              onPressed: _isLoading ? null : _fetchPrices,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.blueAccent,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                elevation: 0,
              ),
              child: _isLoading
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                      ),
                    )
                  : const Text(
                      'Get Prices',
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                    ),
            ),
            const SizedBox(height: 32),

            // Error Display
            if (_error != null)
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.redAccent.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.redAccent.withOpacity(0.5)),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.error_outline, color: Colors.redAccent),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        _error!,
                        style: const TextStyle(color: Colors.redAccent),
                      ),
                    ),
                  ],
                ),
              ),

            // Results Display
            if (_result != null) ...[
              if (_result!['mode'] == 'live')
                _buildLiveResults()
              else
                _buildHistoricalResults(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildDateButton(BuildContext context, {
    required String label,
    required DateTime? date,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: const Color(0xFF1A1F3A),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: date != null 
                ? Colors.blueAccent.withOpacity(0.5)
                : Colors.white.withOpacity(0.1),
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              label,
              style: const TextStyle(
                color: Colors.white70,
                fontSize: 12,
              ),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Icon(
                  Icons.calendar_today,
                  color: date != null ? Colors.blueAccent : Colors.white38,
                  size: 16,
                ),
                const SizedBox(width: 8),
                Text(
                  date != null
                      ? DateFormat('dd MMM yyyy').format(date)
                      : 'Select Date',
                  style: TextStyle(
                    color: date != null ? Colors.white : Colors.white38,
                    fontSize: 14,
                    fontWeight: date != null ? FontWeight.w500 : FontWeight.normal,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLiveResults() {
    final data = _result!['data'] as Map<String, dynamic>;
    
    return Container(
      margin: const EdgeInsets.only(top: 16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            const Color(0xFF1A1F3A),
            const Color(0xFF0F1729),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.blueAccent.withOpacity(0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: Colors.greenAccent.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: Colors.greenAccent.withOpacity(0.5)),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.circle, color: Colors.greenAccent, size: 8),
                    SizedBox(width: 6),
                    Text(
                      'LIVE',
                      style: TextStyle(
                        color: Colors.greenAccent,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
              const Spacer(),
              Text(
                _symbolController.text.toUpperCase(),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const Divider(color: Colors.white24, height: 32),
          ...data.entries.map((e) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 6),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  _formatKey(e.key),
                  style: const TextStyle(color: Colors.white70, fontSize: 14),
                ),
                Text(
                  _formatValue(e.value),
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.w600,
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          )),
        ],
      ),
    );
  }

  Widget _buildHistoricalResults() {
    final data = _result!['data'] as Map<String, dynamic>;
    final metadata = _result!['metadata'] as Map<String, dynamic>;
    
    // Extract data points for the symbol
    final symbolData = data[_symbolController.text.toUpperCase()] ?? 
                       data[_symbolController.text] ?? 
                       data.values.first;
    
    List<dynamic> dataPoints = [];
    if (symbolData is Map && symbolData.containsKey('dataPoints')) {
      dataPoints = symbolData['dataPoints'] as List;
    } else if (symbolData is List) {
      dataPoints = symbolData;
    }

    return Container(
      margin: const EdgeInsets.only(top: 16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            const Color(0xFF1A1F3A),
            const Color(0xFF0F1729),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.orangeAccent.withOpacity(0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: Colors.orangeAccent.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: Colors.orangeAccent.withOpacity(0.5)),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.history, color: Colors.orangeAccent, size: 12),
                    SizedBox(width: 6),
                    Text(
                      'HISTORICAL',
                      style: TextStyle(
                        color: Colors.orangeAccent,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
              const Spacer(),
              Text(
                _symbolController.text.toUpperCase(),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          
          // Metadata
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.05),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Column(
              children: [
                _buildMetadataRow('Period', '${metadata['from']} to ${metadata['to']}'),
                _buildMetadataRow('Interval', metadata['interval']),
                _buildMetadataRow('Data Points', '${dataPoints.length}'),
              ],
            ),
          ),
          const Divider(color: Colors.white24, height: 32),
          
          // Data Points Table Header
          Container(
            padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
            decoration: BoxDecoration(
              color: Colors.blueAccent.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Row(
              children: [
                Expanded(flex: 2, child: Text('Date/Time', style: TextStyle(color: Colors.white70, fontWeight: FontWeight.bold, fontSize: 12))),
                Expanded(child: Text('Open', style: TextStyle(color: Colors.white70, fontWeight: FontWeight.bold, fontSize: 12), textAlign: TextAlign.right)),
                Expanded(child: Text('High', style: TextStyle(color: Colors.white70, fontWeight: FontWeight.bold, fontSize: 12), textAlign: TextAlign.right)),
                Expanded(child: Text('Low', style: TextStyle(color: Colors.white70, fontWeight: FontWeight.bold, fontSize: 12), textAlign: TextAlign.right)),
                Expanded(child: Text('Close', style: TextStyle(color: Colors.white70, fontWeight: FontWeight.bold, fontSize: 12), textAlign: TextAlign.right)),
              ],
            ),
          ),
          const SizedBox(height: 8),
          
          // Data Points List (scrollable)
          Container(
            constraints: const BoxConstraints(maxHeight: 400),
            child: ListView.builder(
              shrinkWrap: true,
              itemCount: dataPoints.length,
              itemBuilder: (context, index) {
                final point = dataPoints[index];
                return Container(
                  padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                  margin: const EdgeInsets.only(bottom: 4),
                  decoration: BoxDecoration(
                    color: index % 2 == 0 
                        ? Colors.white.withOpacity(0.03)
                        : Colors.transparent,
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        flex: 2,
                        child: Text(
                          _formatTimestamp(point['timestamp'] ?? point['date'] ?? ''),
                          style: const TextStyle(color: Colors.white70, fontSize: 11),
                        ),
                      ),
                      Expanded(
                        child: Text(
                          _formatNumber(point['open']),
                          style: const TextStyle(color: Colors.white, fontSize: 11),
                          textAlign: TextAlign.right,
                        ),
                      ),
                      Expanded(
                        child: Text(
                          _formatNumber(point['high']),
                          style: const TextStyle(color: Colors.greenAccent, fontSize: 11),
                          textAlign: TextAlign.right,
                        ),
                      ),
                      Expanded(
                        child: Text(
                          _formatNumber(point['low']),
                          style: const TextStyle(color: Colors.redAccent, fontSize: 11),
                          textAlign: TextAlign.right,
                        ),
                      ),
                      Expanded(
                        child: Text(
                          _formatNumber(point['close']),
                          style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w600),
                          textAlign: TextAlign.right,
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMetadataRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Colors.white60, fontSize: 12)),
          Text(value, style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w500)),
        ],
      ),
    );
  }

  String _formatKey(String key) {
    // Convert camelCase to Title Case
    return key
        .replaceAllMapped(RegExp(r'([A-Z])'), (match) => ' ${match.group(1)}')
        .split(' ')
        .map((word) => word.isEmpty ? '' : word[0].toUpperCase() + word.substring(1))
        .join(' ')
        .trim();
  }

  String _formatValue(dynamic value) {
    if (value == null) return 'N/A';
    if (value is num) {
      return value.toStringAsFixed(2);
    }
    return value.toString();
  }

  String _formatNumber(dynamic value) {
    if (value == null) return '-';
    if (value is num) {
      return value.toStringAsFixed(2);
    }
    return value.toString();
  }

  String _formatTimestamp(dynamic timestamp) {
    if (timestamp == null) return '-';
    try {
      if (timestamp is int) {
        final date = DateTime.fromMillisecondsSinceEpoch(timestamp);
        return DateFormat('dd MMM HH:mm').format(date);
      }
      if (timestamp is String) {
        final date = DateTime.parse(timestamp);
        return DateFormat('dd MMM HH:mm').format(date);
      }
    } catch (e) {
      // Ignore parsing errors
    }
    return timestamp.toString();
  }

  Widget _buildToggleSwitch({
    required String label,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: const TextStyle(color: Colors.white70, fontSize: 14),
        ),
        Switch(
          value: value,
          onChanged: onChanged,
          activeColor: Colors.blueAccent,
          activeTrackColor: Colors.blueAccent.withOpacity(0.5),
        ),
      ],
    );
  }
}
