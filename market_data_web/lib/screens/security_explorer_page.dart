import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/api_service.dart';

class SecurityExplorerPage extends StatefulWidget {
  const SecurityExplorerPage({super.key});

  @override
  State<SecurityExplorerPage> createState() => _SecurityExplorerPageState();
}

class _SecurityExplorerPageState extends State<SecurityExplorerPage> {
  final TextEditingController _queryController = TextEditingController();
  final TextEditingController _indexController = TextEditingController();
  
  List<dynamic> _securities = [];
  bool _isLoading = false;
  String _errorMessage = '';

  @override
  void dispose() {
    _queryController.dispose();
    _indexController.dispose();
    super.dispose();
  }

  Future<void> _searchSecurities() async {
    setState(() {
      _isLoading = true;
      _errorMessage = '';
    });

    try {
      final apiService = Provider.of<ApiService>(context, listen: false);
      
      final Map<String, dynamic> request = {};
      if (_queryController.text.isNotEmpty) {
        request['query'] = _queryController.text;
      }
      if (_indexController.text.isNotEmpty) {
        request['index'] = _indexController.text;
      }

      // If both empty, maybe don't search or search all? 
      // User might want to see all securities if they click search empty.
      // But typically we want some filter. However, let's allow "GetAll" implicitly if empty? 
      // Backend handles empty by returning all? Or SecurityController/Service logic might require something.
      // SecurityService.search logic checks if fields != null. If all null, it returns empty mongo query?
      // mongoTemplate.find(new Query(), ...) returns all documents. 
      // So yes, empty search returns all securities. 
      
      final results = await apiService.searchSecuritiesAdvanced(request);
      setState(() {
        _securities = results;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Error searching securities: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Color _getCapColor(String? type) {
    if (type == null) return Colors.grey;
    switch (type.toUpperCase()) {
      case 'LARGE_CAP': return Colors.greenAccent;
      case 'MID_CAP': return Colors.orangeAccent;
      case 'SMALL_CAP': return Colors.blueAccent;
      default: return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF1E1E2F),
      appBar: AppBar(
        title: const Text('Security Explorer'),
        backgroundColor: const Color(0xFF2D2D44),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Filter Section
            Card(
              color: const Color(0xFF2D2D44),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: TextField(
                            controller: _queryController,
                            style: const TextStyle(color: Colors.white),
                            decoration: const InputDecoration(
                              labelText: 'Search (Symbol, ISIN)',
                              labelStyle: TextStyle(color: Colors.white70),
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.search, color: Colors.white54),
                              filled: true,
                              fillColor: Color(0xFF1E1E2F),
                            ),
                            onSubmitted: (_) => _searchSecurities(),
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: TextField(
                            controller: _indexController,
                            style: const TextStyle(color: Colors.white),
                            decoration: const InputDecoration(
                              labelText: 'Index (e.g., NIFTY 50)',
                              labelStyle: TextStyle(color: Colors.white70),
                              border: OutlineInputBorder(),
                              prefixIcon: Icon(Icons.list_alt, color: Colors.white54),
                              filled: true,
                              fillColor: Color(0xFF1E1E2F),
                            ),
                            onSubmitted: (_) => _searchSecurities(),
                          ),
                        ),
                        const SizedBox(width: 16),
                        ElevatedButton(
                          onPressed: _isLoading ? null : _searchSecurities,
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.blueAccent,
                            padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 20),
                          ),
                          child: _isLoading 
                            ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                            : const Text('Search', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Error Message
            if (_errorMessage.isNotEmpty)
              Container(
                margin: const EdgeInsets.only(bottom: 16),
                padding: const EdgeInsets.all(12),
                color: Colors.red.withOpacity(0.2),
                child: Text(_errorMessage, style: const TextStyle(color: Colors.redAccent)),
              ),

            // Results Grid
            Expanded(
              child: _securities.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.search_off, size: 64, color: Colors.white12),
                          const SizedBox(height: 16),
                          const Text('No securities found. Try adjusting filters.', style: TextStyle(color: Colors.white54)),
                        ],
                      ),
                    )
                  : GridView.builder(
                      gridDelegate: const SliverGridDelegateWithMaxCrossAxisExtent(
                        maxCrossAxisExtent: 350,
                        childAspectRatio: 1.8,
                        crossAxisSpacing: 16,
                        mainAxisSpacing: 16,
                      ),
                      itemCount: _securities.length,
                      itemBuilder: (context, index) {
                        final sec = _securities[index];
                        final key = sec['key'] ?? {};
                        final metadata = sec['metadata'] ?? {};
                        final symbol = key['symbol'] ?? 'Unknown';
                        final isin = key['isin'] ?? '-';
                        final sector = metadata['sector'] ?? 'Unknown Sector';
                        final industry = metadata['industry'] ?? 'Unknown Industry';
                        final capType = metadata['market_cap_type'];
                        
                        return Card(
                          color: const Color(0xFF27293D),
                          elevation: 4,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                          child: Padding(
                            padding: const EdgeInsets.all(16.0),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Expanded(
                                      child: Text(
                                        symbol,
                                        style: const TextStyle(
                                          color: Colors.white,
                                          fontSize: 20,
                                          fontWeight: FontWeight.bold,
                                        ),
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ),
                                    if (capType != null)
                                      Container(
                                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                        decoration: BoxDecoration(
                                          color: _getCapColor(capType).withOpacity(0.2),
                                          borderRadius: BorderRadius.circular(8),
                                          border: Border.all(color: _getCapColor(capType), width: 1),
                                        ),
                                        child: Text(
                                          capType,
                                          style: TextStyle(
                                            color: _getCapColor(capType),
                                            fontSize: 10,
                                            fontWeight: FontWeight.bold,
                                          ),
                                        ),
                                      ),
                                  ],
                                ),
                                const Divider(color: Colors.white10),
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    _buildInfoRow(Icons.pie_chart, sector),
                                    const SizedBox(height: 4),
                                    _buildInfoRow(Icons.business, industry),
                                    const SizedBox(height: 4),
                                    _buildInfoRow(Icons.fingerprint, isin),
                                  ],
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(IconData icon, String text) {
    return Row(
      children: [
        Icon(icon, size: 14, color: Colors.white54),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(color: Colors.white70, fontSize: 13),
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }
}
