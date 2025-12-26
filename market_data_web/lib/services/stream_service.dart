import 'dart:async';
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';
import '../utils/app_logger.dart';

class StreamService {
  WebSocketChannel? _channel;
  final StreamController<Map<String, dynamic>> _streamController = StreamController<Map<String, dynamic>>.broadcast();

  Stream<Map<String, dynamic>> get stream => _streamController.stream;

  // Connection status
  bool _isConnected = false;
  bool get isConnected => _isConnected;

  final String _wsUrl = 'ws://localhost:8092/ws/market-data-stream';

  void connect() {
    if (_isConnected) return;
    
    try {
      _channel = WebSocketChannel.connect(Uri.parse(_wsUrl));
      _isConnected = true;
      _isConnected = true;
      AppLogger.info("StreamService.connect", "WebSocket Connected");

      _channel!.stream.listen(
        (message) {
          try {
            final data = json.decode(message);
             if (data is Map<String, dynamic>) {
                 _streamController.add(data);
             }
          } catch (e) {
            AppLogger.error("StreamService.connect", "Error parsing WS message", e);
          }
        },
        onError: (error) {
          AppLogger.error("StreamService.connect", "WebSocket Error", error);
          _isConnected = false;
        },
        onDone: () {
          AppLogger.info("StreamService.connect", "WebSocket Closed");
          _isConnected = false;
        },
      );
    } catch (e) {
      AppLogger.error("StreamService.connect", "Error connecting to WebSocket", e);
      _isConnected = false;
    }
  }

  void disconnect() {
    if (_channel != null) {
      _channel!.sink.close();
      _isConnected = false;
    }
  }

  void dispose() {
    disconnect();
    _streamController.close();
  }
}
