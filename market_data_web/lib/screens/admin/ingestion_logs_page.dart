import 'dart:async';
import 'package:flutter/material.dart';
import '../../models/ingestion_log.dart';
import '../../services/admin_service.dart';

class IngestionLogsPage extends StatefulWidget {
  const IngestionLogsPage({Key? key}) : super(key: key);

  @override
  State<IngestionLogsPage> createState() => _IngestionLogsPageState();
}

class _IngestionLogsPageState extends State<IngestionLogsPage> {
  final AdminService _adminService = AdminService();
  String _selectedProvider = 'UPSTOX';
  final List<String> _providers = ['UPSTOX', 'ZERODHA'];

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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Market Data Admin Dashboard'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _fetchLogs,
          ),
        ],
      ),
      body: Column(
        children: [
          _buildControls(),
          Expanded(child: _buildLogsTable()),
        ],
      ),
    );
  }

  Widget _buildControls() {
    return Card(
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Wrap(
          crossAxisAlignment: WrapCrossAlignment.center,
          spacing: 10,
          children: [
             DropdownButton<String>(
              value: _selectedProvider,
              items: _providers.map((String value) {
                return DropdownMenuItem<String>(
                  value: value,
                  child: Text(value),
                );
              }).toList(),
              onChanged: (newValue) {
                setState(() {
                  _selectedProvider = newValue!;
                });
              },
            ),
            ElevatedButton(
              onPressed: () => _triggerSync(),
              child: const Text('Trigger Historical Sync'),
            ),
            ElevatedButton(
              onPressed: () => _startIngestion(),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.green),
              child: const Text('Start Live Feed'),
            ),
            ElevatedButton(
              onPressed: () => _stopIngestion(),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
              child: const Text('Stop Live Feed'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLogsTable() {
    if (_logs.isEmpty) {
      return const Center(child: Text('No logs available.'));
    }

    return SingleChildScrollView(
      child: DataTable(
        columns: const [
          DataColumn(label: Text('Job ID')),
          DataColumn(label: Text('Start Time')),
          DataColumn(label: Text('Status')),
          DataColumn(label: Text('Duration (ms)')),
          DataColumn(label: Text('Success')),
          DataColumn(label: Text('Failed')),
        ],
        rows: _logs.map((log) {
          return DataRow(cells: [
            DataCell(Text(log.jobId.substring(0, 8))),
            DataCell(Text(log.startTime.toString())),
            DataCell(Text(
              log.status,
              style: TextStyle(
                color: log.status == 'SUCCESS' ? Colors.green : Colors.red,
                fontWeight: FontWeight.bold,
              ),
            )),
            DataCell(Text(log.durationMs.toString())),
            DataCell(Text(log.successCount.toString())),
            DataCell(Text(log.failureCount.toString())),
          ]);
        }).toList(),
      ),
    );
  }

  Future<void> _triggerSync() async {
    try {
      await _adminService.triggerHistoricalSync();
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
