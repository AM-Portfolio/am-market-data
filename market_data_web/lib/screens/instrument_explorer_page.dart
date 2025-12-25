import 'package:flutter/material.dart';
import '../services/api_service.dart';

class InstrumentExplorerPage extends StatefulWidget {
  const InstrumentExplorerPage({super.key});

  @override
  State<InstrumentExplorerPage> createState() => _InstrumentExplorerPageState();
}

class _InstrumentExplorerPageState extends State<InstrumentExplorerPage> {
  final ApiService _apiService = ApiService();

  // Filter State
  final TextEditingController _queryController = TextEditingController();
  final TextEditingController _isinController = TextEditingController();
  
  final List<String> _selectedExchanges = [];
  final List<String> _selectedSegments = [];
  final List<String> _selectedTypes = [];
  
  bool _isLoading = false;
  List<Map<String, dynamic>> _results = [];
  String? _error;

  // Options
  final List<String> _exchanges = ['NSE', 'NFO', 'BSE', 'MCX'];
  final List<String> _segments = ['NSE_EQ', 'NSE_FO', 'BSE_EQ', 'MCX_FO'];
  final List<String> _types = ['EQUITY', 'FUTURE', 'OPTION', 'INDEX'];

  @override
  void initState() {
    super.initState();
    // Default Defaults
    _selectedExchanges.add('NSE');
    _selectedTypes.add('INDEX');
    // Trigger initial search
    WidgetsBinding.instance.addPostFrameCallback((_) => _search());
  }

  @override
  void dispose() {
    _queryController.dispose();
    _isinController.dispose();
    super.dispose();
  }

  Future<void> _search() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final criteria = {
        'queries': _queryController.text.isNotEmpty ? [_queryController.text] : [],
        'isins': _isinController.text.isNotEmpty ? [_isinController.text] : [],
        'exchanges': _selectedExchanges.isNotEmpty ? _selectedExchanges : null,
        'segments': _selectedSegments.isNotEmpty ? _selectedSegments : null,
        'instrumentTypes': _selectedTypes.isNotEmpty ? _selectedTypes : null,
        'provider': 'UPSTOX' // Default to Upstox for now
      };

      final results = await _apiService.advancedSearchInstruments(criteria);
      
      setState(() {
        _results = results;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  void _clearFilters() {
    _queryController.clear();
    _isinController.clear();
    setState(() {
      _selectedExchanges.clear();
      _selectedSegments.clear();
      _selectedTypes.clear();
      _results.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: const Color(0xFF1A1A2E), // Match app theme
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Instrument Explorer',
                style: TextStyle(
                  fontSize: 28, 
                  fontWeight: FontWeight.bold,
                  color: Colors.white
                ),
              ),
              IconButton(
                icon: const Icon(Icons.refresh, color: Colors.white),
                onPressed: _clearFilters,
                tooltip: 'Clear Filters',
              )
            ],
          ),
          const SizedBox(height: 16),
          
          // Filters Card
          Card(
            color: const Color(0xFF16213E),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: TextField(
                          controller: _queryController,
                          style: const TextStyle(color: Colors.white),
                          decoration: const InputDecoration(
                            labelText: 'Search (Name/Symbol)',
                            labelStyle: TextStyle(color: Colors.white70),
                            border: OutlineInputBorder(),
                            enabledBorder: OutlineInputBorder(borderSide: BorderSide(color: Colors.white24)),
                            prefixIcon: Icon(Icons.search, color: Colors.white54),
                          ),
                          onSubmitted: (_) => _search(),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: TextField(
                          controller: _isinController,
                          style: const TextStyle(color: Colors.white),
                          decoration: const InputDecoration(
                            labelText: 'ISIN',
                            labelStyle: TextStyle(color: Colors.white70),
                            border: OutlineInputBorder(),
                            enabledBorder: OutlineInputBorder(borderSide: BorderSide(color: Colors.white24)),
                            prefixIcon: Icon(Icons.qr_code, color: Colors.white54),
                          ),
                          onSubmitted: (_) => _search(),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  
                  // Toggles
                  _buildMultiSelect('Exchanges', _exchanges, _selectedExchanges),
                  const SizedBox(height: 8),
                  _buildMultiSelect('Segments', _segments, _selectedSegments),
                  const SizedBox(height: 8),
                  _buildMultiSelect('Types', _types, _selectedTypes),
                  
                  const SizedBox(height: 16),
                  ElevatedButton.icon(
                    onPressed: _search, 
                    icon: const Icon(Icons.search),
                    label: const Text('Search Instruments'),
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
                      backgroundColor: Colors.blueAccent
                    ),
                  )
                ],
              ),
            ),
          ),
          
          const SizedBox(height: 16),
          
          // Results
          Expanded(
            child: _isLoading 
              ? const Center(child: CircularProgressIndicator())
              : _error != null
                ? Center(child: Text('Error: $_error', style: const TextStyle(color: Colors.redAccent)))
                : _results.isEmpty
                  ? const Center(child: Text('No instruments found. Adjust filters and search.', style: TextStyle(color: Colors.white54)))
                  : _buildResultsTable(),
          ),
        ],
      ),
    );
  }

  Widget _buildMultiSelect(String label, List<String> options, List<String> selected) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 80, 
          child: Padding(
            padding: const EdgeInsets.only(top: 10),
            child: Text(label, style: const TextStyle(color: Colors.white70, fontWeight: FontWeight.bold)),
          )
        ),
        Expanded(
          child: Wrap(
            spacing: 8,
            runSpacing: 4,
            children: options.map((opt) {
              final isSelected = selected.contains(opt);
              return FilterChip(
                label: Text(opt),
                selected: isSelected,
                onSelected: (val) {
                  setState(() {
                    if (val) {
                      selected.add(opt);
                    } else {
                      selected.remove(opt);
                    }
                  });
                },
                backgroundColor: const Color(0xFF0F3460),
                selectedColor: Colors.blueAccent.withOpacity(0.5),
                labelStyle: TextStyle(color: isSelected ? Colors.white : Colors.white70),
                checkmarkColor: Colors.white,
              );
            }).toList(),
          ),
        )
      ],
    );
  }

  Widget _buildResultsTable() {
    return Card(
      color: const Color(0xFF16213E),
      child: SingleChildScrollView(
        child: PaginatedDataTable(
          headingRowColor: MaterialStateProperty.all(const Color(0xFF0F3460)),
          columns: const [
            DataColumn(label: Text('Trading Symbol', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
            DataColumn(label: Text('Name', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
            DataColumn(label: Text('Exchange', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
            DataColumn(label: Text('Segment', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
            DataColumn(label: Text('Type', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
            DataColumn(label: Text('ISIN', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
            DataColumn(label: Text('Instrument Key', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold))),
          ],
          source: _InstrumentDataSource(_results),
          rowsPerPage: _results.isEmpty ? 1 : (_results.length < 100 ? _results.length : 100),
          availableRowsPerPage: const [10, 20, 50, 100, 200],
          onRowsPerPageChanged: (val) {
             // Basic support if needed, but setState logic is tricky purely inside build
          },
          showCheckboxColumn: false,
          arrowHeadColor: Colors.white,
        ),
      ),
    );
  }
}

class _InstrumentDataSource extends DataTableSource {
  final List<Map<String, dynamic>> _data;

  _InstrumentDataSource(this._data);

  @override
  DataRow? getRow(int index) {
    if (index >= _data.length) return null;
    final item = _data[index];
    
    return DataRow(
      cells: [
        DataCell(Text(item['trading_symbol'] ?? '-', style: const TextStyle(color: Colors.white))),
        DataCell(SizedBox(width: 200, child: Text(item['name'] ?? '-', style: const TextStyle(color: Colors.white70), overflow: TextOverflow.ellipsis))),
        DataCell(Text(item['exchange'] ?? '-', style: const TextStyle(color: Colors.white70))),
        DataCell(Text(item['segment'] ?? '-', style: const TextStyle(color: Colors.white70))),
        DataCell(Text(item['instrument_type'] ?? '-', style: const TextStyle(color: Colors.white70))),
        DataCell(Text(item['isin'] ?? '-', style: const TextStyle(color: Colors.white70))),
        DataCell(Text(item['instrument_key'] ?? '-', style: const TextStyle(color: Colors.white54, fontSize: 11))),
      ],
    );
  }

  @override
  bool get isRowCountApproximate => false;

  @override
  int get rowCount => _data.length;

  @override
  int get selectedRowCount => 0;
}
