'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import styles from '../buy/page.module.css'; // Reusing styles from the buy page

export default function SellPage() {
  const [cryptoSymbol, setCryptoSymbol] = useState('');
  const [quantity, setQuantity] = useState('');
  const [availableHoldings, setAvailableHoldings] = useState([]);
  const [currentPrice, setCurrentPrice] = useState(null);
  const [currentBalance, setCurrentBalance] = useState(null);
  const [totalValue, setTotalValue] = useState(0);
  const [userBalance, setUserBalance] = useState(null);
  const [maxQuantity, setMaxQuantity] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  const router = useRouter();
  
  useEffect(() => {
    // Fetch user holdings
    fetch('http://localhost:8080/api/transactions/holdings')
      .then(response => {
        if (!response.ok) {
          throw new Error(`Error ${response.status}: ${response.statusText}`);
        }
        return response.json();
      })
      .then(data => {
        // Filter out holdings with zero balance
        const validHoldings = Array.isArray(data) ? data.filter(holding => holding.balance > 0) : [];
        setAvailableHoldings(validHoldings);
        
        // Set default crypto if available
        if (validHoldings.length > 0) {
          const firstHolding = validHoldings[0];
          setCryptoSymbol(firstHolding.symbol);
          setCurrentPrice(firstHolding.currentPrice);
          setCurrentBalance(firstHolding.balance);
          setMaxQuantity(firstHolding.balance);
        }
      })
      .catch(err => {
        console.error('Error fetching holdings:', err);
        setError('Failed to load crypto holdings. Please try again.');
      });
      
    // Fetch user balance
    fetch('http://localhost:8080/api/transactions/balance')
      .then(response => {
        if (!response.ok) {
          throw new Error(`Error ${response.status}: ${response.statusText}`);
        }
        return response.json();
      })
      .then(data => {
        if (data && data.balance) {
          setUserBalance(data.balance);
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
      setTotalValue(parseFloat(quantity) * parseFloat(currentPrice));
    } else {
      setTotalValue(0);
    }
  }, [quantity, currentPrice]);
  
  const handleSymbolChange = (e) => {
    const selectedSymbol = e.target.value;
    setCryptoSymbol(selectedSymbol);
    
    // Find the holding with the selected symbol
    const selectedHolding = availableHoldings.find(holding => holding.symbol === selectedSymbol);
    if (selectedHolding) {
      setCurrentPrice(selectedHolding.currentPrice);
      setCurrentBalance(selectedHolding.balance);
      setMaxQuantity(selectedHolding.balance);
      setQuantity(''); // Reset quantity when changing symbol
    }
  };
  
  const handleQuantityChange = (e) => {
    const newQuantity = e.target.value;
    if (!newQuantity || parseFloat(newQuantity) <= parseFloat(maxQuantity)) {
      setQuantity(newQuantity);
    }
  };
  
  const handleMaxQuantity = () => {
    setQuantity(maxQuantity.toString());
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
    
    if (parseFloat(quantity) > parseFloat(maxQuantity)) {
      setError(`You can only sell up to ${maxQuantity} ${cryptoSymbol}`);
      setLoading(false);
      return;
    }
    
    try {
      const response = await fetch('http://localhost:8080/api/transactions/sell', {
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
      
      // Refresh holdings
      const holdingsResponse = await fetch('http://localhost:8080/api/transactions/holdings');
      if (!holdingsResponse.ok) {
        throw new Error(`Failed to refresh holdings: ${holdingsResponse.status}`);
      }
      
      const holdingsData = await holdingsResponse.json();
      const validHoldings = Array.isArray(holdingsData) ? holdingsData.filter(holding => holding.balance > 0) : [];
      setAvailableHoldings(validHoldings);
      
      // Update current holding data
      const updatedHolding = validHoldings.find(holding => holding.symbol === cryptoSymbol);
      if (updatedHolding) {
        setCurrentBalance(updatedHolding.balance);
        setMaxQuantity(updatedHolding.balance);
      } else {
        // If all of this crypto was sold, reset crypto selection if other holdings exist
        if (validHoldings.length > 0) {
          const firstHolding = validHoldings[0];
          setCryptoSymbol(firstHolding.symbol);
          setCurrentPrice(firstHolding.currentPrice);
          setCurrentBalance(firstHolding.balance);
          setMaxQuantity(firstHolding.balance);
        } else {
          setCryptoSymbol('');
          setCurrentPrice(null);
          setCurrentBalance(null);
          setMaxQuantity(0);
        }
      }
      
      setSuccessMessage(`Successfully sold ${quantity} ${cryptoSymbol} for ${formatCurrency(totalValue)}!`);
      setQuantity('');
    } catch (err) {
      console.error('Error selling crypto:', err);
      setError('Failed to complete sale: ' + err.message);
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
  
  const formatCryptoAmount = (value) => {
    if (value === null || value === undefined) return "0";
    return parseFloat(value).toLocaleString('en-US', { 
      minimumFractionDigits: 8, 
      maximumFractionDigits: 8 
    });
  };
  
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Sell Cryptocurrency</h1>
      
      <div className={styles.balanceInfo}>
        <h2>Available Balance: {userBalance !== null ? formatCurrency(userBalance) : "Loading..."}</h2>
      </div>
      
      {error && <p className={styles.error}>{error}</p>}
      {successMessage && <p className={styles.success}>{successMessage}</p>}
      
      {availableHoldings.length === 0 ? (
        <div className={styles.noHoldings}>
          <p>You don't have any cryptocurrency holdings to sell.</p>
          <button 
            onClick={() => router.push('/buy')} 
            className={styles.buyButton}
          >
            Buy Cryptocurrency
          </button>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className={styles.buyForm}>
          <div className={styles.formGroup}>
            <label htmlFor="cryptoSymbol">Select Cryptocurrency:</label>
            <select
              id="cryptoSymbol"
              value={cryptoSymbol}
              onChange={handleSymbolChange}
              className={styles.select}
              disabled={availableHoldings.length === 0}
            >
              {availableHoldings.map(holding => (
                <option key={holding.symbol} value={holding.symbol}>
                  {holding.name ? `${holding.name} (${holding.symbol})` : holding.symbol}
                </option>
              ))}
            </select>
          </div>
          
          {currentPrice && (
            <div className={styles.priceInfo}>
              <p>Current Price: {formatCurrency(currentPrice)}</p>
              <p>Your Balance: {formatCryptoAmount(currentBalance)} {cryptoSymbol}</p>
            </div>
          )}
          
          <div className={styles.formGroup}>
            <label htmlFor="quantity">Quantity to Sell:</label>
            <div className={styles.quantityContainer}>
              <input
                id="quantity"
                type="number"
                step="0.00000001"
                min="0.00000001"
                max={maxQuantity}
                value={quantity}
                onChange={handleQuantityChange}
                className={styles.input}
                placeholder="Enter amount to sell"
                required
              />
              <button 
                type="button" 
                className={styles.maxButton}
                onClick={handleMaxQuantity}
              >
                MAX
              </button>
            </div>
          </div>
          
          {totalValue > 0 && (
            <div className={styles.costSummary}>
              <p>Total Value: {formatCurrency(totalValue)}</p>
            </div>
          )}
          
          <button 
            type="submit" 
            className={styles.buyButton}
            disabled={loading || !quantity || parseFloat(quantity) <= 0 || parseFloat(quantity) > parseFloat(maxQuantity)}
          >
            {loading ? 'Processing...' : 'Sell Now'}
          </button>
        </form>
      )}
    </div>
  );
}
