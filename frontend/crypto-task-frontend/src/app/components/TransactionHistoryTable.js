'use client';

import { useState, useEffect } from 'react';
import cryptoService from '../services/cryptoService';
import styles from '../transactions/page.module.css';

export default function TransactionHistoryTable() {
  const [transactions, setTransactions] = useState([]);
  const [cryptoPrices, setCryptoPrices] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sortConfig, setSortConfig] = useState({ key: 'transactionDate', direction: 'descending' });

  // Function to fetch transaction history
  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const data = await cryptoService.getUserTransactions();
      setTransactions(data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch transaction history. Please try again later.');
      console.error('Error fetching transactions:', err);
    } finally {
      setLoading(false);
    }
  };

  // Function to fetch current crypto prices
  const fetchCryptoPrices = async () => {
    try {
      // Instead of using the API directly, we'll connect to WebSocket for prices
      // The cryptoService WebSocket already gives us price updates
    } catch (err) {
      console.error('Error fetching crypto prices:', err);
    }
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
    const sortableData = [...transactions];
    if (sortConfig.key) {
      sortableData.sort((a, b) => {
        // Handle null values
        if (a[sortConfig.key] === null) return 1;
        if (b[sortConfig.key] === null) return -1;
        
        // Convert to comparable values
        let aValue = a[sortConfig.key];
        let bValue = b[sortConfig.key];
        
        // Handle dates
        if (sortConfig.key === 'transactionDate') {
          aValue = new Date(aValue).getTime();
          bValue = new Date(bValue).getTime();
        }
        
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

  // Format currency values
  const formatCurrency = (value) => {
    if (!value) return '$0.00';
    
    // Convert to number if it's an object (BigDecimal from Java)
    const numValue = typeof value === 'object' ? parseFloat(value) : value;
    
    // Format as USD
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(numValue);
  };

  // Format crypto amount with appropriate precision
  const formatAmount = (amount) => {
    if (!amount) return '0';
    
    // Convert to number if it's an object (BigDecimal from Java)
    const numAmount = typeof amount === 'object' ? parseFloat(amount) : amount;
    
    // Format based on size
    return numAmount.toLocaleString('en-US', {
      minimumFractionDigits: 8,
      maximumFractionDigits: 8
    });
  };

  // Calculate profit/loss for a transaction
  const calculateProfitLoss = (transaction) => {
    // For BUY transactions, compare purchase price with current price
    if (transaction.transactionType === 'BUY') {
      const currentPrice = cryptoPrices[transaction.cryptoSymbol];
      if (!currentPrice) return null;
      
      const purchasePrice = typeof transaction.price === 'object' ? 
        parseFloat(transaction.price) : transaction.price;
      
      const amount = typeof transaction.amount === 'object' ? 
        parseFloat(transaction.amount) : transaction.amount;
      
      const boughtFor = purchasePrice * amount;
      const currentValue = currentPrice * amount;
      
      return currentValue - boughtFor;
    }
    
    // For SELL transactions, the profit/loss is already realized
    // Return 0 as placeholder
    return 0;
  };

  // Get CSS class for profit/loss
  const getProfitLossClass = (transaction) => {
    if (transaction.transactionType === 'BUY') {
      const profitLoss = calculateProfitLoss(transaction);
      if (profitLoss === null) return '';
      return profitLoss > 0 ? styles.profit : profitLoss < 0 ? styles.loss : '';
    }
    return '';
  };

  // Format profit/loss for display
  const formatProfitLoss = (transaction) => {
    if (transaction.transactionType === 'BUY') {
      const profitLoss = calculateProfitLoss(transaction);
      if (profitLoss === null) return 'N/A';
      
      const formattedValue = formatCurrency(Math.abs(profitLoss));
      return profitLoss >= 0 ? `+${formattedValue}` : `-${formattedValue}`;
    }
    return 'Realized';
  };

  // Initialize data fetching
  useEffect(() => {
    const fetchData = async () => {
      await fetchTransactions();
    };
    
    fetchData();
    
    // Connect to WebSocket for real-time price updates
    const disconnect = cryptoService.connectWebSocket((data) => {
      if (Array.isArray(data) && data.length > 0) {
        // Convert array to object with symbol as key
        const pricesMap = {};
        data.forEach(crypto => {
          pricesMap[crypto.symbol] = crypto.price;
        });
        setCryptoPrices(pricesMap);
      }
    });
    
    // Set up refresh interval for transactions only
    const interval = setInterval(fetchTransactions, 30000);
    
    return () => {
      clearInterval(interval);
      if (disconnect) disconnect();
    };
  }, []);

  // Render loading state
  if (loading && transactions.length === 0) {
    return (
      <div className={styles.container}>
        <h1 className={styles.title}>Transaction History</h1>
        <div className={styles.loadingContainer}>
          <p>Loading transaction history...</p>
        </div>
      </div>
    );
  }

  // Render error state
  if (error && transactions.length === 0) {
    return (
      <div className={styles.container}>
        <h1 className={styles.title}>Transaction History</h1>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Transaction History</h1>
      
      {transactions.length === 0 ? (
        <div className={styles.noTransactions}>
          <p>You don't have any transactions yet.</p>
          <p>Get started by buying some cryptocurrency!</p>
        </div>
      ) : (
        <div className={styles.transactionsContainer}>
          <table className={styles.transactionsTable}>
            <thead>
              <tr>
                <th onClick={() => requestSort('id')}>
                  ID {sortConfig.key === 'id' && (
                    <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
                  )}
                </th>
                <th onClick={() => requestSort('transactionDate')}>
                  Date {sortConfig.key === 'transactionDate' && (
                    <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
                  )}
                </th>
                <th onClick={() => requestSort('transactionType')}>
                  Type {sortConfig.key === 'transactionType' && (
                    <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
                  )}
                </th>
                <th onClick={() => requestSort('cryptoSymbol')}>
                  Crypto {sortConfig.key === 'cryptoSymbol' && (
                    <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
                  )}
                </th>
                <th onClick={() => requestSort('amount')}>
                  Amount {sortConfig.key === 'amount' && (
                    <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
                  )}
                </th>
                <th onClick={() => requestSort('price')}>
                  Price {sortConfig.key === 'price' && (
                    <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
                  )}
                </th>
                <th onClick={() => requestSort('totalValue')}>
                  Total {sortConfig.key === 'totalValue' && (
                    <span>{sortConfig.direction === 'ascending' ? ' ↑' : ' ↓'}</span>
                  )}
                </th>
                <th>
                  Profit/Loss
                </th>
              </tr>
            </thead>
            <tbody>
              {getSortedData().map((transaction) => (
                <tr key={transaction.id}>
                  <td>{transaction.id}</td>
                  <td>{new Date(transaction.transactionDate).toLocaleString()}</td>
                  <td>
                    <div className={`${styles.transactionType} ${transaction.transactionType === 'BUY' ? styles.typeBuy : styles.typeSell}`}>
                      {transaction.transactionType}
                    </div>
                  </td>
                  <td className={styles.transactionSymbol}>{transaction.cryptoSymbol}</td>
                  <td className={styles.transactionAmount}>{formatAmount(transaction.amount)}</td>
                  <td className={styles.transactionPrice}>{formatCurrency(transaction.price)}</td>
                  <td className={styles.transactionValue}>{formatCurrency(transaction.totalValue)}</td>
                  <td className={getProfitLossClass(transaction)}>
                    {formatProfitLoss(transaction)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {loading && <div className={styles.refreshing}>Refreshing data...</div>}
        </div>
      )}
    </div>
  );
}
