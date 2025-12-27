import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../widgets/heatmap_view.dart';
import '../widgets/constituents_table.dart';
import '../widgets/app_sidebar.dart';
import '../widgets/indices_performance_view.dart'; // Market Overview
import '../screens/streamer_page.dart';
import '../screens/market_analytics_page.dart';
import '../screens/instrument_explorer_page.dart';
import '../screens/security_explorer_page.dart';

import '../screens/admin/ingestion_logs_page.dart';
import '../utils/app_logger.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  // 0 = Table, 1 = Heatmap, 2 = Analytics
  int _currentView = 2; // Default to Analytics view

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      AppLogger.info("HomePage.initState", "Initial Load of Indices");
      context.read<MarketProvider>().loadIndices();
    });
  }

  // Helper to filter out invalid index values
  String _getValidIndexSymbol(String? index) {
    if (index == null ||
        index.isEmpty ||
        index == 'All Indices' ||
        index == 'Streamer' ||
        index == 'Instruments' ||
        index == 'Security Explorer') { // Added for Security Explorer
      return 'NIFTY 50';
    }
    return index;
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MarketProvider>();
    final isAllIndices = provider.selectedIndex == "All Indices";
    final isStreamer = provider.selectedIndex == "Streamer";
    final isInstruments = provider.selectedIndex == "Instruments";
    final isSecurityExplorer = provider.selectedIndex == "Security Explorer";
    final isPriceTest = provider.selectedIndex == "Price Test";
    final isAdmin = provider.selectedIndex == "Admin Dashboard"; // Added Admin check
    final isAnalytics = _currentView == 2;

    return Scaffold(
      appBar: _buildAppBar(provider, isAllIndices, isStreamer, isInstruments, isSecurityExplorer, isPriceTest, isAdmin),
      body: Row(
        children: [
          AppSidebar(
            provider: provider,
            isAllIndices: isAllIndices,
            isStreamer: isStreamer,
            isInstruments: isInstruments,
            isSecurityExplorer: isSecurityExplorer,
            isPriceTest: isPriceTest,
            // isAdmin is handled by selelctedIndex highlighting logic inside Sidebar
          ), 
          _buildContent(provider, isAllIndices, isStreamer, isInstruments, isSecurityExplorer, isPriceTest, isAdmin, isAnalytics), 
        ],
      ),
    );
  }

  PreferredSizeWidget _buildAppBar(MarketProvider provider, bool isAllIndices, bool isStreamer, bool isInstruments, bool isSecurityExplorer, bool isPriceTest, bool isAdmin) {
    if (isAdmin) {
        return AppBar(
            title: const Text('Market Data Admin Dashboard', style: TextStyle(color: Colors.black87, fontWeight: FontWeight.bold)),
            backgroundColor: Colors.white,
            elevation: 0,
            iconTheme: const IconThemeData(color: Colors.black87),
        );
    }
    
    return AppBar(
        title: Text(
            isAllIndices ? 'Market Overview' : 
            isStreamer ? 'Streamer Config' : 
            isInstruments ? 'Instrument Explorer' : 
            isSecurityExplorer ? 'Security Explorer' : 
            isPriceTest ? 'Price Test' : 
            (provider.selectedIndex ?? 'Market Data'),
            style: const TextStyle(color: Colors.black87, fontWeight: FontWeight.bold)
        ), 
        backgroundColor: Colors.white,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.black87),
        actions: [
          // Force Refresh Toggle
          Row(
            children: [
               const Text("Force Refresh", style: TextStyle(fontSize: 12, color: Colors.black87)),
               Checkbox(
                 value: provider.forceRefresh,
                 onChanged: (val) => provider.toggleForceRefresh(val ?? false),
                 activeColor: Colors.blue,
               ),
            ],
          ),
          const SizedBox(width: 10),

          if (!isAllIndices && !isStreamer && !isInstruments && !isSecurityExplorer && !isPriceTest) ...[ 
            IconButton(
              icon: const Icon(Icons.table_chart),
              tooltip: 'Table View',
              onPressed: () => setState(() => _currentView = 0),
              color: _currentView == 0 ? Colors.blue : Colors.black54,
            ),
            IconButton(
              icon: const Icon(Icons.grid_view),
              tooltip: 'Heatmap View',
              onPressed: () {
                  setState(() => _currentView = 1);
                  AppLogger.info("HomePage", "Switched to Heatmap View");
              },
              color: _currentView == 1 ? Colors.blue : Colors.black54,
            ),
            IconButton(
              icon: const Icon(Icons.analytics),
              tooltip: 'Analytics View',
              onPressed: () {
                  setState(() => _currentView = 2);
                  AppLogger.info("HomePage", "Switched to Analytics View");
              },
              color: _currentView == 2 ? Colors.blue : Colors.black54,
            ),
          ],
          IconButton(
            icon: const Icon(Icons.refresh),
            color: Colors.black54,
            onPressed: () {
                if (isAllIndices) {
                    provider.loadAllIndicesData();
                } else if (isStreamer) {
                    // No refresh action 
                } else if (isSecurityExplorer) {
                    // No refresh action
                } else {
                    provider.refreshIndexData();
                }
            },
          ),
        ],
      );
  }

  Widget _buildContent(MarketProvider provider, bool isAllIndices, bool isStreamer, bool isInstruments, bool isSecurityExplorer, bool isPriceTest, bool isAdmin, bool isAnalytics) {
    return Expanded(
            child: Container(
              color: const Color(0xFFF5F7FA), // Light background
              child: IndexedStack(
                index: isAllIndices ? 0 : 
                       isStreamer ? 1 : 
                       isInstruments ? 2 : 
                       isSecurityExplorer ? 3 : 
                       isPriceTest ? 4 : 
                       isAdmin ? 6 : // Admin is index 6
                       5, // Analytics/Table/Heatmap is 5
                children: [
                   // Index 0: Market Overview
                   const IndicesPerformanceView(),

                   // Index 1: Streamer
                   StreamerPage(),

                   // Index 2: Instrument Explorer
                   InstrumentExplorerPage(),

                   // Index 3: Security Explorer
                   SecurityExplorerPage(),

                   // Index 4: Analytics or Index Details (Table/Heatmap)
                   provider.isLoading
                     ? const Center(child: CircularProgressIndicator())
                     : provider.error != null
                         ? Center(child: Text('Error: ${provider.error}', style: const TextStyle(color: Colors.redAccent)))
                         : isAnalytics
                             ? MarketAnalyticsPage(indexSymbol: _getValidIndexSymbol(provider.selectedIndex))
                             : _currentView == 0
                                 ? const ConstituentsTable()
                                 : const HeatmapView(),
                    
                   // Index 6: Admin Dashboard
                   IngestionLogsPage(),
                ],
              ),
            ),
          );
  }
}
