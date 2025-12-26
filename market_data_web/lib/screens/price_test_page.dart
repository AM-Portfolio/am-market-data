
import 'dart:convert';
import 'package:flutter/material.dart';
import '../services/api_service.dart';
import 'package:http/http.dart' as http;

class PriceTestPage extends StatefulWidget {
  const PriceTestPage({super.key});

  @override
  State<PriceTestPage> createState() => _PriceTestPageState();
}

class _PriceTestPageState extends State<PriceTestPage> {
  final _symbolController = TextEditingController(text: 'RELIANCE');
  final ApiService _apiService = ApiService();

  // Test State
  Map<String, dynamic>? _priceData;
  String _rawResponse = '';
  bool _isLoading = false;
  String _error = '';

  // Config
  String _provider = 'UPSTOX'; // Default
  bool _isIndex = false;

  Future<void> _fetchPrice() async {
    setState(() {
      _isLoading = true;
      _priceData = null;
      _rawResponse = '';
      _error = '';
    });

    try {
      final symbol = _symbolController.text.toUpperCase();
      
      // Direct call to debug raw response
      // Use ApiService logic but maybe expose debug method or just copy minimal logic
      final response = await http.get(Uri.parse('${ApiService.baseUrl}/market-data/quotes?symbols=$symbol&provider=$_provider&isIndex=$_isIndex'));
      
      setState(() {
         _rawResponse = response.body; 
      });

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        setState(() {
           if (data is Map && data.containsKey(symbol)) {
             _priceData = data[symbol]; 
           } else if (data is Map && data.isNotEmpty) {
             _priceData = data.values.first; // Fallback
           }
        });
      } else {
        setState(() => _error = "Status ${response.statusCode}: ${response.body}");
      }
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    // White Theme Scope
    return Scaffold(
        backgroundColor: const Color(0xFFF5F7FA),
        appBar: AppBar(
          title: const Text('Price Fetch Test', style: TextStyle(color: Colors.black87, fontWeight: FontWeight.bold)),
          backgroundColor: Colors.white,
          elevation: 0,
        ),
        body: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
               // Control Card
               Container(
                 padding: const EdgeInsets.all(24),
                 decoration: BoxDecoration(
                   color: Colors.white,
                   borderRadius: BorderRadius.circular(16),
                   boxShadow: [
                     BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
                   ]
                 ),
                 child: Column(
                   children: [
                     Row(
                       children: [
                         Expanded(
                           child: TextField(
                             controller: _symbolController,
                             style: const TextStyle(color: Colors.black87),
                             decoration: const InputDecoration(labelText: 'Symbol', labelStyle: TextStyle(color: Colors.grey)),
                           ),
                         ),
                         const SizedBox(width: 16),
                         DropdownButton<String>(
                           value: _provider,
                           items: ['UPSTOX', 'ZERODHA'].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
                           onChanged: (v) => setState(() => _provider = v!),
                           style: const TextStyle(color: Colors.black87),
                           dropdownColor: Colors.white,
                         ),
                       ],
                     ),
                     const SizedBox(height: 16),
                     Row(
                       children: [
                         const Text("Is Index?", style: TextStyle(color: Colors.black87)),
                         Switch(value: _isIndex, onChanged: (v) => setState(() => _isIndex = v)),
                         const Spacer(),
                         ElevatedButton.icon(
                           onPressed: _isLoading ? null : _fetchPrice,
                           icon: const Icon(Icons.play_arrow),
                           label: const Text("Fetch Price"),
                           style: ElevatedButton.styleFrom(
                             backgroundColor: Colors.blueAccent,
                             foregroundColor: Colors.white, 
                             padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
                             shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                           ),
                         )
                       ],
                     )
                   ],
                 ),
               ),
               
               const SizedBox(height: 24),
               
               if (_error.isNotEmpty)
                 Container(
                   padding: const EdgeInsets.all(16),
                   color: Colors.red.withOpacity(0.1),
                   child: Text(_error, style: const TextStyle(color: Colors.red)),
                 ),
               
               if (_isLoading)
                 const Center(child: Padding(padding: EdgeInsets.all(20), child: CircularProgressIndicator())),
                 
               if (_priceData != null) ...[
                 const Text("Parsed Data", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.black87)),
                 const SizedBox(height: 10),
                 _buildDataCard(_priceData!),
               ],
               
               const SizedBox(height: 24),
               
               if (_rawResponse.isNotEmpty) ...[
                  const Text("Raw Response", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.black87)),
                  const SizedBox(height: 10),
                  Expanded(
                    child: Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(color: Colors.grey.shade900, borderRadius: BorderRadius.circular(8)),
                      child: SingleChildScrollView(
                        child: SelectableText(
                          _formatJson(_rawResponse),
                          style: const TextStyle(fontFamily: 'monospace', fontSize: 12, color: Colors.greenAccent),
                        ),
                      ),
                    ),
                  )
               ]
            ],
          ),
        ),
      );
  }

  Widget _buildDataCard(Map<String, dynamic> data) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
        ],
        border: Border.all(color: Colors.blueAccent.withOpacity(0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
           Text(data['symbol'] ?? 'UNKNOWN', style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.black87)),
           const Divider(),
           Wrap(
             spacing: 24,
             children: [
               _kv("LTP", "${data['lastPrice']}"),
               _kv("Change", "${data['change']}", color: (data['change']??0) >= 0 ? Colors.green : Colors.red),
               _kv("Open", "${data['openPrice']}"),
               _kv("High", "${data['highPrice']}"),
               _kv("Low", "${data['lowPrice']}"),
               _kv("Close", "${data['closePrice']}"),
             ],
           )
        ],
      )
    );
  }

  Widget _kv(String k, String v, {Color? color}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(k, style: const TextStyle(color: Colors.grey, fontSize: 12)),
        Text(v, style: TextStyle(color: color ?? Colors.black87, fontSize: 18, fontWeight: FontWeight.bold)),
      ],
    );
  }

  String _formatJson(String jsonStr) {
    try {
      const JsonEncoder encoder = JsonEncoder.withIndent('  ');
      return encoder.convert(jsonDecode(jsonStr));
    } catch (e) {
      return jsonStr;
    }
  }
}
