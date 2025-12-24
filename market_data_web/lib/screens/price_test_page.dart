import 'package:flutter/material.dart';
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

  Future<void> _checkPrice() async {
    final symbol = _symbolController.text.trim();
    if (symbol.isEmpty) return;

    setState(() {
      _isLoading = true;
      _error = null;
      _result = null;
    });

    try {
      final data = await _apiService.fetchLivePrices([symbol]);
      
      setState(() {
        // The API returns a Map<String, dynamic> where keys are symbols
        // E.g. { "INFY": { "ltp": 1500.0, ... } }
        if (data.containsKey(symbol)) {
             _result = data[symbol];
        } else if (data.isNotEmpty) {
             // Fallback if the key case mismatches or something
             _result = data.values.first;
        } else {
             _error = "No data found for $symbol";
        }
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Price Verification Test")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _symbolController,
              decoration: const InputDecoration(
                labelText: "Enter Trading Symbol",
                hintText: "e.g. RELIANCE, INFY",
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: _isLoading ? null : _checkPrice,
              child: _isLoading ? const CircularProgressIndicator() : const Text("Check Price"),
            ),
            const SizedBox(height: 24),
            if (_error != null)
              Text("Error: $_error", style: const TextStyle(color: Colors.red)),
            if (_result != null)
              Card(
                color: Colors.blueGrey[900],
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text("Symbol: ${_symbolController.text}", style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                      const Divider(color: Colors.white54),
                      ..._result!.entries.map((e) => Padding(
                        padding: const EdgeInsets.symmetric(vertical: 4),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(e.key, style: const TextStyle(color: Colors.white70)),
                            Text(e.value.toString(), style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                          ],
                        ),
                      )),
                    ],
                  ),
                ),
              )
          ],
        ),
      ),
    );
  }
}
