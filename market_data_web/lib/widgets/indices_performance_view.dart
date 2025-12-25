import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/market_provider.dart';
import '../models/market_data.dart';
import 'dart:async';

class IndicesPerformanceView extends StatefulWidget {
  const IndicesPerformanceView({Key? key}) : super(key: key);

  @override
  State<IndicesPerformanceView> createState() => _IndicesPerformanceViewState();
}

class _IndicesPerformanceViewState extends State<IndicesPerformanceView> with TickerProviderStateMixin {
  late AnimationController _tickerController;
  late ScrollController _tickerScrollController;
  Timer? _autoScrollTimer;

  @override
  void initState() {
    super.initState();
    _tickerController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    )..forward();
    
    _tickerScrollController = ScrollController();
    _startAutoScroll();
  }

  void _startAutoScroll() {
    _autoScrollTimer = Timer.periodic(const Duration(milliseconds: 50), (timer) {
      if (_tickerScrollController.hasClients) {
        final maxScroll = _tickerScrollController.position.maxScrollExtent;
        final currentScroll = _tickerScrollController.offset;
        
        if (currentScroll >= maxScroll) {
          _tickerScrollController.jumpTo(0);
        } else {
          _tickerScrollController.jumpTo(currentScroll + 1);
        }
      }
    });
  }

  @override
  void dispose() {
    _tickerController.dispose();
    _tickerScrollController.dispose();
    _autoScrollTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<MarketProvider>(
      builder: (context, provider, child) {
        if (provider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }
        
        if (provider.error != null) {
          return Center(child: Text('Error: ${provider.error}', style: const TextStyle(color: Colors.red)));
        }

        if (provider.allIndicesData.isEmpty) {
          return const Center(child: Text('No data loaded. Select "All Indices" to load.', style: TextStyle(color: Colors.white)));
        }

        // Favorite indices for ticker
        final favoriteIndices = provider.allIndicesData.where((data) => 
          ['NIFTY 50', 'INDIA VIX', 'NIFTY BANK', 'NIFTY IT', 'NIFTY AUTO'].contains(data.indexSymbol.toUpperCase())
        ).toList();

        // All indices for grid
        final allIndices = List<StockIndicesMarketData>.from(provider.allIndicesData);
        allIndices.sort((a, b) => b.pChange.compareTo(a.pChange));

        return Column(
          children: [
            // Toggle Row
            _buildToggleRow(provider),
            
            const Divider(color: Colors.grey, height: 1),

            // Animated Ticker for Favorites
            if (favoriteIndices.isNotEmpty) _buildAnimatedTicker(favoriteIndices),

            const SizedBox(height: 16),

            // Animated Grid of Index Cards
            Expanded(
              child: _buildAnimatedGrid(allIndices),
            ),
          ],
        );
      },
    );
  }

  Widget _buildToggleRow(MarketProvider provider) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.black.withOpacity(0.3), Colors.transparent],
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Text(
            'Index Mode:', 
            style: TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.bold)
          ),
          Row(
            children: [
              Text(
                provider.indexSymbol ? 'Index Data' : 'Constituent Stocks',
                style: const TextStyle(color: Colors.grey, fontSize: 12),
              ),
              const SizedBox(width: 8),
              Switch(
                value: provider.indexSymbol,
                onChanged: (value) {
                  provider.toggleIndexSymbol(value);
                },
                activeColor: Colors.blue,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildAnimatedTicker(List<StockIndicesMarketData> favoriteIndices) {
    return Container(
      height: 80,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Colors.blue.withOpacity(0.1),
            Colors.purple.withOpacity(0.1),
          ],
        ),
        border: Border(
          bottom: BorderSide(color: Colors.blue.withOpacity(0.3), width: 1),
        ),
      ),
      child: ListView.builder(
        controller: _tickerScrollController,
        scrollDirection: Axis.horizontal,
        itemCount: favoriteIndices.length * 100, // Infinite scroll effect
        itemBuilder: (context, index) {
          final data = favoriteIndices[index % favoriteIndices.length];
          return _buildTickerCard(data);
        },
      ),
    );
  }

  Widget _buildTickerCard(StockIndicesMarketData data) {
    final isPositive = data.change >= 0;
    final color = isPositive ? Colors.green : Colors.red;

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            color.withOpacity(0.2),
            color.withOpacity(0.05),
          ],
        ),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.3), width: 1),
        boxShadow: [
          BoxShadow(
            color: color.withOpacity(0.2),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            data.indexSymbol,
            style: const TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.bold,
              fontSize: 13,
            ),
          ),
          const SizedBox(width: 12),
          Text(
            data.lastPrice.toStringAsFixed(2),
            style: const TextStyle(
              color: Colors.white70,
              fontSize: 12,
            ),
          ),
          const SizedBox(width: 8),
          Icon(
            isPositive ? Icons.arrow_upward : Icons.arrow_downward,
            color: color,
            size: 14,
          ),
          const SizedBox(width: 4),
          Text(
            '${data.pChange.toStringAsFixed(2)}%',
            style: TextStyle(
              color: color,
              fontWeight: FontWeight.bold,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAnimatedGrid(List<StockIndicesMarketData> allIndices) {
    return GridView.builder(
      padding: const EdgeInsets.all(16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        childAspectRatio: 2.2,
        crossAxisSpacing: 16,
        mainAxisSpacing: 16,
      ),
      itemCount: allIndices.length,
      itemBuilder: (context, index) {
        return TweenAnimationBuilder<double>(
          tween: Tween(begin: 0.0, end: 1.0),
          duration: Duration(milliseconds: 400 + (index * 50)),
          curve: Curves.easeOutCubic,
          builder: (context, value, child) {
            return Transform.translate(
              offset: Offset(0, 20 * (1 - value)),
              child: Opacity(
                opacity: value,
                child: child,
              ),
            );
          },
          child: _buildGlossyIndexCard(allIndices[index]),
        );
      },
    );
  }

  Widget _buildGlossyIndexCard(StockIndicesMarketData data) {
    final isPositive = data.change >= 0;
    final color = isPositive ? Colors.green : Colors.red;

    return MouseRegion(
      cursor: SystemMouseCursors.click,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              const Color(0xFF2E2E3E).withOpacity(0.9),
              const Color(0xFF1E1E2E).withOpacity(0.9),
            ],
          ),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: color.withOpacity(0.3),
            width: 1,
          ),
          boxShadow: [
            BoxShadow(
              color: color.withOpacity(0.1),
              blurRadius: 12,
              offset: const Offset(0, 4),
            ),
            BoxShadow(
              color: Colors.black.withOpacity(0.3),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Stack(
          children: [
            // Glossy overlay
            Positioned(
              top: 0,
              left: 0,
              right: 0,
              child: Container(
                height: 40,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      Colors.white.withOpacity(0.1),
                      Colors.transparent,
                    ],
                  ),
                  borderRadius: const BorderRadius.only(
                    topLeft: Radius.circular(16),
                    topRight: Radius.circular(16),
                  ),
                ),
              ),
            ),
            
            // Content
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Index Name
                  Text(
                    data.indexSymbol,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  
                  // Price
                  Text(
                    data.lastPrice.toStringAsFixed(2),
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  
                  // Change Row
                  Row(
                    children: [
                      Icon(
                        isPositive ? Icons.trending_up : Icons.trending_down,
                        color: color,
                        size: 16,
                      ),
                      const SizedBox(width: 4),
                      Text(
                        data.change.toStringAsFixed(2),
                        style: TextStyle(
                          color: color,
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: color.withOpacity(0.2),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          '${isPositive ? '+' : ''}${data.pChange.toStringAsFixed(2)}%',
                          style: TextStyle(
                            color: color,
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
