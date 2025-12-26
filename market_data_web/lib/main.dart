import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'providers/market_provider.dart';
import 'services/api_service.dart';
import 'screens/home_page.dart';
import 'screens/admin/ingestion_logs_page.dart';

void main() {
  runApp(const MarketDataApp());
}

class MarketDataApp extends StatelessWidget {
  const MarketDataApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider(create: (_) => ApiService()),
        ChangeNotifierProvider(create: (_) => MarketProvider()),
      ],
      child: MaterialApp(
        title: 'Market Data Dashboard',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(
            seedColor: Colors.blue,
            brightness: Brightness.light, 
          ),
          useMaterial3: true,
          textTheme: GoogleFonts.interTextTheme(Theme.of(context).textTheme).apply(
            bodyColor: Colors.black87,
            displayColor: Colors.black87,
          ),
          scaffoldBackgroundColor: const Color(0xFFF5F7FA), // Light Grey Background for White Theme
        ),
        initialRoute: '/',
      routes: {
        '/': (context) => const HomePage(),
        '/admin': (context) => const IngestionLogsPage(),
      },
      ),
    );
  }
}
