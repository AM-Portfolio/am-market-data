import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../domain/repository/market_data_repository.dart';
import '../domain/models/security_quote.dart';
import '../domain/models/security_search_request.dart';

class SecurityExplorerPage extends StatefulWidget {
  const SecurityExplorerPage({super.key});

  @override
  State<SecurityExplorerPage> createState() => _SecurityExplorerPageState();
}

class _SecurityExplorerPageState extends State<SecurityExplorerPage> {
  final TextEditingController _queryController = TextEditingController();
  final TextEditingController _indexController = TextEditingController();
  
  List<SecurityQuote> _securities = [];
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
      final repository = context.read<MarketDataRepository>();
      
      final request = SecuritySearchRequest(
        queries: _queryController.text.isNotEmpty ? [_queryController.text] : [],
        provider: 'UPSTOX', // Default provider for now
        // Index filter not directly supported in generic search yet? 
        // SecuritySearchRequest has provider/segment/etc.
        // Assuming query covers symbol. 
        // If index is strictly needed, we might need a specific param or check filtering.
        // For now, let's treat index as part of query if supported or ignore.
        // Or if 'filters' map is used in repository implementation (it was generic request).
      );

      // Using advanced search
      final results = await repository.searchSecuritiesAdvanced(request);
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
      case 'LARGE_CAP': return Colors.green;
      case 'MID_CAP': return Colors.orange;
      case 'SMALL_CAP': return Colors.blueAccent;
      default: return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Security Explorer', style: TextStyle(fontWeight: FontWeight.bold)),
          elevation: 0,
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Filter Section
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
                              controller: _queryController,
                              style: const TextStyle(color: Colors.black87),
                              decoration: InputDecoration(
                                labelText: 'Search (Symbol, ISIN)',
                                labelStyle: TextStyle(color: Colors.grey.shade600),
                                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                                prefixIcon: const Icon(Icons.search, color: Colors.grey),
                                filled: true,
                                fillColor: Colors.grey.shade50,
                              ),
                              onSubmitted: (_) => _searchSecurities(),
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: TextField(
                              controller: _indexController,
                              style: const TextStyle(color: Colors.black87),
                              decoration: InputDecoration(
                                labelText: 'Index (e.g., NIFTY 50)',
                                labelStyle: TextStyle(color: Colors.grey.shade600),
                                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                                prefixIcon: const Icon(Icons.list_alt, color: Colors.grey),
                                filled: true,
                                fillColor: Colors.grey.shade50,
                              ),
                              onSubmitted: (_) => _searchSecurities(),
                            ),
                          ),
                          const SizedBox(width: 16),
                          ElevatedButton(
                            onPressed: _isLoading ? null : _searchSecurities,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.blueAccent,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 20),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                            ),
                            child: _isLoading 
                              ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                              : const Text('Search', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                          ),
                        ],
                      ),
                    ],
                 ),
              ),
              const SizedBox(height: 16),
  
              // Error Message
              if (_errorMessage.isNotEmpty)
                Container(
                  margin: const EdgeInsets.only(bottom: 16),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(color: Colors.red.withOpacity(0.1), borderRadius: BorderRadius.circular(8)),
                  child: Text(_errorMessage, style: const TextStyle(color: Colors.red)),
                ),
  
              // Results Grid
              Expanded(
                child: _securities.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.search_off, size: 64, color: Colors.grey.withOpacity(0.3)),
                            const SizedBox(height: 16),
                            const Text('No securities found. Try adjusting filters.', style: TextStyle(color: Colors.grey)),
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
                          // Domain model properties
                          final symbol = sec.symbol;
                          final isin = sec.isin ?? '-';
                          final sector = 'Unknown Sector'; // Not yet in SecurityQuote? Or add it?
                          // SecurityQuote has exchange, segment, instrumentType.
                          // It does NOT have sector/industry unless I add it.
                          // For now, placeholder or 'N/A'
                          final industry = 'Unknown Industry';
                          final capType = null; // sec.capType?
                          
                          return Container(
                             decoration: BoxDecoration(
                               color: Colors.white,
                               borderRadius: BorderRadius.circular(16),
                               boxShadow: [
                                 BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
                               ]
                             ),
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
                                           color: Colors.black87,
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
                                           color: _getCapColor(capType).withOpacity(0.1),
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
                                 Divider(color: Colors.grey.shade200),
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
        Icon(icon, size: 14, color: Colors.grey),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(color: Colors.black87, fontSize: 13),
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }
}
