from pathlib import Path

def inject_flutter_websocket(sdk_path):
    print("  Injecting WebSocket support into Flutter SDK...")
    
    # 1. Dependency
    pubspec_path = sdk_path / "pubspec.yaml"
    if pubspec_path.exists():
        content = pubspec_path.read_text()
        if "web_socket_channel" not in content:
            content = content.replace("dependencies:", "dependencies:\n  web_socket_channel: ^3.0.0")
            pubspec_path.write_text(content)

    # 2. WebSocket Directory & Client
    ws_dir = sdk_path / "lib" / "websocket"
    ws_dir.mkdir(parents=True, exist_ok=True)
    ws_client_path = ws_dir / "market_data_websocket_client.dart"
    
    ws_client_content = """part of openapi.api;

class MarketDataWebSocketClient {
  final String _wsUrl;
  WebSocketChannel? _channel;
  final StreamController<MarketDataUpdateV1> _streamController = StreamController<MarketDataUpdateV1>.broadcast();
  
  bool _isConnected = false;
  bool _isDisposed = false;
  
  MarketDataWebSocketClient({String baseUrl = 'ws://localhost:8092'}) 
      : _wsUrl = '$baseUrl/ws/market-data-stream';

  Stream<MarketDataUpdateV1> get updates => _streamController.stream;
  bool get isConnected => _isConnected;

  void connect() {
    if (_isConnected || _isDisposed) return;
    try {
      _channel = WebSocketChannel.connect(Uri.parse(_wsUrl));
      _isConnected = true;
      _channel!.stream.listen(
        (message) {
          try {
            final Map<String, dynamic> data = json.decode(message as String);
            final update = MarketDataUpdateV1.fromJson(data);
            if (update != null) _streamController.add(update);
          } catch (e) {}
        },
        onError: (error) { _isConnected = false; _reconnect(); },
        onDone: () { _isConnected = false; _reconnect(); },
      );
    } catch (e) { _isConnected = false; _reconnect(); }
  }

  void _reconnect() {
    if (_isDisposed) return;
    Future.delayed(const Duration(seconds: 5), () => connect());
  }

  void disconnect() {
    _channel?.sink.close();
    _isConnected = false;
  }

  void dispose() {
    _isDisposed = true;
    disconnect();
    _streamController.close();
  }
}
"""
    ws_client_path.write_text(ws_client_content)

    # 3. Update lib/api.dart with correct imports and parts
    import re
    api_dart_path = sdk_path / "lib" / "api.dart"
    if api_dart_path.exists():
        content = api_dart_path.read_text()
        
        # Add import if missing
        ws_import = "import 'package:web_socket_channel/web_socket_channel.dart';"
        if ws_import not in content:
            content = content.replace(
                "import 'package:meta/meta.dart';",
                f"import 'package:meta/meta.dart';\n{ws_import}"
            )
            
        # Add part if missing
        ws_part = "part 'websocket/market_data_websocket_client.dart';"
        if ws_part not in content:
            # Find the last part statement to insert after it
            parts = re.findall(r"part '.*';", content)
            if parts:
                last_part = parts[-1]
                content = content.replace(last_part, f"{last_part}\n{ws_part}")
            else:
                # Fallback
                content = content.replace("library openapi.api;", f"library openapi.api;\n\n{ws_part}")
            
        # Clean up ANY export of the websocket client that might have survived
        # This is critical because a library with parts cannot have exports
        content = re.sub(r"export 'websocket/market_data_websocket_client\.dart';\s*", "", content)
        
        api_dart_path.write_text(content)
        print("  ✅ Updated api.dart with WebSocket part and import")

def inject_java_websocket(sdk_path):
    print("  Injecting WebSocket support into Java SDK...")
    ws_dir = sdk_path / "src" / "main" / "java" / "com" / "am" / "marketdata" / "websocket"
    ws_dir.mkdir(parents=True, exist_ok=True)
    
    ws_client_path = ws_dir / "MarketDataWebSocketClient.java"
    ws_client_content = """package com.am.marketdata.websocket;

import com.am.marketdata.model.MarketDataUpdateV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class MarketDataWebSocketClient {
    private final String wsUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocket webSocket;
    private Consumer<MarketDataUpdateV1> onUpdate;

    public MarketDataWebSocketClient(String baseUrl) {
        this.wsUrl = baseUrl + "/ws/market-data-stream";
    }

    public void connect(Consumer<MarketDataUpdateV1> onUpdate) {
        this.onUpdate = onUpdate;
        HttpClient.newHttpClient().newWebSocketBuilder()
            .buildAsync(URI.create(wsUrl), new WebSocketListener())
            .join();
    }

    private class WebSocketListener implements WebSocket.Listener {
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            try {
                MarketDataUpdateV1 update = objectMapper.readValue(data.toString(), MarketDataUpdateV1.class);
                if (onUpdate != null) onUpdate.accept(update);
            } catch (Exception e) {}
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
    }

    public void disconnect() {
        if (webSocket != null) webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Disconnect");
    }
}
"""
    ws_client_path.write_text(ws_client_content)

def inject_python_websocket(sdk_path):
    print("  Injecting WebSocket support into Python SDK...")
    ws_dir = sdk_path / "market_data_client" / "websocket"
    ws_dir.mkdir(parents=True, exist_ok=True)
    (ws_dir / "__init__.py").touch()
    
    ws_client_path = ws_dir / "market_data_websocket_client.py"
    ws_client_content = """import asyncio
import json
import websockets
from market_data_client.models.market_data_update_v1 import MarketDataUpdateV1

class MarketDataWebSocketClient:
    def __init__(self, base_url="ws://localhost:8092"):
        self.ws_url = f"{base_url}/ws/market-data-stream"
        self._on_update = None
        self._task = None

    async def connect(self, on_update):
        self._on_update = on_update
        async with websockets.connect(self.ws_url) as websocket:
            while True:
                message = await websocket.recv()
                data = json.loads(message)
                update = MarketDataUpdateV1.from_dict(data)
                if self._on_update: self._on_update(update)

    def start(self, on_update):
        self._task = asyncio.create_task(self.connect(on_update))

    def stop(self):
        if self._task: self._task.cancel()
"""
    ws_client_path.write_text(ws_client_content)

def inject_all(sdks_config):
    print("\n💉 [INJECT] Applying custom WebSocket support...")
    inject_flutter_websocket(sdks_config["Dart"]["path"])
    inject_java_websocket(sdks_config["Java"]["path"])
    inject_python_websocket(sdks_config["Python"]["path"])
    print("✅ Injection complete.")
