
import 'dart:async';
import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../services/stream_service.dart';
import 'package:intl/intl.dart';

import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../utils/app_logger.dart';

class StreamerPage extends StatefulWidget {
  const StreamerPage({super.key});

  @override
  State<StreamerPage> createState() => _StreamerPageState();
}

class _StreamerPageState extends State<StreamerPage> {
  final ApiService _apiService = ApiService();
  final StreamService _streamService = StreamService();
  
  // Config State
  String _provider = 'UPSTOX'; // UPSTOX, ZERODHA
  String _exchangeSegment = 'None'; 
  bool _autoPrefix = true;
  bool _isIndexSymbol = false;
  final TextEditingController _symbolsController = TextEditingController(text: 'NIFTY 50'); 
  final TextEditingController _searchController = TextEditingController();
  
  // Live Data State
  List<Map<String, dynamic>> _feedHistory = []; 
  Map<String, dynamic> _quotes = {}; 
  List<String> _logs = [];
  bool _isStreaming = false;

  // Pagination State
  int _currentPage = 0;
  final int _itemsPerPage = 100;

  // Search State
  List<Map<String, dynamic>> _searchResults = [];
  bool _isSearching = false;
  
  StreamSubscription? _subscription;
  MarketProvider? _marketProvider; 

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    try {
      _marketProvider = Provider.of<MarketProvider>(context, listen: false);
    } catch (e) {
      _log("didChangeDependencies Error: $e", method: "StreamerPage.didChangeDependencies", level: LogLevel.error);
    }
  }

  @override
  void initState() {
    super.initState();
    _streamService.connect();
    
    // Listen to stream
    _subscription = _streamService.stream.listen((message) {
       if (!mounted) return; 
       
       if (message.containsKey('quotes')) {
         try {
           final provider = _marketProvider;
           final bool hasProvider = provider != null;

           setState(() {
             final newQuotes = message['quotes'] as Map<String, dynamic>;
             final now = DateTime.now(); 
           
             newQuotes.forEach((key, val) {
               val['timestamp'] = now;
               val['symbol'] = key; 
               
               _feedHistory.insert(0, val);
               _quotes[key] = val; 
               
               if (mounted && hasProvider) {
                  try {
                    provider!.updateLivePrice(val);
                  } catch (e) {
                    // Suppress
                  }
               }
             });
              
             if (_feedHistory.length > 500) {
               _feedHistory = _feedHistory.sublist(0, 500);
             }
           });
         } catch (e) {
           _log("Error processing update: $e", method: "StreamerPage.streamListener", level: LogLevel.error);
         }
       }
    });
  }

  @override
  void dispose() {
    _subscription?.cancel();
    _streamService.dispose();
    _symbolsController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  void _log(String msg, {String method = 'StreamerPage', LogLevel level = LogLevel.info}) {
    if (mounted) {
      setState(() {
        _logs.insert(0, "[${DateFormat('HH:mm:ss').format(DateTime.now())}] $msg");
        if (_logs.length > 50) _logs.removeLast();
      });
    }
    AppLogger.log(level: level, tag: method, message: msg);
  }

  // --- Actions ---

  Future<void> _getLoginUrl() async {
    final url = await _apiService.getLoginUrl(_provider);
    if (url != null) {
      _log("Login URL generated: $url");
      if (!mounted) return;
       showDialog(
        context: context,
        builder: (ctx) => AlertDialog(
          title: const Text("Login URL"),
          content: SelectableText(url),
          actions: [TextButton(onPressed: () => Navigator.pop(ctx), child: const Text("Close"))],
        ),
      );
    } else {
      _log("Failed to get Login URL");
    }
  }

  Future<void> _startStream() async {
    final raw = _symbolsController.text;
    if (raw.isEmpty) {
      _log("Please enter symbols", method: "StreamerPage._startStream", level: LogLevel.warning);
      return;
    }
    
    List<String> symbols = raw.split(',').map((e) => e.trim().toUpperCase()).where((e) => e.isNotEmpty).toList();
    if (symbols.isEmpty) return;

    if (_autoPrefix && _exchangeSegment != 'None') {
      symbols = symbols.map((s) {
        if (s.contains('|')) return s; 
        return "$_exchangeSegment|$s";
      }).toList();
    }

    final success = await _apiService.connectStream(symbols, _provider, isIndexSymbol: _isIndexSymbol);
    if (success) {
      setState(() => _isStreaming = true);
      _log("Stream Connect Request Sent: OK", method: "StreamerPage._startStream");
    } else {
      _log("Stream Connect Failed", method: "StreamerPage._startStream", level: LogLevel.error);
    }
  }

  Future<void> _stopStream() async {
    final success = await _apiService.disconnectStream(_provider);
    if (success) {
       setState(() => _isStreaming = false);
       _log("Stream Stop Request Sent", method: "StreamerPage._stopStream");
    } else {
      _log("Stop Stream Failed", method: "StreamerPage._stopStream", level: LogLevel.error);
    }
  }

  Future<void> _search() async {
    final query = _searchController.text;
    if (query.isEmpty) return;

    setState(() => _isSearching = true);
    final results = await _apiService.searchInstruments(query, _provider);
    setState(() {
      _searchResults = results;
      _isSearching = false;
    });
    _log("Found ${results.length} instruments for '$query'", method: "StreamerPage._search");
  }

  void _addSymbol(String symbol) {
    final current = _symbolsController.text;
    if (current.isNotEmpty && !current.endsWith(',')) {
      _symbolsController.text = "$current, $symbol";
    } else {
      _symbolsController.text = "$current$symbol";
    }
    _log("Added $symbol");
  }

  @override
  Widget build(BuildContext context) {
    // White Theme Overrides
    // Using global theme from main.dart
    return Scaffold(
        appBar: AppBar(
            title: const Text("Market Data Streamer", style: TextStyle(color: Colors.black87, fontWeight: FontWeight.bold)), 
            backgroundColor: Colors.white,
            elevation: 0,
            iconTheme: const IconThemeData(color: Colors.black87),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildConfigPanel(),
              const SizedBox(width: 24),
              Expanded(child: _buildRightPanel()),
            ],
          ),
        ),
      );
  }

  // --- UI Helper Methods ---

  Widget _buildConfigPanel() {
    return Container(
      width: 350,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
           BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
        ]
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text("Configuration", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.black87)),
          const SizedBox(height: 20),
          
          // Auth Provider
          const Text("Auth Provider", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
          const SizedBox(height: 5),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(border: Border.all(color: Colors.grey.shade300), borderRadius: BorderRadius.circular(8)),
            child: DropdownButton<String>(
              value: _provider,
              isExpanded: true,
              underline: const SizedBox(),
              style: const TextStyle(color: Colors.black87),
              items: ['UPSTOX', 'ZERODHA'].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
              onChanged: (val) => setState(() => _provider = val!),
            ),
          ),
          const SizedBox(height: 10),
          SizedBox(
            width: double.infinity,
            child: OutlinedButton(
              onPressed: _getLoginUrl,
              style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.blueAccent, 
                  side: const BorderSide(color: Colors.blueAccent),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8))
              ),
              child: const Text("Login & Get Token"),
            ),
          ),
          
          const SizedBox(height: 20),
          
          // Exchange Segment
          const Text("Exchange Segment", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
          const SizedBox(height: 5),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(border: Border.all(color: Colors.grey.shade300), borderRadius: BorderRadius.circular(8)),
            child: DropdownButton<String>(
              value: _exchangeSegment,
              isExpanded: true,
              underline: const SizedBox(),
              style: const TextStyle(color: Colors.black87),
              items: [
                'NSE_EQ', 'NFO', 'CDS', 'MCX', 'BSE_EQ', 'BSE_FO', 'None'
              ].map((e) => DropdownMenuItem(value: e, child: Text(e == 'NSE_EQ' ? 'NSE Equity (NSE_EQ)' : e))).toList(),
              onChanged: (val) => setState(() => _exchangeSegment = val!),
            ),
          ),
          
          const SizedBox(height: 10),
          Row(
            children: [
              Checkbox(
                value: _autoPrefix, 
                onChanged: (val) => setState(() => _autoPrefix = val!),
                activeColor: Colors.blueAccent,
              ),
              const Text("Auto-prefix Exchange?", style: TextStyle(color: Colors.black87)),
            ],
          ),
          
          Row(
            children: [
              Checkbox(
                value: _isIndexSymbol, 
                onChanged: (val) => setState(() => _isIndexSymbol = val!),
                activeColor: Colors.blueAccent,
              ),
              const Text("Is Index Symbol?", style: TextStyle(color: Colors.black87)),
            ],
          ),
          
          const SizedBox(height: 10),
          const Text("Symbols (Comma Separated)", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
          const SizedBox(height: 5),
          TextField(
            controller: _symbolsController,
            decoration: InputDecoration(
              hintText: "e.g. INFY, RELIANCE, TCS",
              hintStyle: const TextStyle(color: Colors.grey),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Colors.grey.shade300)),
              enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Colors.grey.shade300)),
              filled: true,
              fillColor: Colors.grey.shade50,
            ),
            maxLines: 3,
            style: const TextStyle(color: Colors.black87),
          ),
          const Text("Enter symbols without exchange if Auto-prefix is on.", style: TextStyle(fontSize: 10, color: Colors.grey)),
          
          const SizedBox(height: 20),
          
          Row(
            children: [
              Expanded(
                child: ElevatedButton(
                  onPressed: _startStream,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blueAccent,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  child: const Text("Start Stream"),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: ElevatedButton(
                  onPressed: _stopStream,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.redAccent,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  child: const Text("Stop"),
                ),
              ),
            ],
          ),
          
          const Spacer(),
          const Text("System Logs", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
          const SizedBox(height: 5),
          Container(
            height: 150,
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
                color: Colors.grey.shade900,
                borderRadius: BorderRadius.circular(8)
            ),
            child: ListView.builder(
              reverse: true,
              itemCount: _logs.length,
              itemBuilder: (ctx, i) => Text(_logs[i], style: const TextStyle(color: Colors.greenAccent, fontSize: 10, fontFamily: 'monospace')),
            ),
          )
        ],
      ),
    );
  }

  Widget _buildRightPanel() {
    return Container(
      // Transparent logic container, layout managed by children
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSearchSection(),
          if (_isSearching) const Padding(padding: EdgeInsets.only(top: 10), child: LinearProgressIndicator()),
          const SizedBox(height: 20),
          Expanded(
            child: Column(
              children: [
                if (_searchResults.isNotEmpty) Expanded(child: _buildSearchResults()),
                if (_searchResults.isEmpty && _feedHistory.isEmpty && _quotes.isEmpty)
                   const Padding(
                     padding: EdgeInsets.all(32.0),
                     child: Center(
                       child: Text("Enter a query to find instruments or start a stream.", style: TextStyle(fontSize: 16, color: Colors.grey)),
                     ),
                   ),
                if (_feedHistory.isNotEmpty || _quotes.isNotEmpty) 
                  Expanded(flex: 2, child: _buildLiveFeedSection()),
              ],
            ),
          )
        ],
      ),
    );
  }

  Widget _buildSearchSection() {
    return Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            boxShadow: [
              BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
            ]
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: const [
                 Icon(Icons.search, size: 28, color: Colors.blueAccent),
                 SizedBox(width: 8),
                 Text("Instrument Search", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.black87)),
              ],
            ),
            const SizedBox(height: 20),
            Row(
                children: [
                    Expanded(
                        child: TextField(
                          controller: _searchController,
                          decoration: InputDecoration(
                            hintText: "Search Symbol (e.g. Reliance, Nifty Bank)...",
                            border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Colors.grey.shade300)),
                            enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Colors.grey.shade300)),
                            filled: true,
                            fillColor: Colors.grey.shade50,
                          ),
                          style: const TextStyle(color: Colors.black87),
                        ),
                    ),
                    const SizedBox(width: 10),
                    ElevatedButton(
                        onPressed: _search,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blueAccent,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 32),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                        ),
                        child: const Text("Search", style: TextStyle(fontWeight: FontWeight.bold)),
                      ),
                ],
            )
          ],
        ),
    );
  }

  Widget _buildSearchResults() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
        ],
      ),
      child: ListView.separated(
        padding: const EdgeInsets.all(8),
        separatorBuilder: (ctx, i) => const Divider(),
        itemCount: _searchResults.length,
        itemBuilder: (context, index) {
          final item = _searchResults[index];
          final symbol = item['tradingSymbol'] ?? item['symbol'] ?? 'Unknown';
          final key = item['instrumentKey'] ?? symbol;
          final name = item['name'] ?? '';
          final exchange = item['exchange'] ?? '';
          
          return ListTile(
            title: Text(symbol, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87)),
            subtitle: Text("$name ($exchange)", style: const TextStyle(color: Colors.grey)),
            trailing: IconButton(
              icon: const Icon(Icons.add_circle_outline, color: Colors.blueAccent), 
              onPressed: () => _addSymbol(key)
            ),
            onTap: () => _addSymbol(key),
          );
        },
      ),
    );
  }

  Widget _buildLiveFeedSection() {
    final startIndex = _currentPage * _itemsPerPage;
    final endIndex = (startIndex + _itemsPerPage < _feedHistory.length) 
        ? startIndex + _itemsPerPage 
        : _feedHistory.length;
    
    final currentItems = _feedHistory.sublist(startIndex, endIndex);
    final totalPages = (_feedHistory.length / _itemsPerPage).ceil();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (_searchResults.isNotEmpty) const SizedBox(height: 20),
        Row(
           mainAxisAlignment: MainAxisAlignment.spaceBetween,
           children: [
              const Row(
                children: [
                    Icon(Icons.monitor_heart, color: Colors.blueAccent),
                    SizedBox(width: 8),
                    Text("Live Feed", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.black87)),
                ],
              ),
              if (_feedHistory.isNotEmpty)
                Row(
                  children: [
                    IconButton(
                      icon: const Icon(Icons.first_page, color: Colors.black54),
                      onPressed: _currentPage > 0 ? () => setState(() => _currentPage = 0) : null,
                    ),
                    IconButton(
                      icon: const Icon(Icons.chevron_left, color: Colors.black54),
                      onPressed: _currentPage > 0 ? () => setState(() => _currentPage--) : null,
                    ),
                    Text(
                      "Page ${_currentPage + 1} / ${totalPages == 0 ? 1 : totalPages} (${_feedHistory.length} items)", 
                      style: const TextStyle(color: Colors.black87, fontSize: 12)
                    ),
                    IconButton(
                      icon: const Icon(Icons.chevron_right, color: Colors.black54),
                      onPressed: _currentPage < totalPages - 1 ? () => setState(() => _currentPage++) : null,
                    ),
                    IconButton(
                      icon: const Icon(Icons.last_page, color: Colors.black54),
                      onPressed: _currentPage < totalPages - 1 ? () => setState(() => _currentPage = totalPages - 1) : null,
                    ),
                  ],
                ),
           ],
        ),
        const SizedBox(height: 10),
        Expanded(
          child: Container(
             width: double.infinity,
             decoration: BoxDecoration(
               color: Colors.white,
               borderRadius: BorderRadius.circular(16),
               boxShadow: [
                 BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
               ],
             ),
             child: ClipRRect(
               borderRadius: BorderRadius.circular(16),
               child: SingleChildScrollView(
                  scrollDirection: Axis.vertical,
                  child: Theme(
                    data: Theme.of(context).copyWith(dividerColor: Colors.grey.shade100),
                    child: SizedBox(
                      width: double.infinity,
                      child: DataTable(
                        headingRowColor: MaterialStateProperty.all(Colors.grey.shade100),
                        dataRowColor: MaterialStateProperty.all(Colors.white),
                        columnSpacing: 20,
                        headingTextStyle: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87),
                        dataTextStyle: const TextStyle(color: Colors.black87),
                        columns: const [
                          DataColumn(label: Text('Time')),
                          DataColumn(label: Text('Symbol')),
                          DataColumn(label: Text('LTP')),
                          DataColumn(label: Text('Change')),
                          DataColumn(label: Text('% Change')),
                          DataColumn(label: Text('Prev Close')),
                        ],
                        rows: currentItems.map((data) {
                          final key = data['symbol'] ?? 'UNKNOWN';
                          final ltp = (data['lastPrice'] as num?)?.toDouble() ?? 0.0;
                          final change = (data['change'] as num?)?.toDouble() ?? 0.0;
                          final pChange = (data['changePercent'] as num?)?.toDouble() ?? 0.0;
                          final color = change >= 0 ? Colors.green : Colors.red;
                          final time = data['timestamp'] as DateTime? ?? DateTime.now();
                          final prevClose = ltp - change;
                          
                          // Improve Time Visibility
                          final timeStr = DateFormat('HH:mm:ss').format(time);

                          return DataRow(
                            cells: [
                              DataCell(Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                decoration: BoxDecoration(
                                  color: Colors.blueAccent.withOpacity(0.1), 
                                  borderRadius: BorderRadius.circular(4),
                                  border: Border.all(color: Colors.blueAccent.withOpacity(0.3)),
                                ),
                                child: Text(timeStr, style: const TextStyle(color: Colors.blueAccent, fontWeight: FontWeight.bold, fontFamily: 'monospace')),
                              )),
                              DataCell(Text(key, style: const TextStyle(fontWeight: FontWeight.bold))),
                              DataCell(Text(ltp.toStringAsFixed(2), style: const TextStyle(fontWeight: FontWeight.w600))),
                              DataCell(Text(change.toStringAsFixed(2), style: TextStyle(color: color, fontWeight: FontWeight.bold))),
                              DataCell(Text('${pChange.toStringAsFixed(2)}%', style: TextStyle(color: color, fontWeight: FontWeight.bold))),
                              DataCell(Text(prevClose.toStringAsFixed(2), style: const TextStyle(color: Colors.grey))),
                            ],
                          );
                        }).toList(),
                      ),
                    ),
                  ),
               ),
             ),
          ),
        ),
      ],
    );
  }
}
