//////////////////////////////////////////////////
//
// AM Market Data Streaming API - 1.0.0
// Protocol: ws
// Host: localhost:8092
// Path: /ws/market-data-stream
//
//////////////////////////////////////////////////


package com.asyncapi;

import io.quarkus.websockets.next.WebSocketClient;
import io.quarkus.websockets.next.WebSocketClientConnection;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.CloseReason;
import jakarta.inject.Inject;
import io.quarkus.logging.Log;

@WebSocketClient(path = "/ws/market-data-stream")  
public class AMMarketDataStreamingAPI{

  @Inject
  WebSocketClientConnection connection;

  @OnOpen
  public void onOpen() {
      String broadcastMessage = "Echo called from AM Market Data Streaming API server";
      Log.info("Connected to AM Market Data Streaming API server");
      Log.info(broadcastMessage);
  }

  @OnError
  public void onError(Throwable throwable) {
      Log.error("Websocket connection error: " + throwable.getMessage());
  }



  @OnClose
   public void onClose(CloseReason reason, WebSocketClientConnection connection) {
      int code = reason.getCode();
      Log.info("Websocket disconnected from AM Market Data Streaming API with Close code: " + code);
  }
}


