import axios from 'axios';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const API_BASE_URL = 'http://localhost:8080/api';
const WS_URL = 'http://localhost:8080/ws';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// STOMP client instance
let stompClient = null;

const cryptoService = {
  // Connect to WebSocket
  connectWebSocket: (onPricesUpdate) => {
    if (stompClient) return;
    
    const socket = new SockJS(WS_URL);
    stompClient = new Client({
      webSocketFactory: () => socket,
      debug: function (str) {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });
    
    stompClient.onConnect = (frame) => {
      console.log('Connected to WebSocket server:', frame);
      
      // Subscribe to all prices
      stompClient.subscribe('/topic/prices', (message) => {
        if (onPricesUpdate) {
          try {
            console.log('Received price update message');
            const data = JSON.parse(message.body);
            if (Array.isArray(data)) {
              console.log(`Parsed data array with ${data.length} items`);
              if (data.length > 0) {
                console.log('First item:', data[0]);
              }
            } else {
              console.log('Parsed single data item:', data);
            }
            onPricesUpdate(data);
          } catch (error) {
            console.error('Error parsing message:', error);
          }
        }
      });
      
      // Send a message to get initial data
      stompClient.publish({
        destination: '/app/subscribe-all',
        body: JSON.stringify({}),
      });
    };
    
    stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Additional details:', frame.body);
    };
    
    stompClient.activate();
    
    return () => {
      if (stompClient) {
        stompClient.deactivate();
        stompClient = null;
      }
    };
  },
  
  // Disconnect from WebSocket
  disconnectWebSocket: () => {
    if (stompClient) {
      stompClient.deactivate();
      stompClient = null;
    }
  },
  
  // Fetch all cryptocurrency prices
  getAllPrices: async () => {
    try {
      const response = await apiClient.get('/prices');
      console.log('Fetched prices from API:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching crypto prices:', error);
      throw error;
    }
  },
  
  // Fetch latest prices in simplified format
  getLatestPrices: async () => {
    try {
      const response = await apiClient.get('/prices/latest');
      return response.data;
    } catch (error) {
      console.error('Error fetching latest crypto prices:', error);
      throw error;
    }
  },
  
  // Fetch price for a specific cryptocurrency
  getPriceBySymbol: async (symbol) => {
    try {
      const response = await apiClient.get(`/prices/${symbol}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching price for ${symbol}:`, error);
      throw error;
    }
  },
  
  // Fetch all top cryptocurrency pairs
  getTopPairs: async () => {
    try {
      const response = await apiClient.get('/pairs');
      return response.data;
    } catch (error) {
      console.error('Error fetching top crypto pairs:', error);
      throw error;
    }
  },
  
  // Fetch all top cryptocurrency pairs with names
  getTopPairsWithNames: async () => {
    try {
      const response = await apiClient.get('/pairs/with-names');
      return response.data;
    } catch (error) {
      console.error('Error fetching top crypto pairs with names:', error);
      throw error;
    }
  },
  
  // Fetch user transaction history
  getUserTransactions: async () => {
    try {
      const response = await apiClient.get('/transactions');
      return response.data;
    } catch (error) {
      console.error('Error fetching transaction history:', error);
      throw error;
    }
  },
};

export default cryptoService;
