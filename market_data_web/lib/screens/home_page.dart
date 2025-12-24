import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../widgets/heatmap_view.dart';
import '../widgets/constituents_table.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  // 0 = Table, 1 = Heatmap
  int _currentView = 1; // Default to Heatmap as per user request

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

    return Scaffold(
      appBar: AppBar(
        title: Text(provider.selectedIndex ?? 'Market Data'),
        actions: [
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
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => provider.refreshIndexData(force: true),
          ),
        ],
      ),
      body: Row(
        children: [
          // Sidebar
          Container(
            width: 250,
            color: Colors.black26,
            child: ListView(
              children: [
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
          // Main Content
          Expanded(
            child: provider.isLoading
                ? const Center(child: CircularProgressIndicator())
                : provider.error != null
                    ? Center(child: Text('Error: ${provider.error}'))
                    : _currentView == 0
                        ? const ConstituentsTable()
                        : const HeatmapView(),
          ),
        ],
      ),
    );
  }
}
