import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../utils/app_logger.dart';

class AppSidebar extends StatelessWidget {
  final MarketProvider provider;
  final bool isAllIndices;
  final bool isStreamer;
  final bool isInstruments;
  final bool isSecurityExplorer;
  final bool isPriceTest;

  const AppSidebar({
    super.key,
    required this.provider,
    required this.isAllIndices,
    required this.isStreamer,
    required this.isInstruments,
    required this.isSecurityExplorer,
    required this.isPriceTest,
  });

  @override
  Widget build(BuildContext context) {
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
          ListTile(
            title: const Text("Admin Dashboard", style: TextStyle(color: Colors.white)),
            leading: const Icon(Icons.admin_panel_settings, color: Colors.red),
            onTap: () {
                Navigator.pushNamed(context, '/admin');
            },
          ),
        ],
      ),
    );
  }
}
