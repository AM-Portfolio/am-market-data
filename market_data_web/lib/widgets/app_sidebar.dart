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
    final theme = Theme.of(context);
    final bool isAdmin = provider.selectedIndex == "Admin Dashboard";

    return Container(
      width: 260,
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(4, 0),
          )
        ],
      ),
      child: Column(
        children: [
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(vertical: 20),
              children: [
                // Market Overview Option
                _buildMenuItem(context, "All Indices (Overview)", Icons.dashboard_rounded, Colors.blueAccent, isAllIndices),

                const SizedBox(height: 5),

                // Streamer Option
                _buildMenuItem(context, "Streamer", Icons.waves, Colors.purpleAccent, isStreamer),

                const SizedBox(height: 5),

                // Instrument Explorer Option
                _buildMenuItem(context, "Instrument Explorer", Icons.search, Colors.tealAccent, isInstruments),

                const SizedBox(height: 5),

                // Security Explorer Option (Added)
                _buildMenuItem(context, "Security Explorer", Icons.security, Colors.redAccent, isSecurityExplorer),

                const SizedBox(height: 5),

                // Price Test Option (Added)
                _buildMenuItem(context, "Price Test", Icons.price_check, Colors.amberAccent, isPriceTest),

                const SizedBox(height: 15),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                  child: Text("INDICES", style: TextStyle(color: Colors.grey.shade500, fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.2)),
                ),

                if (provider.availableIndices != null) ...[
                  // Broad Market Dropdown
                  if (provider.availableIndices!.broad.isNotEmpty)
                    Theme(
                      data: theme.copyWith(dividerColor: Colors.transparent), 
                      child: ExpansionTile(
                        leading: const Icon(Icons.public, color: Colors.greenAccent),
                        title: const Text("Broad Market", style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
                        iconColor: Colors.greenAccent,
                        collapsedIconColor: Colors.grey,
                        children: provider.availableIndices!.broad.map((idx) => _buildSubMenuItem(context, idx)).toList(),
                      ),
                    ),

                  // Sectoral Indices Dropdown
                  if (provider.availableIndices!.sector.isNotEmpty)
                     Theme(
                      data: theme.copyWith(dividerColor: Colors.transparent),
                      child: ExpansionTile(
                        leading: const Icon(Icons.pie_chart, color: Colors.orangeAccent),
                        title: const Text("Sectoral Indices", style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500)),
                        iconColor: Colors.orangeAccent,
                        collapsedIconColor: Colors.grey,
                        children: provider.availableIndices!.sector.map((idx) => _buildSubMenuItem(context, idx)).toList(),
                      ),
                     ),
                ]
              ],
            ),
          ),
          // System Tools Section (Bottom of Sidebar)
          Divider(color: Colors.grey.shade200),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text("SYSTEM TOOLS", style: TextStyle(color: Colors.grey.shade500, fontSize: 10, fontWeight: FontWeight.bold, letterSpacing: 1.2)),
          ),
          ListTile(
            title: const Text("Refresh Cookies", style: TextStyle(fontWeight: FontWeight.w500)),
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
          // Admin Dashboard integrated navigation
          _buildMenuItem(context, "Admin Dashboard", Icons.admin_panel_settings, Colors.red, isAdmin, customLabel: "Admin Dashboard"),
        ],
      ),
    );
  }

  Widget _buildMenuItem(BuildContext context, String id, IconData icon, Color color, bool isSelected, {String? customLabel}) {
     return ListTile(
      title: Text(customLabel ?? id, style: TextStyle(fontWeight: isSelected ? FontWeight.bold : FontWeight.w500, color: isSelected ? color : Colors.black87)),
      leading: Icon(icon, color: color),
      selected: isSelected,
      selectedTileColor: color.withOpacity(0.1),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
      onTap: () => provider.selectIndex(customLabel ?? id),
    );
  }

  Widget _buildSubMenuItem(BuildContext context, String id) {
    final isSelected = provider.selectedIndex == id;
    return ListTile(
      title: Text(id, style: TextStyle(fontSize: 13, color: isSelected ? Colors.blueAccent : Colors.black54, fontWeight: isSelected ? FontWeight.bold : FontWeight.normal)),
      selected: isSelected,
      selectedTileColor: Colors.blueAccent.withOpacity(0.05),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      contentPadding: const EdgeInsets.only(left: 32, right: 16),
      dense: true,
      onTap: () => provider.selectIndex(id),
    );
  }
}
