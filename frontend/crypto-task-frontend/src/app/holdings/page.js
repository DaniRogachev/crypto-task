'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './page.module.css';

export default function HoldingsPage() {
  const [holdings, setHoldings] = useState([]);
  const [userBalance, setUserBalance] = useState(null);
  const [totalValue, setTotalValue] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const router = useRouter();
  
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        // Fetch user holdings
        const holdingsResponse = await fetch('http://localhost:8080/api/transactions/holdings');
        if (!holdingsResponse.ok) {
          throw new Error(`Error ${holdingsResponse.status}: ${holdingsResponse.statusText}`);
        }
        const holdingsData = await holdingsResponse.json();
        
        // Filter out holdings with zero balance and sort by value (descending)
        const validHoldings = Array.isArray(holdingsData) 
          ? holdingsData
              .filter(holding => holding.balance > 0)
              .sort((a, b) => b.currentValue - a.currentValue)
          : [];
        
        setHoldings(validHoldings);
        
        // Calculate total value of all holdings
        const total = validHoldings.reduce((sum, holding) => sum + parseFloat(holding.currentValue), 0);
        setTotalValue(total);
        
        // Fetch user balance
        const balanceResponse = await fetch('http://localhost:8080/api/transactions/balance');
        if (!balanceResponse.ok) {
          throw new Error(`Error ${balanceResponse.status}: ${balanceResponse.statusText}`);
        }
        const balanceData = await balanceResponse.json();
        
        if (balanceData && balanceData.balance) {
          setUserBalance(balanceData.balance);
        } else {
          console.error('Invalid balance data:', balanceData);
          setError('Failed to load user balance. Please try again.');
        }
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load data: ' + err.message);
      } finally {
        setLoading(false);
      }
    };
    
    fetchData();
    
    // Set up periodic refresh every 30 seconds
    const refreshInterval = setInterval(fetchData, 30000);
    
    // Clean up interval on component unmount
    return () => clearInterval(refreshInterval);
  }, []);
  
  const formatCurrency = (value) => {
    if (value === null || value === undefined) return "$0.00";
    return "$" + parseFloat(value).toLocaleString('en-US', { 
      minimumFractionDigits: 2, 
      maximumFractionDigits: 2 
    });
  };
  
  const formatCryptoAmount = (value) => {
    if (value === null || value === undefined) return "0";
    return parseFloat(value).toLocaleString('en-US', { 
      minimumFractionDigits: 8, 
      maximumFractionDigits: 8 
    });
  };
  
  const handleBuyClick = () => {
    router.push('/buy');
  };
  
  const handleSellClick = (symbol) => {
    router.push('/sell');
  };
  
  const calculateTotalAssets = () => {
    if (userBalance === null || totalValue === null) return 0;
    return parseFloat(userBalance) + parseFloat(totalValue);
  };
  
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Cryptocurrency Holdings</h1>
      
      {error && <p className={styles.error}>{error}</p>}
      
      <div className={styles.summaryContainer}>
        <div className={styles.summaryCard}>
          <h3>USD Balance</h3>
          <p className={styles.summaryValue}>{userBalance !== null ? formatCurrency(userBalance) : "Loading..."}</p>
        </div>
        
        <div className={styles.summaryCard}>
          <h3>Crypto Value</h3>
          <p className={styles.summaryValue}>{formatCurrency(totalValue)}</p>
        </div>
        
        <div className={styles.summaryCard}>
          <h3>Total Assets</h3>
          <p className={styles.summaryValue}>{formatCurrency(calculateTotalAssets())}</p>
        </div>
      </div>
      
      <div className={styles.actionsContainer}>
        <button 
          onClick={handleBuyClick} 
          className={styles.buyButton}
        >
          Buy Cryptocurrency
        </button>
        
        <button 
          onClick={() => handleSellClick()} 
          className={styles.sellButton}
          disabled={holdings.length === 0}
        >
          Sell Cryptocurrency
        </button>
      </div>
      
      {loading ? (
        <div className={styles.loadingContainer}>
          <p>Loading your holdings...</p>
        </div>
      ) : holdings.length === 0 ? (
        <div className={styles.noHoldings}>
          <p>You don't have any cryptocurrency holdings yet.</p>
          <p>Get started by buying some cryptocurrency!</p>
        </div>
      ) : (
        <div className={styles.holdingsContainer}>
          <table className={styles.holdingsTable}>
            <thead>
              <tr>
                <th>Cryptocurrency</th>
                <th>Balance</th>
                <th>Current Price</th>
                <th>Value</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {holdings.map(holding => (
                <tr key={holding.symbol}>
                  <td className={styles.cryptoName}>
                    <span className={styles.cryptoSymbol}>{holding.symbol}</span>
                    {holding.name && <span className={styles.cryptoFullName}>{holding.name}</span>}
                  </td>
                  <td className={styles.cryptoBalance}>
                    {formatCryptoAmount(holding.balance)}
                  </td>
                  <td className={styles.cryptoPrice}>
                    {formatCurrency(holding.currentPrice)}
                  </td>
                  <td className={styles.cryptoValue}>
                    {formatCurrency(holding.currentValue)}
                  </td>
                  <td className={styles.cryptoActions}>
                    <button 
                      onClick={() => handleSellClick(holding.symbol)} 
                      className={styles.sellButtonSmall}
                    >
                      Sell
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
