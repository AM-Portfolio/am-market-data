import 'dart:async';
import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../services/stream_service.dart';
import 'package:intl/intl.dart';

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
  final TextEditingController _symbolsController = TextEditingController();
  final TextEditingController _searchInputController = TextEditingController();
  
  // Live Data State
  Map<String, dynamic> _quotes = {}; // Symbol -> Data
  List<String> _logs = [];
  bool _isStreaming = false;

  // Search State
  List<Map<String, dynamic>> _searchResults = [];
  bool _isSearching = false;

  @override
  void initState() {
    super.initState();
    _streamService.connect();
    
    // Listen to stream
    _streamService.stream.listen((message) {
       if (message.containsKey('quotes')) {
         setState(() {
           final newQuotes = message['quotes'] as Map<String, dynamic>;
           // Merge or Replace? Usually replace the latest for that symbol
           newQuotes.forEach((key, val) {
              _quotes[key] = val;
           });
         });
       }
    });

    _log("Streamer Page Initialized");
  }

  @override
  void dispose() {
    _streamService.dispose();
    _symbolsController.dispose();
    _searchInputController.dispose();
    super.dispose();
  }

  void _log(String msg) {
    setState(() {
      _logs.insert(0, "[${DateFormat('HH:mm:ss').format(DateTime.now())}] $msg");
      if (_logs.length > 50) _logs.removeLast();
    });
  }

  // --- Actions ---

  Future<void> _getLoginUrl() async {
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
      _log("Please enter symbols");
      return;
    }
    
    final symbols = raw.split(',').map((e) => e.trim().toUpperCase()).where((e) => e.isNotEmpty).toList();
    if (symbols.isEmpty) return;

    _log("Starting stream for: ${symbols.join(', ')}");
    final success = await _apiService.connectStream(symbols, _provider);
    if (success) {
      setState(() => _isStreaming = true);
      _log("Stream Connect Request Sent: OK");
    } else {
      _log("Stream Connect Failed");
    }
  }

  Future<void> _stopStream() async {
    final success = await _apiService.disconnectStream(_provider);
    if (success) {
       setState(() => _isStreaming = false);
       _log("Stream Stop Request Sent");
    } else {
      _log("Stop Stream Failed");
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
    _log("Found ${results.length} instruments for '$query'");
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
      appBar: AppBar(title: const Text("Streamer Config")),
      body: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Left Panel: Config & Search
          Container(
            width: 400,
            padding: const EdgeInsets.all(16),
            color: Colors.black12,
            child: ListView(
              children: [
                // Config Card
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text("Configuration", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 10),
                        DropdownButton<String>(
                          value: _provider,
                          isExpanded: true,
                          items: ['UPSTOX', 'ZERODHA'].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
                          onChanged: (val) => setState(() => _provider = val!),
                        ),
                        const SizedBox(height: 10),
                        ElevatedButton(onPressed: _getLoginUrl, child: const Text("Get Login URL")),
                        const Divider(),
                        TextField(
                          controller: _symbolsController,
                          decoration: const InputDecoration(labelText: "Symbols (Comma Separated)", hintText: "INFY, TCS"),
                          maxLines: 3,
                        ),
                        const SizedBox(height: 10),
                        Row(
                          children: [
                            ElevatedButton(
                              onPressed: _startStream,
                              style: ElevatedButton.styleFrom(backgroundColor: Colors.green),
                              child: const Text("Start Stream", style: TextStyle(color: Colors.white)),
                            ),
                            const SizedBox(width: 10),
                            ElevatedButton(
                              onPressed: _stopStream,
                              style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                              child: const Text("Stop", style: TextStyle(color: Colors.white)),
                            ),
                          ],
                        )
                      ],
                    ),
                  ),
                ),
                
                const SizedBox(height: 20),

                // Search Card
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text("Instrument Search", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 10),
                        Row(
                          children: [
                            Expanded(child: TextField(controller: _searchInputController, decoration: const InputDecoration(hintText: "Search Symbol..."))),
                            IconButton(onPressed: _search, icon: const Icon(Icons.search)),
                          ],
                        ),
                        if (_isSearching) const LinearProgressIndicator(),
                        const SizedBox(height: 10),
                        Container(
                          height: 200,
                          color: Colors.black12,
                          child: ListView.builder(
                            itemCount: _searchResults.length,
                            itemBuilder: (context, index) {
                              final item = _searchResults[index];
                              final symbol = item['tradingSymbol'] ?? item['symbol'] ?? 'Unknown';
                              final key = item['instrumentKey'] ?? symbol;
                              return ListTile(
                                title: Text(symbol, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                                subtitle: Text("${item['name'] ?? ''} (${item['exchange'] ?? ''})", style: const TextStyle(fontSize: 10)),
                                trailing: const Icon(Icons.add, size: 16),
                                dense: true,
                                onTap: () => _addSymbol(key),
                              );
                            },
                          ),
                        )
                      ],
                    ),
                  ),
                ),

                const SizedBox(height: 20),
                
                // Logs
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                         const Text("System Logs", style: TextStyle(fontWeight: FontWeight.bold)),
                         Container(
                           height: 150,
                           color: Colors.black,
                           padding: const EdgeInsets.all(8),
                           child: ListView.builder(
                             itemCount: _logs.length,
                             itemBuilder: (ctx, i) => Text(_logs[i], style: const TextStyle(color: Colors.greenAccent, fontSize: 10, fontFamily: 'monospace')),
                           ),
                         )
                      ],
                    ),
                  ),
                )
              ],
            ),
          ),
          
          // Right Panel: Live Feed
          Expanded(
            child: Column(
               children: [
                 Container(
                   padding: const EdgeInsets.all(16),
                   color: const Color(0xFF2E2E3E),
                   child: Row(
                     mainAxisAlignment: MainAxisAlignment.spaceBetween,
                     children: [
                       const Text("Live Market Data Feed", style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white)),
                       if (_streamService.isConnected) const Chip(label: Text("WS Connected", style: TextStyle(color: Colors.white)), backgroundColor: Colors.green)
                       else const Chip(label: Text("WS Disconnected"), backgroundColor: Colors.red),
                     ],
                   ),
                 ),
                 Expanded(
                   child: SingleChildScrollView(
                     child: DataTable(
                       headingRowColor: MaterialStateProperty.all(Colors.black12),
                       columns: const [
                         DataColumn(label: Text('Symbol')),
                         DataColumn(label: Text('LTP')),
                         DataColumn(label: Text('Change')),
                         DataColumn(label: Text('% Change')),
                       ],
                       rows: _quotes.entries.map((entry) {
                         final key = entry.key;
                         final data = entry.value;
                         final change = (data['change'] as num?)?.toDouble() ?? 0.0;
                         final pChange = (data['changePercent'] as num?)?.toDouble() ?? 0.0;
                         final color = change >= 0 ? Colors.green : Colors.red;

                         return DataRow(
                           cells: [
                             DataCell(Text(key, style: const TextStyle(fontWeight: FontWeight.bold))),
                             DataCell(Text((data['lastPrice'] as num?)?.toDouble().toStringAsFixed(2) ?? '0.00')),
                             DataCell(Text(change.toStringAsFixed(2), style: TextStyle(color: color))),
                             DataCell(Text('${pChange.toStringAsFixed(2)}%', style: TextStyle(color: color))),
                           ],
                         );
                       }).toList(),
                     ),
                   ),
                 )
               ],
            ),
          )
        ],
      ),
    );
  }
}
