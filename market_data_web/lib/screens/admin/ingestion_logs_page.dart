import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../../providers/market_provider.dart';
import '../../models/ingestion_log.dart';
import '../../services/admin_service.dart';
import '../../utils/app_logger.dart';

class IngestionLogsPage extends StatefulWidget {
  const IngestionLogsPage({super.key});

  @override
  State<IngestionLogsPage> createState() => _IngestionLogsPageState();
}

class _IngestionLogsPageState extends State<IngestionLogsPage> with RouteAware {
  final AdminService _adminService = AdminService();
  
  // Controls State
  String _selectedProvider = 'UPSTOX';
  String _selectedSymbol = ''; // For custom symbol input
  // Pre-selected index dropdown value
  String? _selectedIndex; 

  final TextEditingController _symbolController = TextEditingController();
  bool _filtersExpanded = true;
  
  // New State
  DateTimeRange? _selectedDateRange;
  bool _forceRefresh = true;
  bool _fetchIndexStocks = false; // Whether to fetch individual stocks from index symbols

  // Data State
  List<IngestionLog> _logs = [];
  IngestionJobLog? _currentJob;
  bool _isLoading = false;
  Timer? _timer;
  bool _isStreaming = false; // Mock state for feed button visual
  bool _isPollingPaused = false; // Control polling state

  @override
  void initState() {
    super.initState();
    _fetchLogs();
    _startPolling();
  }

  @override
  void dispose() {
    _stopPolling();
    _symbolController.dispose();
    super.dispose();
  }

  void _startPolling() {
    _stopPolling(); // Cancel any existing timer
    _timer = Timer.periodic(const Duration(seconds: 5), (timer) {
      if (mounted && !_isPollingPaused) {
        _fetchLogs();
      }
    });
    AppLogger.info("Admin", "Started polling for logs");
  }

  void _stopPolling() {
    _timer?.cancel();
    _timer = null;
    AppLogger.info("Admin", "Stopped polling for logs");
  }

  void _pausePolling() {
    setState(() => _isPollingPaused = true);
    AppLogger.info("Admin", "Paused polling for logs");
  }

  void _resumePolling() {
    setState(() => _isPollingPaused = false);
    AppLogger.info("Admin", "Resumed polling for logs");
  }

  Future<void> _fetchLogs() async {
    try {
      final logs = await _adminService.getLogs(
          startDate: _selectedDateRange?.start,
          endDate: _selectedDateRange?.end
      );
      if (mounted) {
        setState(() {
          _logs = logs;
          if (_logs.isNotEmpty && _currentJob == null) {
              // Initial load logic if needed, but usually we just show list
          }
           // Update current job stats if running? 
           // For now, let's just pick the latest one for the top cards
           if (_logs.isNotEmpty) {
               final latest = _logs.first;
               // We need IngestionJobLog type for the Cards, but _logs is IngestionLog
               // Map it strictly or usage loose types. properties match mostly.
               _currentJob = IngestionJobLog(
                   jobId: latest.jobId,
                   startTime: latest.startTime,
                   endTime: latest.endTime,
                   status: latest.status,
                   totalSymbols: latest.totalSymbols,
                   successCount: latest.successCount,
                   failureCount: latest.failureCount,
                   failedSymbols: latest.failedSymbols,
                   durationMs: latest.durationMs,
                   message: latest.message,
                   payloadSize: latest.payloadSize 
               );
           }
        });
      }
    } catch (e) {
      AppLogger.error("IngestionLogsPage", "Error fetching logs: $e");
    }
  }

  Future<void> _triggerHistoricalSync() async {
    // Determine symbol: Text Input is the source of truth (populated by dropdown or manual)
    String symbol = _symbolController.text;
    
    if (symbol.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Please select an Index or enter a Symbol")));
        return;
    }

    AppLogger.info("Admin", "Triggering sync for $symbol (Force: $_forceRefresh, Fetch Index Stocks: $_fetchIndexStocks)");
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Triggering sync for $symbol...")));
    
    try {
        await _adminService.triggerHistoricalSync(
          symbol: symbol, 
          forceRefresh: _forceRefresh,
          fetchIndexStocks: _fetchIndexStocks,
        );
        if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Sync Triggered Successfully!")));
            _fetchLogs();
        }
    } catch (e) {
        if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Failed to trigger sync: $e"), backgroundColor: Colors.red));
        }
    }
  }

  Future<void> _stopIngestion() async {
      try {
          await _adminService.stopIngestion(_selectedProvider); // Uses selected provider
          _pausePolling(); // Stop polling when stopping ingestion
          if (mounted) ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Ingestion Stopped Successfully! (Polling paused)")));
      } catch (e) {
          if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Failed to stop ingestion: $e"), backgroundColor: Colors.red));
      }
  }

  void _startStream() {
      setState(() => _isStreaming = true);
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Feed Started (Simulated)")));
  }

  void _stopStream() {
      setState(() => _isStreaming = false);
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Feed Stopped")));
  }


  Future<void> _showJobDetails(String jobId) async {
    // Fetch full details including logs
    showDialog(
      context: context,
      builder: (ctx) => const Center(child: CircularProgressIndicator()),
    );
    
    try {
        final job = await _adminService.getJobDetails(jobId);
        Navigator.pop(context); // Close loading
        
        if (job != null) {
                                final logs = job.logs ?? [];
                                
                                showDialog(
                                    context: context,
                                    builder: (context) => Dialog(
                                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                                        child: Container(
                                            width: 800,
                                            height: 600,
                                            padding: const EdgeInsets.all(24),
                                            child: Column(
                                                crossAxisAlignment: CrossAxisAlignment.start,
                                                children: [
                                                    Row(
                                                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                                        children: [
                                                            Text("Job Details: ${job.jobId}", style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                                                            IconButton(icon: const Icon(Icons.close), onPressed: () => Navigator.pop(context))
                                                        ],
                                                    ),
                                                    const Divider(),
                                                    const SizedBox(height: 10),
                                                    Wrap(
                                                        spacing: 20,
                                                        runSpacing: 10,
                                                        children: [
                                                            _detailItem("Status", job.status, color: job.status == 'SUCCESS' ? Colors.green : Colors.blue),
                                                            _detailItem("Duration", "${(job.durationMs/1000).toStringAsFixed(1)}s"),
                                                            _detailItem("Processed", "${job.totalSymbols}"),
                                                            _detailItem("Success", "${job.successCount}", color: Colors.green),
                                                            _detailItem("Failed", "${job.failureCount}", color: Colors.red),
                                                            _detailItem("Payload", _formatBytes(job.payloadSize)),
                                                        ],
                                                    ),
                                                    const SizedBox(height: 20),
                                                    const Text("Execution Logs", style: TextStyle(fontWeight: FontWeight.bold)),
                                                    const SizedBox(height: 10),
                                                    Expanded(
                                                        child: Container(
                                                            width: double.infinity,
                                                            padding: const EdgeInsets.all(12),
                                                            decoration: BoxDecoration(
                                                                color: Colors.black87,
                                                                borderRadius: BorderRadius.circular(8)
                                                            ),
                                                            child: ListView.builder(
                                                                itemCount: logs.length,
                                                                itemBuilder: (ctx, i) => Padding(
                                                                    padding: const EdgeInsets.only(bottom: 4),
                                                                    child: Text(
                                                                        logs[i], 
                                                                        style: const TextStyle(color: Colors.greenAccent, fontFamily: 'monospace', fontSize: 12)
                                                                    ),
                                                                ),
                                                            ),
                                                        ),
                                                    ),
                                if (job.failedSymbols.isNotEmpty) ...[
                                    const SizedBox(height: 20),
                                    const Text("Failed Symbols", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red)),
                                    const SizedBox(height: 5),
                                    Container(
                                        padding: const EdgeInsets.all(8),
                                        decoration: BoxDecoration(color: Colors.red.withOpacity(0.1), borderRadius: BorderRadius.circular(4)),
                                        child: Text(job.failedSymbols.join(", "), style: const TextStyle(color: Colors.red)),
                                    )
                                ]
                            ],
                        ),
                    ),
                ),
            );
        }
    } catch (e) {
        Navigator.pop(context); // Close loading
        AppLogger.error("Admin", "Error fetching job details: $e");
    }
  }

  Widget _detailItem(String label, String value, {Color? color}) {
      return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
              Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
              Text(value, style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: color ?? Colors.black87)),
          ],
      );
  }

  @override
  Widget build(BuildContext context) {
      // NOTE: We rely on the parent (HomePage) to provide the Scaffold structure.
      // We just build the content here.
      // NOTE: We rely on the parent (HomePage) to provide the Scaffold structure.
      // We just build the content here.
      return Container(
          color: const Color(0xFFF5F7FA), // Light background
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Stats Cards
              if (_currentJob != null) ...[
                 Wrap(
                   spacing: 16,
                   runSpacing: 16,
                   children: [
                     _buildGlossyCard("Symbols Processed", "${_currentJob!.totalSymbols}", Icons.bar_chart, Colors.blue),
                     _buildGlossyCard("Payload Size", _formatBytes(_currentJob!.payloadSize), Icons.data_usage, Colors.purple),
                     _buildGlossyCard("Success", "${_currentJob!.successCount}", Icons.check_circle, Colors.green),
                     _buildGlossyCard("Failed", "${_currentJob!.failureCount}", Icons.error, Colors.red),
                   ],
                 ),
                 const SizedBox(height: 24),
              ],
              
              // Controls
              _buildControlsCard(),
              const SizedBox(height: 24),

              // Job History Table Header with Polling Status
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text("Job History", style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.black87)),
                  Row(
                    children: [
                      // Polling status indicator
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        decoration: BoxDecoration(
                          color: _isPollingPaused ? Colors.orange.withOpacity(0.1) : Colors.green.withOpacity(0.1),
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(color: _isPollingPaused ? Colors.orange : Colors.green),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(
                              _isPollingPaused ? Icons.pause_circle_filled : Icons.autorenew, 
                              size: 16, 
                              color: _isPollingPaused ? Colors.orange : Colors.green,
                            ),
                            const SizedBox(width: 6),
                            Text(
                              _isPollingPaused ? "Polling Paused" : "Auto-refresh ON",
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.bold,
                                color: _isPollingPaused ? Colors.orange : Colors.green,
                              ),
                            ),
                          ],
                        ),
                      ),
                      if (_isPollingPaused) ...[
                        const SizedBox(width: 8),
                        ElevatedButton.icon(
                          onPressed: _resumePolling,
                          icon: const Icon(Icons.play_arrow, size: 16),
                          label: const Text("Resume"),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.green,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            minimumSize: Size.zero,
                            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                          ),
                        ),
                      ],
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 10),
              
              Expanded(
                child: Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(12),
                    boxShadow: [
                      BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
                    ],
                  ),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: SingleChildScrollView(
                      scrollDirection: Axis.vertical,
                      child: SingleChildScrollView(
                        scrollDirection: Axis.horizontal, // Fix overflow on small screens
                        child: ConstrainedBox(
                           constraints: BoxConstraints(minWidth: MediaQuery.of(context).size.width - 350), // Min width to look good
                           child: DataTable(
                              headingTextStyle: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black87),
                              dataTextStyle: const TextStyle(color: Colors.black87),
                              columns: const [
                                DataColumn(label: Text('Job ID')),
                                DataColumn(label: Text('Start Time')),
                                DataColumn(label: Text('Status')),
                                DataColumn(label: Text('Duration')),
                                DataColumn(label: Text('Success')),
                                DataColumn(label: Text('Failed')), 
                                DataColumn(label: Text('Payload')), 
                                DataColumn(label: Text('Actions')),
                              ],
                              rows: _logs.map((log) => DataRow(cells: [
                                DataCell(Text(log.jobId.length > 8 ? log.jobId.substring(0, 8) : log.jobId)),
                                DataCell(Text(DateFormat('MMM dd, HH:mm:ss').format(log.startTime))),
                                DataCell(_buildStatusBadge(log.status)),
                                DataCell(Text("${(log.durationMs / 1000).toStringAsFixed(1)}s")),
                                DataCell(Text("${log.successCount}", style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold))),
                                DataCell(Text("${log.failureCount}", style: const TextStyle(color: Colors.red, fontWeight: FontWeight.bold))),
                                DataCell(Text(_formatBytes(log.payloadSize))),
                                DataCell(IconButton(
                                  icon: const Icon(Icons.visibility, color: Colors.blueGrey),
                                  onPressed: () => _showJobDetails(log.jobId),
                                )),
                              ])).toList(),
                            ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        );
  }

  // Helper to format bytes
  String _formatBytes(int bytes) {
    if (bytes < 1024) return "$bytes B";
    if (bytes < 1024 * 1024) return "${(bytes / 1024).toStringAsFixed(1)} KB";
    return "${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB";
  }

  Widget _buildGlossyCard(String title, String value, IconData icon, Color color) {
    return Container(
      width: 250,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white, // In light theme, glossy is implicit with white + soft shadow/gradient
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: color.withOpacity(0.15),
            blurRadius: 15,
            offset: const Offset(0, 8),
          ),
          BoxShadow( // White inner glow
             color: Colors.white.withOpacity(0.8),
             blurRadius: 0,
             offset: const Offset(-4, -4),
          )
        ],
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
             color.withOpacity(0.05),
             Colors.white,
          ],
        )
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(title, style: TextStyle(color: Colors.grey.shade600, fontSize: 13, fontWeight: FontWeight.bold)),
              Icon(icon, color: color, size: 20),
            ],
          ),
          const SizedBox(height: 10),
          Text(value, style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.black87)),
        ],
      ),
    );
  }

  Widget _buildControlsCard() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 10, offset: const Offset(0, 4)),
        ],
      ),
      child: Column(
        children: [
           // Header with Expand Toggle
           InkWell(
             onTap: () => setState(() => _filtersExpanded = !_filtersExpanded),
             child: Padding(
               padding: const EdgeInsets.all(16.0),
               child: Row(
                 mainAxisAlignment: MainAxisAlignment.spaceBetween,
                 children: [
                    Row(
                      children:  [
                        const Icon(Icons.tune, color: Colors.blueAccent),
                        const SizedBox(width: 10),
                        Text("Actions & Filters", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.grey.shade800)),
                      ],
                    ),
                    Icon(_filtersExpanded ? Icons.expand_less : Icons.expand_more, color: Colors.grey),
                 ],
               ),
             ),
           ),
           
           if (_filtersExpanded)
             Padding(
               padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
               child: Column(
                 children: [
                   const Divider(),
                   const SizedBox(height: 10),
                   Wrap(
                      spacing: 16,
                      runSpacing: 16,
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        // Provider Dropdown
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey.shade300),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: DropdownButton<String>(
                            value: _selectedProvider,
                            underline: const SizedBox(),
                            icon: const Icon(Icons.arrow_drop_down, color: Colors.black54),
                            style: const TextStyle(color: Colors.black87),
                            items: ["UPSTOX", "ZERODHA"].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
                            onChanged: (val) => setState(() => _selectedProvider = val!),
                          ),
                        ),
                        
                        // Index Dropdown
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12),
                          decoration: BoxDecoration(
                            border: Border.all(color: Colors.grey.shade300),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: DropdownButton<String>( 
                           hint: const Text("Select Index"),
                           value: _selectedIndex,
                           underline: const SizedBox(),
                           icon: const Icon(Icons.arrow_drop_down, color: Colors.black54),
                           style: const TextStyle(color: Colors.black87),
                           items: ["NIFTY 50", "NIFTY BANK", "NIFTY 500"].map((e) => DropdownMenuItem(value: e, child: Text(e))).toList(),
                           onChanged: (val) {
                               setState(() {
                                   _selectedIndex = val;
                                   if (val != null) _symbolController.text = val;
                               });
                           }
                          ),
                        ),
                        
                        // Symbol Input
                        SizedBox(
                          width: 250,
                          child: TextField(
                            controller: _symbolController,
                            decoration: InputDecoration(
                              hintText: "Or Custom Symbol",
                              border: OutlineInputBorder(borderRadius: BorderRadius.circular(8), borderSide: BorderSide(color: Colors.grey.shade300)),
                              contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
                              suffixIcon: IconButton(
                                icon: const Icon(Icons.clear, size: 16),
                                onPressed: () {
                                    _symbolController.clear();
                                    setState(() => _selectedIndex = null); // Clear dropdown too
                                },
                              ),
                            ),
                          ),
                        ),
                        
                        // Trigger Button
                        ElevatedButton.icon(
                          onPressed: _triggerHistoricalSync,
                          icon: const Icon(Icons.sync, size: 18),
                          label: const Text("Sync History"),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.blueAccent,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                          ),
                        ),

                        // Stop Ingestion Button
                        OutlinedButton.icon(
                          onPressed: _stopIngestion,
                          icon: const Icon(Icons.stop_circle_outlined, size: 18, color: Colors.red),
                          label: const Text("Stop Ingest", style: TextStyle(color: Colors.red)),
                          style: OutlinedButton.styleFrom(
                            side: const BorderSide(color: Colors.red),
                            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                          ),
                        ),
                        
                        // Stream Controls
                        if (!_isStreaming)
                          OutlinedButton.icon(
                             onPressed: _startStream,
                             icon: const Icon(Icons.play_arrow, color: Colors.green, size: 18),
                             label: const Text("Start Feed", style: TextStyle(color: Colors.green)),
                             style: OutlinedButton.styleFrom(
                               side: const BorderSide(color: Colors.green),
                               padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                               shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                             ),
                          )
                        else
                          OutlinedButton.icon(
                             onPressed: _stopStream,
                             icon: const Icon(Icons.stop, color: Colors.red, size: 18),
                             label: const Text("Stop Feed", style: TextStyle(color: Colors.red)),
                             style: OutlinedButton.styleFrom(
                               side: const BorderSide(color: Colors.red),
                               padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                               shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                             ),
                          )
                      ],
                   ),
                   const SizedBox(height: 16),
                   Wrap(
                       spacing: 20,
                       runSpacing: 16,
                       crossAxisAlignment: WrapCrossAlignment.center,
                       children: [
                           // Date Filter
                           OutlinedButton.icon(
                               onPressed: () async {
                                   final picked = await showDateRangePicker(
                                       context: context,
                                       firstDate: DateTime(2020),
                                       lastDate: DateTime.now(),
                                   );
                                   if (picked != null) {
                                       setState(() => _selectedDateRange = picked);
                                       _fetchLogs();
                                   }
                               },
                               icon: const Icon(Icons.calendar_today, size: 18),
                               label: Text(_selectedDateRange == null 
                                   ? "Filter by Date" 
                                   : "${DateFormat('MM/dd').format(_selectedDateRange!.start)} - ${DateFormat('MM/dd').format(_selectedDateRange!.end)}"),
                           ),
                           if (_selectedDateRange != null)
                               IconButton(
                                   icon: const Icon(Icons.clear, size: 18), 
                                   onPressed: () {
                                       setState(() => _selectedDateRange = null);
                                       _fetchLogs();
                                   }
                               ),
                           
                           // Force Refresh Toggle
                           const SizedBox(width: 10),
                           Row(
                               mainAxisSize: MainAxisSize.min,
                               children: [
                                   Switch(
                                       value: _forceRefresh, 
                                       onChanged: (val) => setState(() => _forceRefresh = val),
                                       activeColor: Colors.blue,
                                   ),
                                   const Text("Force Refresh"),
                               ],
                           ),
                           
                           // Fetch Index Stocks Toggle
                           const SizedBox(width: 10),
                           Row(
                               mainAxisSize: MainAxisSize.min,
                               children: [
                                   Switch(
                                       value: _fetchIndexStocks, 
                                       onChanged: (val) => setState(() => _fetchIndexStocks = val),
                                       activeColor: Colors.green,
                                   ),
                                   const Text("Fetch Index Stocks"),
                               ],
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

  Widget _buildStatusBadge(String status) {
    Color color;
    switch (status) {
      case 'SUCCESS': color = Colors.green; break;
      case 'FAILED': color = Colors.red; break;
      case 'PARTIAL_SUCCESS': color = Colors.orange; break;
      default: color = Colors.blue;
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withOpacity(0.5)),
      ),
      child: Text(status, style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.bold)),
    );
  }
}

// Temporary data class if model isn't sufficient for view logic
class IngestionJobLog {
    final String jobId;
    final DateTime startTime;
    final DateTime? endTime;
    final String status;
    final int totalSymbols;
    final int successCount;
    final int failureCount;
    final List<String> failedSymbols;
    final double durationMs;
    final String? message;
    final int payloadSize;
    final List<String> logs;

    IngestionJobLog({
        required this.jobId,
        required this.startTime,
        this.endTime,
        required this.status,
        required this.totalSymbols,
        required this.successCount,
        required this.failureCount,
        required this.failedSymbols,
        required this.durationMs,
        this.message,
        this.payloadSize = 0,
        this.logs = const [],
    });
}
