import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../domain/repository/market_data_repository.dart';
import '../domain/models/security_quote.dart';
import '../domain/models/sector_performance.dart';
import '../providers/market_provider.dart';
import '../widgets/market_movers_view.dart';
import '../widgets/sector_performance_view.dart';
import '../widgets/heatmap_view.dart';

class MarketAnalyticsPage extends StatefulWidget {
  final String indexSymbol;

  const MarketAnalyticsPage({super.key, this.indexSymbol = 'NIFTY 50'});

  @override
  State<MarketAnalyticsPage> createState() => _MarketAnalyticsPageState();
}

class _MarketAnalyticsPageState extends State<MarketAnalyticsPage> {
  
  List<SecurityQuote> _gainers = [];
  List<SecurityQuote> _losers = [];
  List<SectorPerformance> _sectors = [];
  
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
      final repository = context.read<MarketDataRepository>();
      final gainers = await repository.getMarketMovers('gainers');
      final losers = await repository.getMarketMovers('losers');
      
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
      final repository = context.read<MarketDataRepository>();
      final sectors = await repository.getSectorPerformance(widget.indexSymbol);
      
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

    // White Theme Scope
    return Scaffold(
        backgroundColor: const Color(0xFFF5F7FA),
        body: SingleChildScrollView(
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
                      const Text(
                        'Market Analytics',
                        style: TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                          color: Colors.black87,
                        ),
                      ),
                      Text(
                        widget.indexSymbol,
                        style: TextStyle(
                          fontSize: 16,
                          color: Colors.grey.shade600,
                        ),
                      ),
                    ],
                  ),
                  IconButton(
                    icon: const Icon(Icons.refresh, color: Colors.blueAccent),
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
                        color: Colors.black87,
                      ),
                    ),
                    const SizedBox(height: 16),
                    const HeatmapView(),
                  ],
                ),
            ],
          ),
        ),
      );
  }
}
