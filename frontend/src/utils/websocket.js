// src/utils/websocket.js
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;

export const connectUserWebSocket = (username, onBidResultUpdate) => {
  const socket = new SockJS('http://localhost:8089/ws');
  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {
      console.log('✅ WebSocket Connected');

      // 👇 Subscribe to bid result updates for this user
      stompClient.subscribe(`/user/${username}/queue/bid-results`, (message) => {
        const data = JSON.parse(message.body);
        console.log("📩 WebSocket Bid Update:", data);
        onBidResultUpdate(data);
      });
    },
    onStompError: (frame) => {
      console.error('WebSocket error:', frame);
    },
  });

  stompClient.activate();
};

export const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    console.log('🔌 WebSocket Disconnected');
  }
};
