'use client';

import Link from 'next/link';
import { useState } from 'react';
import axios from 'axios';
import styles from './Header.module.css';

// API base URL
const API_BASE_URL = 'http://localhost:8080/api';

export default function Header() {
  const [isResetting, setIsResetting] = useState(false);

  const resetAccount = async () => {
    try {
      setIsResetting(true);
      const response = await axios.post(`${API_BASE_URL}/transactions/reset`, {}, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      // Refresh the page to update all components with the reset data
      window.location.reload();
      
      alert('Account reset successful! Your balance has been restored to $10,000.');
    } catch (error) {
      console.error('Error resetting account:', error);
      alert('Failed to reset account. Please try again later.');
    } finally {
      setIsResetting(false);
    }
  };

  return (
    <header className={styles.header}>
      <div className={styles.logo}>
        <Link href="/">
          Crypto Trading App
        </Link>
      </div>
      <nav className={styles.nav}>
        <Link href="/" className={styles.navLink}>
          Table
        </Link>
        <Link href="/buy" className={styles.navLink}>
          Buy
        </Link>
        <Link href="/sell" className={styles.navLink}>
          Sell
        </Link>
        <Link href="/holdings" className={styles.navLink}>
          Holdings
        </Link>
        <Link href="/transactions" className={styles.navLink}>
          Transaction History
        </Link>
        <button 
          className={styles.resetButton}
          onClick={resetAccount}
          disabled={isResetting}
        >
          {isResetting ? 'Resetting...' : 'Reset Account'}
        </button>
      </nav>
    </header>
  );
}
