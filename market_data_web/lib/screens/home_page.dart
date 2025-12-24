import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../widgets/heatmap_view.dart';
import '../widgets/constituents_table.dart';
import '../widgets/indices_performance_view.dart'; // Market Overview
import '../screens/streamer_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  // 0 = Table, 1 = Heatmap
  int _currentView = 1; 

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<MarketProvider>().loadIndices();
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MarketProvider>();
    final isAllIndices = provider.selectedIndex == "All Indices";
    final isStreamer = provider.selectedIndex == "Streamer";

    return Scaffold(
      appBar: AppBar(
        title: Text(isAllIndices ? 'Market Overview' : isStreamer ? 'Streamer Config' : (provider.selectedIndex ?? 'Market Data')),
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
          
          if (!isAllIndices && !isStreamer) ...[
            IconButton(
              icon: const Icon(Icons.table_chart),
              tooltip: 'Table View',
              onPressed: () => setState(() => _currentView = 0),
              color: _currentView == 0 ? Colors.blue : null,
            ),
            IconButton(
              icon: const Icon(Icons.grid_view),
              tooltip: 'Heatmap View',
              onPressed: () => setState(() => _currentView = 1),
              color: _currentView == 1 ? Colors.blue : null,
            ),
          ],
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
                if (isAllIndices) {
                    provider.loadAllIndicesData();
                } else if (isStreamer) {
                    // No refresh action for streamer yet, or maybe reconnect?
                } else {
                    provider.refreshIndexData();
                }
            },
          ),
        ],
      ),
      body: Row(
        children: [
          // Sidebar
          Container(
            width: 250,
            color: Colors.black26,
            child: Column(
              children: [
                Expanded(
                  child: ListView(
                    children: [
                      // Market Overview Option
                      ListTile(
                        title: const Text("All Indices (Overview)", style: TextStyle(fontWeight: FontWeight.bold)),
                        leading: const Icon(Icons.dashboard, color: Colors.blueAccent),
                        selected: isAllIndices,
                        selectedTileColor: Colors.blue.withOpacity(0.2),
                        onTap: () => provider.selectIndex("All Indices"),
                      ),
                      
                      // Streamer Option
                      ListTile(
                        title: const Text("Streamer", style: TextStyle(fontWeight: FontWeight.bold)),
                        leading: const Icon(Icons.waves, color: Colors.purpleAccent),
                        selected: isStreamer,
                        selectedTileColor: Colors.blue.withOpacity(0.2),
                        onTap: () => provider.selectIndex("Streamer"),
                      ),
                      
                      const SizedBox(height: 10),
                      
                      if (provider.availableIndices != null) ...[
                        if (provider.availableIndices!.broad.isNotEmpty)
                          const Padding(
                            padding: EdgeInsets.all(8.0),
                            child: Text('BROAD MARKET', style: TextStyle(color: Colors.grey)),
                          ),
                        ...provider.availableIndices!.broad.map((idx) => ListTile(
                              title: Text(idx),
                              selected: provider.selectedIndex == idx,
                              selectedTileColor: Colors.blue.withOpacity(0.2),
                              onTap: () => provider.selectIndex(idx),
                            )),
                        if (provider.availableIndices!.sector.isNotEmpty)
                          const Padding(
                            padding: EdgeInsets.all(8.0),
                            child: Text('SECTORAL', style: TextStyle(color: Colors.grey)),
                          ),
                        ...provider.availableIndices!.sector.map((idx) => ListTile(
                              title: Text(idx),
                              selected: provider.selectedIndex == idx,
                              selectedTileColor: Colors.blue.withOpacity(0.2),
                              onTap: () => provider.selectIndex(idx),
                            )),
                      ]
                    ],
                  ),
                ),
                // System Tools Section (Bottom of Sidebar)
                const Divider(),
                const Padding(
                  padding: EdgeInsets.all(8.0),
                  child: Text("SYSTEM TOOLS", style: TextStyle(color: Colors.grey, fontSize: 10)),
                ),
                ListTile(
                  title: const Text("Refresh Cookies"),
                  leading: const Icon(Icons.cookie, color: Colors.orange),
                  onTap: () async {
                      await provider.refreshCookies();
                      ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(content: Text(provider.error ?? "Cookies refreshed successfully!"))
                      );
                  },
                ),
              ],
            ),
          ),
          // Main Content
          Expanded(
            child: provider.isLoading
                ? const Center(child: CircularProgressIndicator())
                : provider.error != null
                    ? Center(child: Text('Error: ${provider.error}'))
                    : isAllIndices
                        ? const IndicesPerformanceView()
                        : isStreamer
                            ? const StreamerPage()
                            : _currentView == 0
                                ? const ConstituentsTable()
                                : const HeatmapView(),
          ),
        ],
      ),
    );
  }
}
