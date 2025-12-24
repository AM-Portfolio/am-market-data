import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../widgets/market_movers_view.dart';
import '../widgets/sector_performance_view.dart';
import '../widgets/market_cap_view.dart';
import '../widgets/heatmap_view.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';

class MarketAnalyticsPage extends StatefulWidget {
  final String indexSymbol;

  const MarketAnalyticsPage({super.key, this.indexSymbol = 'NIFTY 50'});

  @override
  State<MarketAnalyticsPage> createState() => _MarketAnalyticsPageState();
}

class _MarketAnalyticsPageState extends State<MarketAnalyticsPage> {
  final ApiService _apiService = ApiService();
  
  List<Map<String, dynamic>> _gainers = [];
  List<Map<String, dynamic>> _losers = [];
  List<Map<String, dynamic>> _sectors = [];
  
  bool _isLoadingMovers = false;
  bool _isLoadingSectors = false;

  @override
  void initState() {
    super.initState();
    _loadAnalytics();
  }

  @override
  void didUpdateWidget(MarketAnalyticsPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.indexSymbol != widget.indexSymbol) {
      _loadAnalytics();
    }
  }

  Future<void> _loadAnalytics() async {
    setState(() {
      _isLoadingMovers = true;
      _isLoadingSectors = true;
    });

    // Load all analytics data concurrently
    await Future.wait([
      _loadMovers(),
      _loadSectors(),
    ]);
  }

  Future<void> _loadMovers() async {
    try {
      final gainers = await _apiService.fetchMovers(
        type: 'gainers',
        limit: 10,
        indexSymbol: widget.indexSymbol,
      );
      final losers = await _apiService.fetchMovers(
        type: 'losers',
        limit: 10,
        indexSymbol: widget.indexSymbol,
      );
      
      if (mounted) {
        setState(() {
          _gainers = gainers;
          _losers = losers;
          _isLoadingMovers = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoadingMovers = false);
      }
    }
  }

  Future<void> _loadSectors() async {
    try {
      final sectors = await _apiService.fetchSectorPerformance(
        indexSymbol: widget.indexSymbol,
      );
      
      if (mounted) {
        setState(() {
          _sectors = sectors;
          _isLoadingSectors = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoadingSectors = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<MarketProvider>(context);

    return Container(
      color: const Color(0xFF1A1A2E),
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Market Analytics',
                      style: const TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                    Text(
                      widget.indexSymbol,
                      style: const TextStyle(
                        fontSize: 16,
                        color: Colors.white60,
                      ),
                    ),
                  ],
                ),
                IconButton(
                  icon: const Icon(Icons.refresh, color: Colors.white),
                  onPressed: _loadAnalytics,
                  tooltip: 'Refresh Analytics',
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Market Movers
            MarketMoversView(
              gainers: _gainers,
              losers: _losers,
              isLoading: _isLoadingMovers,
            ),
            const SizedBox(height: 16),

            // Sector Performance (Full Width)
            SectorPerformanceView(
              sectors: _sectors,
              isLoading: _isLoadingSectors,
            ),
            const SizedBox(height: 24),

            // Heatmap Section
            if (provider.selectedIndex != null && 
                provider.selectedIndex!.isNotEmpty &&
                provider.selectedIndex != 'All Indices')
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Heatmap View',
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 16),
                  HeatmapView(),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
