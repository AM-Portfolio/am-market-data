import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../widgets/heatmap_view.dart';
import '../widgets/constituents_table.dart';
import '../widgets/indices_performance_view.dart'; // Market Overview
import '../screens/streamer_page.dart';
import '../screens/market_analytics_page.dart';
import '../screens/instrument_explorer_page.dart';
import '../screens/security_explorer_page.dart';
import '../screens/price_test_page.dart'; // Added for Price Test
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
    final isPriceTest = provider.selectedIndex == "Price Test"; // Added for Price Test
    final isAnalytics = _currentView == 2;

    return Scaffold(
      appBar: _buildAppBar(provider, isAllIndices, isStreamer, isInstruments, isSecurityExplorer, isPriceTest),
      body: Row(
        children: [
          _buildSidebar(provider, isAllIndices, isStreamer, isInstruments, isSecurityExplorer, isPriceTest), 
          _buildContent(provider, isAllIndices, isStreamer, isInstruments, isSecurityExplorer, isPriceTest, isAnalytics), 
        ],
      ),
    );
  }

  PreferredSizeWidget _buildAppBar(MarketProvider provider, bool isAllIndices, bool isStreamer, bool isInstruments, bool isSecurityExplorer, bool isPriceTest) {
    return AppBar(
        title: Text(isAllIndices ? 'Market Overview' : isStreamer ? 'Streamer Config' : isInstruments ? 'Instrument Explorer' : isSecurityExplorer ? 'Security Explorer' : isPriceTest ? 'Price Test' : (provider.selectedIndex ?? 'Market Data')), 
        actions: [
          // Force Refresh Toggle
          Row(
            children: [
               const Text("Force Refresh", style: TextStyle(fontSize: 12)),
               Checkbox(
                 value: provider.forceRefresh,
                 onChanged: (val) => provider.toggleForceRefresh(val ?? false),
                 activeColor: Colors.blue,
               ),
            ],
          ),
          const SizedBox(width: 10),

          const SizedBox(width: 10),

          if (!isAllIndices && !isStreamer && !isInstruments && !isSecurityExplorer && !isPriceTest) ...[ 
            IconButton(
              icon: const Icon(Icons.table_chart),
              tooltip: 'Table View',
              onPressed: () => setState(() => _currentView = 0),
              color: _currentView == 0 ? Colors.blue : null,
            ),
            IconButton(
              icon: const Icon(Icons.grid_view),
              tooltip: 'Heatmap View',
              onPressed: () {
                  setState(() => _currentView = 1);
                  AppLogger.info("HomePage", "Switched to Heatmap View");
              },
              color: _currentView == 1 ? Colors.blue : null,
            ),
            IconButton(
              icon: const Icon(Icons.analytics),
              tooltip: 'Analytics View',
              onPressed: () {
                  setState(() => _currentView = 2);
                  AppLogger.info("HomePage", "Switched to Analytics View");
              },
              color: _currentView == 2 ? Colors.blue : null,
            ),
          ],
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
                if (isAllIndices) {
                    provider.loadAllIndicesData();
                } else if (isStreamer) {
                    // No refresh action for streamer yet, or maybe reconnect?
                } else if (isSecurityExplorer) {
                    // No refresh action for Security Explorer yet
                } else {
                    provider.refreshIndexData();
                }
            },
          ),
        ],
      );
  }

  Widget _buildSidebar(MarketProvider provider, bool isAllIndices, bool isStreamer, bool isInstruments, bool isSecurityExplorer, bool isPriceTest) {
    return Container(
            width: 260,
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [Color(0xFF1E1E2F), Color(0xFF2D2D44)], // Premium Dark Gradient
              ),
              border: Border(right: BorderSide(color: Colors.white10)),
            ),
            child: Column(
              children: [
                Expanded(
                  child: ListView(
                    padding: const EdgeInsets.symmetric(vertical: 20),
                    children: [
                      // Market Overview Option
                      ListTile(
                        title: const Text("All Indices (Overview)", style: TextStyle(fontWeight: FontWeight.w600, color: Colors.white)),
                        leading: const Icon(Icons.dashboard_rounded, color: Colors.blueAccent),
                        selected: isAllIndices,
                        selectedTileColor: Colors.blueAccent.withOpacity(0.15),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)), // Rounded selection
                        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                        onTap: () => provider.selectIndex("All Indices"),
                      ),

                      const SizedBox(height: 5),

                      // Streamer Option
                      ListTile(
                        title: const Text("Streamer", style: TextStyle(fontWeight: FontWeight.w600, color: Colors.white)),
                        leading: const Icon(Icons.waves, color: Colors.purpleAccent),
                        selected: isStreamer,
                        selectedTileColor: Colors.purpleAccent.withOpacity(0.15),
                         shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                        onTap: () => provider.selectIndex("Streamer"),
                      ),

                      const SizedBox(height: 5),

                      // Instrument Explorer Option
                      ListTile(
                        title: const Text("Instrument Explorer", style: TextStyle(fontWeight: FontWeight.w600, color: Colors.white)),
                        leading: const Icon(Icons.search, color: Colors.tealAccent),
                        selected: isInstruments,
                        selectedTileColor: Colors.tealAccent.withOpacity(0.15),
                         shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                        onTap: () => provider.selectIndex("Instruments"),
                      ),

                      const SizedBox(height: 5),

                      // Security Explorer Option (Added)
                      ListTile(
                        title: const Text("Security Explorer", style: TextStyle(fontWeight: FontWeight.w600, color: Colors.white)),
                        leading: const Icon(Icons.security, color: Colors.redAccent),
                        selected: isSecurityExplorer,
                        selectedTileColor: Colors.redAccent.withOpacity(0.15),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                        onTap: () => provider.selectIndex("Security Explorer"),
                      ),

                      const SizedBox(height: 5),

                      // Price Test Option (Added)
                      ListTile(
                        title: const Text("Price Test", style: TextStyle(fontWeight: FontWeight.w600, color: Colors.white)),
                        leading: const Icon(Icons.price_check, color: Colors.amberAccent),
                        selected: isPriceTest,
                        selectedTileColor: Colors.amberAccent.withOpacity(0.15),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                        onTap: () => provider.selectIndex("Price Test"),
                      ),

                      const SizedBox(height: 15),
                      const Padding(
                        padding: EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                        child: Text("INDICES", style: TextStyle(color: Colors.white54, fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.2)),
                      ),

                      if (provider.availableIndices != null) ...[
                        // Broad Market Dropdown
                        if (provider.availableIndices!.broad.isNotEmpty)
                          Theme(
                            data: Theme.of(context).copyWith(dividerColor: Colors.transparent), // Remove borders
                            child: ExpansionTile(
                              leading: const Icon(Icons.public, color: Colors.greenAccent),
                              title: const Text("Broad Market", style: TextStyle(fontSize: 14, color: Colors.white)),
                              iconColor: Colors.greenAccent,
                              collapsedIconColor: Colors.white54,
                              children: provider.availableIndices!.broad.map((idx) => ListTile(
                                title: Text(idx, style: const TextStyle(fontSize: 13, color: Colors.white70)),
                                selected: provider.selectedIndex == idx,
                                selectedTileColor: Colors.greenAccent.withOpacity(0.1),
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                                contentPadding: const EdgeInsets.only(left: 32, right: 16),
                                dense: true,
                                onTap: () => provider.selectIndex(idx),
                              )).toList(),
                            ),
                          ),

                        // Sectoral Indices Dropdown
                        if (provider.availableIndices!.sector.isNotEmpty)
                           Theme(
                            data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
                            child: ExpansionTile(
                              leading: const Icon(Icons.pie_chart, color: Colors.orangeAccent),
                              title: const Text("Sectoral Indices", style: TextStyle(fontSize: 14, color: Colors.white)),
                              iconColor: Colors.orangeAccent,
                              collapsedIconColor: Colors.white54,
                              children: provider.availableIndices!.sector.map((idx) => ListTile(
                                title: Text(idx, style: const TextStyle(fontSize: 13, color: Colors.white70)),
                                selected: provider.selectedIndex == idx,
                                selectedTileColor: Colors.orangeAccent.withOpacity(0.1),
                                 shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                                contentPadding: const EdgeInsets.only(left: 32, right: 16),
                                dense: true,
                                onTap: () => provider.selectIndex(idx),
                              )).toList(),
                            ),
                           ),
                      ]
                    ],
                  ),
                ),
                // System Tools Section (Bottom of Sidebar)
                const Divider(color: Colors.white10),
                const Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Text("SYSTEM TOOLS", style: TextStyle(color: Colors.white54, fontSize: 10, fontWeight: FontWeight.bold, letterSpacing: 1.2)),
                ),
                ListTile(
                  title: const Text("Refresh Cookies", style: TextStyle(color: Colors.white)),
                  leading: const Icon(Icons.cookie, color: Colors.orange),
                  onTap: () async {
                      AppLogger.info("HomePage", "Refresh Cookies requested");
                      await provider.refreshCookies();
                      ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text(provider.error ?? "Cookies refreshed successfully!"))
                      );
                  },
                ),
                const SizedBox(height: 10),
              ],
            ),
          );
  }

  Widget _buildContent(MarketProvider provider, bool isAllIndices, bool isStreamer, bool isInstruments, bool isSecurityExplorer, bool isPriceTest, bool isAnalytics) {
    return Expanded(
            child: Container(
              color: const Color(0xFF1E1E2F), // Ensure dark background matches main theme
              child: IndexedStack(
                index: isAllIndices ? 0 : isStreamer ? 1 : isInstruments ? 2 : isSecurityExplorer ? 3 : isPriceTest ? 4 : 5, 
                children: [
                   // Index 0: Market Overview
                   const IndicesPerformanceView(),

                   // Index 1: Streamer
                   const StreamerPage(),

                   // Index 2: Instrument Explorer
                   const InstrumentExplorerPage(),

                   // Index 3: Security Explorer
                   const SecurityExplorerPage(),

                   // Index 4: Price Test (Added)
                   const PriceTestPage(),

                   // Index 5: Analytics or Index Details (Table/Heatmap)
                   provider.isLoading
                     ? const Center(child: CircularProgressIndicator())
                     : provider.error != null
                         ? Center(child: Text('Error: ${provider.error}', style: const TextStyle(color: Colors.redAccent)))
                         : isAnalytics
                             ? MarketAnalyticsPage(indexSymbol: _getValidIndexSymbol(provider.selectedIndex))
                             : _currentView == 0
                                 ? const ConstituentsTable()
                                 : const HeatmapView(),
                ],
              ),
            ),
          );
  }
}
