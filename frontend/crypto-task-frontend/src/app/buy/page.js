'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from './page.module.css';

export default function BuyPage() {
  const [cryptoSymbol, setCryptoSymbol] = useState('XBT/USD');
  const [quantity, setQuantity] = useState('');
  const [availableCryptos, setAvailableCryptos] = useState([]);
  const [currentPrice, setCurrentPrice] = useState(null);
  const [totalCost, setTotalCost] = useState(0);
  const [userBalance, setUserBalance] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  const router = useRouter();
  
  useEffect(() => {
    // Fetch available cryptos
    fetch('http://localhost:8080/api/crypto/prices')
      .then(response => {
        if (!response.ok) {
          throw new Error(`Error ${response.status}: ${response.statusText}`);
        }
        return response.json();
      })
      .then(data => {
        // Ensure data is an array
        const cryptoArray = Array.isArray(data) ? data : [];
        setAvailableCryptos(cryptoArray);
        
        // Set price for initially selected crypto
        const selectedCrypto = cryptoArray.find(crypto => crypto.symbol === cryptoSymbol);
        if (selectedCrypto) {
          setCurrentPrice(selectedCrypto.price);
        }
      })
      .catch(err => {
        console.error('Error fetching cryptos:', err);
        setError('Failed to load cryptocurrencies. Please try again.');
      });
      
    // Fetch user balance
    fetch('http://localhost:8080/api/transactions/balance')
      .then(response => {
        console.log('Balance response status:', response.status);
        if (!response.ok) {
          throw new Error(`Error ${response.status}: ${response.statusText}`);
        }
        return response.json();
      })
      .then(data => {
        console.log('Balance data:', data);
        if (data && data.balance) {
          setUserBalance(data.balance);
        } else if (data && data.error) {
          throw new Error(data.error);
        } else {
          console.error('Invalid balance data:', data);
          setError('Failed to load user balance. Please try again.');
        }
      })
      .catch(err => {
        console.error('Error fetching user balance:', err);
        setError('Failed to load user balance: ' + err.message);
      });
  }, []);
  
  useEffect(() => {
    if (quantity && currentPrice) {
      setTotalCost(parseFloat(quantity) * parseFloat(currentPrice));
    } else {
      setTotalCost(0);
    }
  }, [quantity, currentPrice]);
  
  const handleSymbolChange = (e) => {
    const selectedSymbol = e.target.value;
    setCryptoSymbol(selectedSymbol);
    
    // Update current price based on selected crypto
    const selectedCrypto = availableCryptos.find(crypto => crypto.symbol === selectedSymbol);
    if (selectedCrypto) {
      setCurrentPrice(selectedCrypto.price);
    }
  };
  
  const handleQuantityChange = (e) => {
    setQuantity(e.target.value);
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccessMessage('');
    
    if (!quantity || parseFloat(quantity) <= 0) {
      setError('Please enter a valid quantity.');
      setLoading(false);
      return;
    }
    
    if (totalCost > (userBalance || 0)) {
      setError('Insufficient balance to complete this purchase.');
      setLoading(false);
      return;
    }
    
    try {
      const response = await fetch('http://localhost:8080/api/transactions/buy', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          symbol: cryptoSymbol,
          quantity: parseFloat(quantity)
        }),
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || `Error: ${response.status}`);
      }
      
      const result = await response.json();
      
      // Update user balance
      const balanceResponse = await fetch('http://localhost:8080/api/transactions/balance');
      if (!balanceResponse.ok) {
        throw new Error(`Failed to refresh balance: ${balanceResponse.status}`);
      }
      
      const balanceData = await balanceResponse.json();
      if (balanceData && balanceData.balance) {
        setUserBalance(balanceData.balance);
      }
      
      setSuccessMessage(`Successfully purchased ${quantity} ${cryptoSymbol}!`);
      setQuantity('');
    } catch (err) {
      console.error('Error buying crypto:', err);
      setError('Failed to complete purchase: ' + err.message);
    } finally {
      setLoading(false);
    }
  };
  
  const formatCurrency = (value) => {
    if (value === null || value === undefined) return "$0.00";
    return "$" + parseFloat(value).toLocaleString('en-US', { 
      minimumFractionDigits: 2, 
      maximumFractionDigits: 2 
    });
  };
  
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Buy Cryptocurrency</h1>
      
      <div className={styles.balanceInfo}>
        <h2>Available Balance: {userBalance !== null ? formatCurrency(userBalance) : "Loading..."}</h2>
      </div>
      
      {error && <p className={styles.error}>{error}</p>}
      {successMessage && <p className={styles.success}>{successMessage}</p>}
      
      <form onSubmit={handleSubmit} className={styles.buyForm}>
        <div className={styles.formGroup}>
          <label htmlFor="cryptoSymbol">Select Cryptocurrency:</label>
          <select
            id="cryptoSymbol"
            value={cryptoSymbol}
            onChange={handleSymbolChange}
            className={styles.select}
            disabled={availableCryptos.length === 0}
          >
            {availableCryptos.length > 0 ? (
              availableCryptos.map(crypto => (
                <option key={crypto.symbol} value={crypto.symbol}>
                  {crypto.name ? `${crypto.name} (${crypto.symbol})` : crypto.symbol}
                </option>
              ))
            ) : (
              <option value="">Loading cryptocurrencies...</option>
            )}
          </select>
        </div>
        
        {currentPrice && (
          <div className={styles.priceInfo}>
            <p>Current Price: {formatCurrency(currentPrice)}</p>
          </div>
        )}
        
        <div className={styles.formGroup}>
          <label htmlFor="quantity">Quantity:</label>
          <input
            id="quantity"
            type="number"
            step="0.00000001"
            min="0.00000001"
            value={quantity}
            onChange={handleQuantityChange}
            className={styles.input}
            placeholder="Enter amount to buy"
            required
          />
        </div>
        
        {totalCost > 0 && (
          <div className={styles.costSummary}>
            <p>Total Cost: {formatCurrency(totalCost)}</p>
            {totalCost > (userBalance || 0) && (
              <p className={styles.error}>Insufficient balance!</p>
            )}
          </div>
        )}
        
        <button 
          type="submit" 
          className={styles.buyButton}
          disabled={loading || !quantity || parseFloat(quantity) <= 0 || totalCost > (userBalance || 0) || availableCryptos.length === 0}
        >
          {loading ? 'Processing...' : 'Buy Now'}
        </button>
      </form>
    </div>
  );
}
