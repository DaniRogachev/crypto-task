import styles from "./page.module.css";
import CryptoTable from "./components/CryptoTable";

export default function Home() {
  return (
    <div className={styles.container}>
      <div className={styles.hero}>
        <h1>Real-Time Cryptocurrency Prices</h1>
        <p>Track the top 20 cryptocurrencies by market value with live updates from Kraken API</p>
      </div>
      
      <CryptoTable />
    </div>
  );
}
