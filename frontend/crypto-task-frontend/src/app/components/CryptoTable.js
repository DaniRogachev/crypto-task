'use client';

import { useState, useEffect } from 'react';
import cryptoService from '../services/cryptoService';
import styles from './CryptoTable.module.css';

export default function CryptoTable() {
  const [cryptoPrices, setCryptoPrices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sortConfig, setSortConfig] = useState({ key: 'price', direction: 'descending' });

  // Function to fetch initial data
  const fetchCryptoPrices = async () => {
    try {
      setLoading(true);
      // We'll rely on WebSocket data instead of API calls
      setError(null);
    } catch (err) {
      setError('Failed to fetch cryptocurrency prices. Please try again later.');
      console.error('Error fetching crypto prices:', err);
    } finally {
      setLoading(false);
    }
  };

  // Handler for WebSocket price updates
  const handlePriceUpdates = (updatedPrices) => {
    console.log('Handling price updates:', updatedPrices);
    
    setCryptoPrices(prevPrices => {
      // If we receive a single price update
      if (!Array.isArray(updatedPrices)) {
        console.log('Single price update for:', updatedPrices.symbol);
        return prevPrices.map(price => 
          price.symbol === updatedPrices.symbol ? updatedPrices : price
        );
      }
      
      // If we receive an array of price updates
      console.log('Received array of prices, length:', updatedPrices.length);
      return updatedPrices;
    });
  };

  // Sort function for the table
  const requestSort = (key) => {
    let direction = 'ascending';
    if (sortConfig.key === key && sortConfig.direction === 'ascending') {
      direction = 'descending';
    }
    setSortConfig({ key, direction });
  };

  // Get sorted data
  const getSortedData = () => {
    const sortableData = [...cryptoPrices];
    if (sortConfig.key) {
      sortableData.sort((a, b) => {
        // Handle null values
        if (a[sortConfig.key] === null) return 1;
        if (b[sortConfig.key] === null) return -1;
        
        // Convert to comparable values
        let aValue = a[sortConfig.key];
        let bValue = b[sortConfig.key];
        
        // Handle BigDecimal values from Java backend
        if (typeof aValue === 'object' && aValue !== null) {
          aValue = parseFloat(aValue);
        }
        if (typeof bValue === 'object' && bValue !== null) {
          bValue = parseFloat(bValue);
        }
        
        if (aValue < bValue) {
          return sortConfig.direction === 'ascending' ? -1 : 1;
        }
        if (aValue > bValue) {
          return sortConfig.direction === 'ascending' ? 1 : -1;
        }
        return 0;
      });
    }
    return sortableData;
  };

  // Format price with appropriate precision
  const formatPrice = (price) => {
    if (!price) return 'N/A';
    
    // Convert to number if it's an object (BigDecimal from Java)
    const numPrice = typeof price === 'object' ? parseFloat(price) : price;
    
    // Format based on price range
    if (numPrice < 0.01) return numPrice.toFixed(8);
    if (numPrice < 1) return numPrice.toFixed(6);
    if (numPrice < 1000) return numPrice.toFixed(2);
    return numPrice.toLocaleString('en-US', { maximumFractionDigits: 2 });
  };

  // Calculate price change class (for coloring)
  const getPriceChangeClass = (crypto) => {
    if (!crypto.askPrice || !crypto.bidPrice) return '';
    
    const ask = typeof crypto.askPrice === 'object' ? parseFloat(crypto.askPrice) : crypto.askPrice;
    const bid = typeof crypto.bidPrice === 'object' ? parseFloat(crypto.bidPrice) : crypto.bidPrice;
    
    // Simple spread calculation
    const spread = ((ask - bid) / bid) * 100;
    
    if (spread > 1) return styles.negative;
    if (spread < -1) return styles.positive;
    return '';
  };

  // Initialize data fetching and WebSocket connection
  useEffect(() => {
    // Initial loading state
    setLoading(true);
    
    // Connect to WebSocket for real-time updates
    const disconnectWebSocket = cryptoService.connectWebSocket((data) => {
      console.log('WebSocket data received:', data);
      
      // If we receive data, update the state and turn off loading
      if (data && (Array.isArray(data) ? data.length > 0 : true)) {
        handlePriceUpdates(data);
        setLoading(false);
      }
    });
    
    // Cleanup function
    return () => {
      if (disconnectWebSocket) disconnectWebSocket();
    };
  }, []);

  // Render loading state
  if (loading && cryptoPrices.length === 0) {
    return <div className={styles.loading}>Loading cryptocurrency prices...</div>;
  }

  // Render error state
  if (error && cryptoPrices.length === 0) {
    return <div className={styles.error}>{error}</div>;
  }

  return (
    <div className={styles.tableContainer}>
      <h2>Top Cryptocurrencies</h2>
      <table className={styles.cryptoTable}>
        <thead>
          <tr>
            <th onClick={() => requestSort('symbol')}>
              Symbol
              {sortConfig.key === 'symbol' && (
                <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
              )}
            </th>
            <th onClick={() => requestSort('name')}>
              Name
              {sortConfig.key === 'name' && (
                <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
              )}
            </th>
            <th onClick={() => requestSort('price')}>
              Price (USD)
              {sortConfig.key === 'price' && (
                <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
              )}
            </th>
            <th onClick={() => requestSort('askPrice')}>
              Ask
              {sortConfig.key === 'askPrice' && (
                <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
              )}
            </th>
            <th onClick={() => requestSort('bidPrice')}>
              Bid
              {sortConfig.key === 'bidPrice' && (
                <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
              )}
            </th>
            <th onClick={() => requestSort('volume24h')}>
              24h Volume
              {sortConfig.key === 'volume24h' && (
                <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
              )}
            </th>
            <th onClick={() => requestSort('lastUpdated')}>
              Last Updated
              {sortConfig.key === 'lastUpdated' && (
                <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
              )}
            </th>
          </tr>
        </thead>
        <tbody>
          {getSortedData().map((crypto) => (
            <tr key={crypto.symbol} className={getPriceChangeClass(crypto)}>
              <td>{crypto.symbol}</td>
              <td>{crypto.name}</td>
              <td className={styles.price}>{formatPrice(crypto.price)}</td>
              <td>{formatPrice(crypto.askPrice)}</td>
              <td>{formatPrice(crypto.bidPrice)}</td>
              <td>{crypto.volume24h ? parseFloat(crypto.volume24h).toLocaleString() : 'N/A'}</td>
              <td>{crypto.lastUpdated ? new Date(crypto.lastUpdated).toLocaleString() : 'N/A'}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {loading && <div className={styles.refreshing}>Refreshing data...</div>}
    </div>
  );
}
