import 'dart:async';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart'; 
import '../../models/ingestion_log.dart';
import '../../services/admin_service.dart';
import 'package:provider/provider.dart';
import '../../providers/market_provider.dart';
import '../../widgets/app_sidebar.dart';

class IngestionLogsPage extends StatefulWidget {
  const IngestionLogsPage({Key? key}) : super(key: key);

  @override
  State<IngestionLogsPage> createState() => _IngestionLogsPageState();
}

class _IngestionLogsPageState extends State<IngestionLogsPage> {
  final AdminService _adminService = AdminService();
  String _selectedProvider = 'UPSTOX';
  final List<String> _providers = ['UPSTOX', 'ZERODHA'];
  
  Timer? _timer;
  bool _isLoading = false;
  List<IngestionLog> _logs = [];

  // Filter State
  bool _filtersExpanded = false;

  @override
  void initState() {
    super.initState();
    _fetchLogs();
    _timer = Timer.periodic(const Duration(seconds: 10), (timer) => _fetchLogs());
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _fetchLogs() async {
    setState(() => _isLoading = true);
    try {
      final logs = await _adminService.getLogs();
      setState(() => _logs = logs);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error loading logs: $e')),
      );
    } finally {
      setState(() => _isLoading = false);
    }
  }

  final TextEditingController _symbolController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MarketProvider>(); // Need provider for Sidebar

    // Force Light Theme for this page as requested
    return Theme(
      data: ThemeData.light().copyWith(
        scaffoldBackgroundColor: const Color(0xFFF5F7FA), // Light grey/white bg
        colorScheme: const ColorScheme.light(
           primary: Colors.blueAccent,
           surface: Colors.white,
           onSurface: Colors.black87,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.white,
          foregroundColor: Colors.black87,
          elevation: 1,
        ),
      ),
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Market Data Admin Dashboard', style: TextStyle(fontWeight: FontWeight.bold)),
          actions: [
            IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: _fetchLogs,
              tooltip: 'Refresh Logs',
            ),
          ],
        ),
        body: Row(
          children: [
            // Reuse AppSidebar (Note: Sidebar might look dark if it hardcodes dark colors, 
            // but we want only the main content area to follow the new theme request mostly,
            // or the whole page. If sidebar uses Theme.of(context), it will adapt.)
            AppSidebar(
              provider: provider,
              isAllIndices: false,
              isStreamer: false,
              isInstruments: false,
              isSecurityExplorer: false,
              isPriceTest: false,
            ),
            // Main Content
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildStatsRow(),
                    const SizedBox(height: 24),
                    _buildControlsCard(),
                    const SizedBox(height: 24),
                    const Text("Job History", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.black87)),
                    const SizedBox(height: 12),
                    Expanded(child: _buildLogsTable()),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatsRow() {
    // Calculate aggregate stats from loaded logs (or last job)
    // For simplicity, let's show stats from the LATEST job if available.
    final latest = _logs.isNotEmpty ? _logs.first : null;
    
    // Or totals? Usually dashboard shows latest sync status.
    // Let's show Latest Job Stats.
    
    int processed = latest?.totalSymbols ?? 0;
    int success = latest?.successCount ?? 0;
    int failed = latest?.failureCount ?? 0;
    String payload = latest != null ? _formatBytes(latest.payloadSize) : "0 B";

    return Row(
      children: [
        Expanded(child: _buildGlossyCard("Symbols Processed", processed.toString(), Colors.blueAccent, Icons.analytics)),
        const SizedBox(width: 16),
        Expanded(child: _buildGlossyCard("Payload Size", payload, Colors.purpleAccent, Icons.data_usage)),
        const SizedBox(width: 16),
        Expanded(child: _buildGlossyCard("Success", success.toString(), Colors.green, Icons.check_circle)),
        const SizedBox(width: 16),
        Expanded(child: _buildGlossyCard("Failed", failed.toString(), Colors.redAccent, Icons.error)),
      ],
    );
  }

  Widget _buildGlossyCard(String title, String value, Color color, IconData icon) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            color.withOpacity(0.1),
            Colors.white,
          ],
        ),
        boxShadow: [
          BoxShadow(
            color: color.withOpacity(0.15),
            blurRadius: 15,
            offset: const Offset(0, 8),
          ),
        ],
        border: Border.all(color: color.withOpacity(0.2), width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(title, style: TextStyle(color: Colors.grey[600], fontSize: 13, fontWeight: FontWeight.w500)),
              Icon(icon, color: color, size: 20),
            ],
          ),
          const SizedBox(height: 12),
          Text(value, style: TextStyle(color: Colors.black87, fontSize: 24, fontWeight: FontWeight.w800)),
        ],
      ),
    );
  }

  String _formatBytes(int bytes) {
    if (bytes <= 0) return "0 B";
    const suffixes = ["B", "KB", "MB", "GB", "TB"];
    var i = 0;
    double size = bytes.toDouble();
    while (size >= 1024 && i < suffixes.length - 1) {
      size /= 1024;
      i++;
    }
    return '${size.toStringAsFixed(2)} ${suffixes[i]}';
  }

  Widget _buildControlsCard() {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      color: Colors.white,
      child: Column(
        children: [
          // Header / Toggle
          InkWell(
            onTap: () => setState(() => _filtersExpanded = !_filtersExpanded),
            borderRadius: BorderRadius.circular(12),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Row(
                    children: [
                      Icon(Icons.tune, color: Colors.blueAccent),
                      SizedBox(width: 12),
                      Text("Actions & Filters", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    ],
                  ),
                  Icon(_filtersExpanded ? Icons.expand_less : Icons.expand_more, color: Colors.grey),
                ],
              ),
            ),
          ),
          
          if (_filtersExpanded)
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
              child: Column(
                children: [
                  const Divider(),
                  const SizedBox(height: 16),
                  Wrap(
                    crossAxisAlignment: WrapCrossAlignment.center,
                    spacing: 16,
                    runSpacing: 16,
                    children: [
                      // Provider Dropdown
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        decoration: BoxDecoration(
                          border: Border.all(color: Colors.grey[300]!),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: DropdownButtonHideUnderline(
                          child: DropdownButton<String>(
                            value: _selectedProvider,
                            items: _providers.map((String value) {
                              return DropdownMenuItem<String>(
                                value: value,
                                child: Text(value),
                              );
                            }).toList(),
                            onChanged: (newValue) => setState(() => _selectedProvider = newValue!),
                          ),
                        ),
                      ),
                      
                      // Symbol Input
                      SizedBox(
                        width: 200,
                        child: TextField(
                          controller: _symbolController,
                          decoration: const InputDecoration(
                            labelText: 'Symbol (Optional)',
                            hintText: 'e.g. INF',
                            border: OutlineInputBorder(),
                            contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 14),
                            isDense: true,
                          ),
                        ),
                      ),
                      
                      const SizedBox(width: 8),
                      
                      ElevatedButton.icon(
                        onPressed: () => _triggerSync(),
                        icon: const Icon(Icons.sync),
                        label: const Text('Trigger Historical Sync'),
                        style: ElevatedButton.styleFrom(
                           backgroundColor: Colors.blueAccent, 
                           foregroundColor: Colors.white,
                           padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                        ),
                      ),
                      
                      OutlinedButton.icon(
                        onPressed: () => _startIngestion(),
                        icon: const Icon(Icons.play_arrow),
                        label: const Text('Start Feed'),
                         style: OutlinedButton.styleFrom(
                           foregroundColor: Colors.green,
                           side: const BorderSide(color: Colors.green),
                           padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                        ),
                      ),
                      
                      OutlinedButton.icon(
                        onPressed: () => _stopIngestion(),
                        icon: const Icon(Icons.stop),
                        label: const Text('Stop Feed'),
                        style: OutlinedButton.styleFrom(
                           foregroundColor: Colors.red, // Red text
                           side: const BorderSide(color: Colors.red),
                           padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildLogsTable() {
    if (_logs.isEmpty) {
      if (_isLoading) return const Center(child: CircularProgressIndicator());
      return const Center(child: Text('No logs available.', style: TextStyle(color: Colors.grey)));
    }

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
        ],
        border: Border.all(color: Colors.grey[200]!),
      ),
      // Fix Overflow
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: ConstrainedBox(
          constraints: const BoxConstraints(minWidth: 800), // Ensure it spans meaningful width
          child: DataTable(
            headingRowColor: MaterialStateProperty.all(Colors.grey[50]),
            dividerThickness: 1,
            columnSpacing: 30,
            showCheckboxColumn: false, // Click anywhere on row
            columns: const [
              DataColumn(label: Text('Job ID', style: TextStyle(fontWeight: FontWeight.bold))),
              DataColumn(label: Text('Start Time', style: TextStyle(fontWeight: FontWeight.bold))),
              DataColumn(label: Text('Status', style: TextStyle(fontWeight: FontWeight.bold))),
              DataColumn(label: Text('Duration', style: TextStyle(fontWeight: FontWeight.bold))),
              DataColumn(label: Text('Success', style: TextStyle(fontWeight: FontWeight.bold))),
              DataColumn(label: Text('Failed', style: TextStyle(fontWeight: FontWeight.bold))),
              DataColumn(label: Text('Payload', style: TextStyle(fontWeight: FontWeight.bold))), 
            ],
            rows: _logs.map((log) {
              return DataRow(
                onSelectChanged: (_) => _showJobDetails(log.jobId),
                cells: [
                  DataCell(Text(log.jobId.substring(0, 8), style: const TextStyle(fontWeight: FontWeight.w500))),
                  DataCell(Text(DateFormat('MMM dd, HH:mm:ss').format(log.startTime))),
                  DataCell(
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: log.status == 'SUCCESS' ? Colors.green.withOpacity(0.1) : 
                               (log.status == 'RUNNING' ? Colors.blue.withOpacity(0.1) : Colors.red.withOpacity(0.1)),
                        borderRadius: BorderRadius.circular(6),
                        border: Border.all(
                            color: log.status == 'SUCCESS' ? Colors.green : 
                                   (log.status == 'RUNNING' ? Colors.blue : Colors.red)
                        ),
                      ),
                      child: Text(
                        log.status,
                        style: TextStyle(
                          color: log.status == 'SUCCESS' ? Colors.green[700] : 
                                 (log.status == 'RUNNING' ? Colors.blue[700] : Colors.red[700]),
                          fontWeight: FontWeight.bold,
                          fontSize: 11,
                        ),
                      ),
                    ),
                  ),
                  DataCell(Text("${(log.durationMs / 1000).toStringAsFixed(1)}s")),
                  DataCell(Text(log.successCount.toString(), style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold))),
                  DataCell(Text(log.failureCount.toString(), style: TextStyle(color: log.failureCount > 0 ? Colors.red : Colors.grey))),
                  DataCell(Text(_formatBytes(log.payloadSize))),
              ]);
            }).toList(),
          ),
        ),
      ),
    );
  }

  Future<void> _showJobDetails(String jobId) async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );

    try {
      final log = await _adminService.getJobDetails(jobId);
      Navigator.pop(context); // Pop loading

      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          backgroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          title: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Job: ${log.jobId.substring(0, 8)}', style: const TextStyle(fontWeight: FontWeight.bold)),
              Container(
                 padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                 decoration: BoxDecoration(
                   color: log.status == 'SUCCESS' ? Colors.green[50] : Colors.red[50],
                   borderRadius: BorderRadius.circular(6),
                 ),
                 child: Text(log.status, style: TextStyle(color: log.status == 'SUCCESS' ? Colors.green : Colors.red, fontSize: 12, fontWeight: FontWeight.bold)),
              ),
            ],
          ),
          content: SizedBox(
            width: 600,
            height: 500,
            child: Column(
              children: [
                // Top Stats in Dialog
                Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.grey[50],
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.grey[200]!),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildDetailStat("Symbols", log.totalSymbols.toString(), Colors.blue),
                      _buildDetailStat("Success", log.successCount.toString(), Colors.green),
                      _buildDetailStat("Failed", log.failureCount.toString(), Colors.red),
                      _buildDetailStat("Duration", "${(log.durationMs/1000).toStringAsFixed(1)}s", Colors.orange),
                    ],
                  ),
                ),
                const SizedBox(height: 20),
                const Align(alignment: Alignment.centerLeft, child: Text("Execution Logs", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14))),
                const SizedBox(height: 8),
                Expanded(
                  child: Container(
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E1E2E), // Dark log console
                      borderRadius: BorderRadius.circular(8),
                    ),
                    padding: const EdgeInsets.all(12),
                    child: log.logs == null || log.logs!.isEmpty
                        ? const Center(child: Text('No logs available.', style: TextStyle(color: Colors.grey)))
                        : ListView.builder(
                            itemCount: log.logs!.length,
                            itemBuilder: (context, index) {
                              return Padding(
                                padding: const EdgeInsets.symmetric(vertical: 2.0),
                                child: Text(log.logs![index], style: const TextStyle(color: Colors.greenAccent, fontSize: 11, fontFamily: 'monospace')),
                              );
                            },
                          ),
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Close'),
            ),
          ],
        ),
      );
    } catch (e) {
      Navigator.pop(context); // Pop loading
      _showError(e);
    }
  }

  Widget _buildDetailStat(String label, String value, Color color) {
    return Column(
      children: [
        Text(label, style: TextStyle(color: Colors.grey[600], fontSize: 11, fontWeight: FontWeight.w600)),
        const SizedBox(height: 4),
        Text(value, style: TextStyle(color: color, fontSize: 16, fontWeight: FontWeight.bold)),
      ],
    );
  }

  // ... Trigger methods (same as before) ...
  Future<void> _triggerSync() async {
    try {
      await _adminService.triggerHistoricalSync(symbol: _symbolController.text.trim().isEmpty ? null : _symbolController.text.trim());
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Historical Sync Triggered')),
      );
      _fetchLogs();
    } catch (e) {
      _showError(e);
    }
  }

  Future<void> _startIngestion() async {
    try {
      await _adminService.startIngestion(_selectedProvider);
       ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Live Ingestion Started for $_selectedProvider')),
      );
    } catch (e) {
      _showError(e);
    }
  }

  Future<void> _stopIngestion() async {
    try {
      await _adminService.stopIngestion(_selectedProvider);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Live Ingestion Stopped for $_selectedProvider')),
      );
    } catch (e) {
      _showError(e);
    }
  }

  void _showError(dynamic e) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
    );
  }
}
