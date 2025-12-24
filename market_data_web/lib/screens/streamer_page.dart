

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
  String _exchangeSegment = 'NSE_EQ';
  bool _autoPrefix = true;
  final TextEditingController _symbolsController = TextEditingController();
  final TextEditingController _searchInputController = TextEditingController();
  
  // Live Data State
  List<Map<String, dynamic>> _feedHistory = []; // List of latest 10 updates
  Map<String, dynamic> _quotes = {}; // Keep latest quote for lookups if needed, but feed shows history
  List<String> _logs = [];
  bool _isStreaming = false;

  // Search State
  List<Map<String, dynamic>> _searchResults = [];
  bool _isSearching = false;

// ... existing initState ...
  @override
  void initState() {
    super.initState();
    AppLogger.info("StreamerPage.initState", "Initializing StreamerPage");
    _streamService.connect();
    
    // Listen to stream
    _streamService.stream.listen((message) {
       if (message.containsKey('quotes')) {
         try {
           setState(() {
             final newQuotes = message['quotes'] as Map<String, dynamic>;
             // Reduced logging noise, only log summary
             // _log("Received ${newQuotes.length} updates", method: "StreamerPage.streamListener");
             // Actually, the user LIKES the log in the UI.
             
             final now = DateTime.now(); 
           
              // Add each new quote to history
             newQuotes.forEach((key, val) {
               val['timestamp'] = now;
               val['symbol'] = key; 
               
               _feedHistory.insert(0, val);
               _quotes[key] = val; 
               
               Provider.of<MarketProvider>(context, listen: false).updateLivePrice(val);
             });
  
             if (_feedHistory.length > 10) {
               _feedHistory = _feedHistory.sublist(0, 10);
             }
           });
         } catch (e) {
           _log("Error processing update: $e", method: "StreamerPage.streamListener", level: LogLevel.error);
         }
       }
    });

    _log("Streamer Page Initialized", method: "StreamerPage.initState");
  }

  @override
  void dispose() {
    _streamService.dispose();
    _symbolsController.dispose();
    _searchInputController.dispose();
    super.dispose();
  }

  void _log(String msg, {String method = 'StreamerPage', LogLevel level = LogLevel.info}) {
    setState(() {
      _logs.insert(0, "[${DateFormat('HH:mm:ss').format(DateTime.now())}] $msg");
      if (_logs.length > 50) _logs.removeLast();
    });
    AppLogger.log(level: level, tag: method, message: msg);
  }

// ... existing helper methods ...
// I will do a MultiReplace to target specific methods to add "method" parameter to _log calls.

// Wait, I can't redefine `_log` without replacing the whole file or using partials.
// I will use replace_file_content to update imports and `_log` definition and `initState`.


  // --- Actions ---

  Future<void> _getLoginUrl() async {
    // ... items omitted for brevity if unchanged, but full replacement required for ReplaceFileContent ...
    // To be safe, I will include the full methods to avoid disjoint replace error if possible or use careful range.
    // However, I am replacing the TOP part of the file mostly.
    // But the DataTable is at the bottom. I should probably do 2 replacements or replace the whole file?
    // ReplaceFileContent must be contiguous.
    // I will replace from start of class state variables down to end of init state to cover the state changes.
    // Then I will do a separate replace for the DataTable part.
    // Actually, I can combine if I include everything between.
    // But better to do 2 chunks with MultiReplaceFileContent.
    
    // BUT wait, this tool is ReplaceFileContent (Single). I should use MultiReplaceFileContent!
    // No, I am strictly instructed to use MultiReplaceFileContent for non-contiguous edits.
    // I will use `multi_replace_file_content` tool.
    
    final url = await _apiService.getLoginUrl(_provider);
    if (url != null) {
      _log("Login URL generated: $url");
      // Open URL? For now just show in log or dialog
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

    // Apply Auto-prefix
    if (_autoPrefix && _exchangeSegment != 'None') {
      symbols = symbols.map((s) {
        if (s.contains('|')) return s; // Already has exchange
        return "$_exchangeSegment|$s";
      }).toList();
    }

    _log("Starting stream for ${symbols.length} symbols: ${symbols.join(', ')}", method: "StreamerPage._startStream");
    final success = await _apiService.connectStream(symbols, _provider);
    if (success) {
      setState(() => _isStreaming = true);
      _log("Stream Connect Request Sent: OK", method: "StreamerPage._startStream");
    } else {
      _log("Stream Connect Failed", method: "StreamerPage._startStream", level: LogLevel.error);
    }
  }

  Future<void> _stopStream() async {
    _log("Stopping stream request...", method: "StreamerPage._stopStream");
    final success = await _apiService.disconnectStream(_provider);
    if (success) {
       setState(() => _isStreaming = false);
       _log("Stream Stop Request Sent", method: "StreamerPage._stopStream");
    } else {
      _log("Stop Stream Failed", method: "StreamerPage._stopStream", level: LogLevel.error);
    }
  }

  Future<void> _search() async {
    final query = _searchInputController.text;
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
    return Scaffold(
      appBar: AppBar(title: const Text("Market Data Streamer"), backgroundColor: const Color(0xFF1E1E2E)),
      body: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Left Panel: Configuration
          Container(
            width: 350,
            padding: const EdgeInsets.all(16),
            color: const Color(0xFF1E1E2E), // Dark background
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text("Configuration", style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.white)),
                const SizedBox(height: 20),
                
                // Auth Provider
                const Text("Auth Provider", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
                const SizedBox(height: 5),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  decoration: BoxDecoration(border: Border.all(color: Colors.grey.shade600), borderRadius: BorderRadius.circular(4)),
                  child: DropdownButton<String>(
                    value: _provider,
                    isExpanded: true,
                    dropdownColor: const Color(0xFF2E2E3E),
                    underline: const SizedBox(),
                    style: const TextStyle(color: Colors.white),
                    items: ['UPSTOX', 'ZERODHA'].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
                    onChanged: (val) => setState(() => _provider = val!),
                  ),
                ),
                const SizedBox(height: 10),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton(
                    onPressed: _getLoginUrl,
                    style: OutlinedButton.styleFrom(foregroundColor: Colors.white, side: const BorderSide(color: Colors.white)),
                    child: const Text("Login & Get Token"),
                  ),
                ),
                
                const SizedBox(height: 20),
                
                // Exchange Segment
                const Text("Exchange Segment", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
                const SizedBox(height: 5),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  decoration: BoxDecoration(border: Border.all(color: Colors.grey.shade600), borderRadius: BorderRadius.circular(4)),
                  child: DropdownButton<String>(
                    value: _exchangeSegment,
                    isExpanded: true,
                    dropdownColor: const Color(0xFF2E2E3E),
                    underline: const SizedBox(),
                    style: const TextStyle(color: Colors.white),
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
                      checkColor: Colors.black,
                      fillColor: MaterialStateProperty.all(Colors.white),
                    ),
                    const Text("Auto-prefix Exchange?", style: TextStyle(color: Colors.white)),
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
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(4)),
                    filled: true,
                    fillColor: const Color(0xFF2E2E3E),
                  ),
                  maxLines: 3,
                  style: const TextStyle(color: Colors.white),
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
                  color: Colors.black,
                  padding: const EdgeInsets.all(8),
                  child: ListView.builder(
                    reverse: true,
                    itemCount: _logs.length,
                    itemBuilder: (ctx, i) => Text(_logs[i], style: const TextStyle(color: Colors.greenAccent, fontSize: 10, fontFamily: 'monospace')),
                  ),
                )
              ],
            ),
          ),
          
          // Right Panel: Search & Feed
          Expanded(
            child: Container(
              color: Colors.white,
               padding: const EdgeInsets.all(24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Search Header
                  Row(
                    children: const [
                       Icon(Icons.search, size: 28, color: Colors.blueAccent),
                       SizedBox(width: 8),
                       Text("Instrument Search", style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.black87)),
                    ],
                  ),
                  const SizedBox(height: 20),
                  
                  // Search Bar
                  TextField(
                    controller: _searchInputController,
                    decoration: InputDecoration(
                      hintText: "Search Symbol (e.g. Reliance, Nifty Bank)...",
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(4)),
                      filled: true,
                      fillColor: Colors.white,
                    ),
                    style: const TextStyle(color: Colors.black87),
                  ),
                  const SizedBox(height: 10),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _search,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blueAccent,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                      child: const Text("Search", style: TextStyle(fontWeight: FontWeight.bold)),
                    ),
                  ),
                  
                  if (_isSearching) const Padding(padding: EdgeInsets.only(top: 10), child: LinearProgressIndicator()),

                  const SizedBox(height: 20),
                  
                   // Results / Placeholder
                  if (_searchResults.isEmpty && _feedHistory.isEmpty && _quotes.isEmpty)
                     const Padding(
                       padding: EdgeInsets.all(32.0),
                       child: Center(
                         child: Text("Enter a query to find instruments.", style: TextStyle(fontSize: 16, color: Colors.grey)),
                       ),
                     ),

                  // Search Results List
                  if (_searchResults.isNotEmpty)
                    Expanded(
                      flex: 1,
                      child: Container(
                        decoration: BoxDecoration(
                          color: const Color(0xFF2E2E3E).withOpacity(0.9),
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.white10),
                          boxShadow: [
                            BoxShadow(color: Colors.black.withOpacity(0.3), blurRadius: 10, offset: const Offset(0, 4)),
                          ],
                        ),
                        child: ListView.builder(
                          itemCount: _searchResults.length,
                          itemBuilder: (context, index) {
                            final item = _searchResults[index];
                            final symbol = item['tradingSymbol'] ?? item['symbol'] ?? 'Unknown';
                            final key = item['instrumentKey'] ?? symbol;
                            final name = item['name'] ?? '';
                            final exchange = item['exchange'] ?? '';
                            
                            return ListTile(
                              title: Text(symbol, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
                              subtitle: Text("$name ($exchange)", style: const TextStyle(color: Colors.grey)),
                              trailing: IconButton(
                                icon: const Icon(Icons.add_circle_outline, color: Colors.blueAccent), 
                                onPressed: () => _addSymbol(key)
                              ),
                              onTap: () => _addSymbol(key),
                            );
                          },
                        ),
                      ),
                    ),
                    
                   if (_searchResults.isNotEmpty) const SizedBox(height: 20),
                   
                   // Live Data Feed (If streaming or has data)
                   if (_feedHistory.isNotEmpty || _quotes.isNotEmpty) ...[
                      const Row(
                         children: [
                            Icon(Icons.monitor_heart, color: Colors.blueAccent),
                             SizedBox(width: 8),
                            Text("Live Feed (Last 10 Updates)", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white)),
                         ],
                      ),
                      const SizedBox(height: 10),
                      Expanded(
                        flex: 2,
                        child: Container(
                           decoration: BoxDecoration(
                             gradient: LinearGradient(
                               colors: [const Color(0xFF2E2E3E), const Color(0xFF252535)],
                               begin: Alignment.topLeft,
                               end: Alignment.bottomRight,
                             ),
                             borderRadius: BorderRadius.circular(12),
                             boxShadow: [
                               BoxShadow(color: Colors.black45, blurRadius: 12, offset: const Offset(0, 6)),
                             ],
                             border: Border.all(color: Colors.white10),
                           ),
                           child: ClipRRect(
                             borderRadius: BorderRadius.circular(12),
                             child: SingleChildScrollView(
                                scrollDirection: Axis.vertical,
                                child: Theme(
                                  data: Theme.of(context).copyWith(dividerColor: Colors.white10),
                                  child: DataTable(
                                    headingRowColor: MaterialStateProperty.all(Colors.black26),
                                    dataRowColor: MaterialStateProperty.all(Colors.transparent),
                                    columnSpacing: 20,
                                    headingTextStyle: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white70),
                                    dataTextStyle: const TextStyle(color: Colors.white),
                                    columns: const [
                                      DataColumn(label: Text('Time')),
                                      DataColumn(label: Text('Symbol')),
                                      DataColumn(label: Text('LTP')),
                                      DataColumn(label: Text('Change')),
                                      DataColumn(label: Text('% Change')),
                                      DataColumn(label: Text('Prev Close')), // New Column
                                    ],
                                    rows: _feedHistory.map((data) {
                                      final key = data['symbol'] ?? 'UNKNOWN';
                                      final ltp = (data['lastPrice'] as num?)?.toDouble() ?? 0.0;
                                      final change = (data['change'] as num?)?.toDouble() ?? 0.0;
                                      final pChange = (data['changePercent'] as num?)?.toDouble() ?? 0.0;
                                      final color = change >= 0 ? const Color(0xFF4CAF50) : const Color(0xFFEF5350); // Muted Green/Red
                                      final time = data['timestamp'] as DateTime? ?? DateTime.now();
                                      final prevClose = ltp - change;

                                      return DataRow(
                                        cells: [
                                          DataCell(Text(DateFormat('HH:mm:ss').format(time), style: const TextStyle(color: Colors.grey))),
                                          DataCell(Text(key, style: const TextStyle(fontWeight: FontWeight.bold))),
                                          DataCell(Text(ltp.toStringAsFixed(2), style: const TextStyle(fontWeight: FontWeight.w600))),
                                          DataCell(Text(change.toStringAsFixed(2), style: TextStyle(color: color, fontWeight: FontWeight.bold))),
                                          DataCell(Text('${pChange.toStringAsFixed(2)}%', style: TextStyle(color: color, fontWeight: FontWeight.bold))),
                                          DataCell(Text(prevClose.toStringAsFixed(2), style: const TextStyle(color: Colors.white70))),
                                        ],
                                      );
                                    }).toList(),
                                  ),
                                ),
                             ),
                           ),
                        ),
                      ),
                   ]
                ],
              ),
            ),
          )
        ],
      ),
    );
  }
}
