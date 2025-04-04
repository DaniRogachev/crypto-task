# Crypto Trading Application

A real-time cryptocurrency trading application that displays prices for the top 20 cryptocurrencies from the Kraken API and allows users to simulate trading.

## Project Structure

The application consists of two main components:

- **Backend**: A Spring Boot application that connects to the Kraken WebSocket API, processes cryptocurrency data, and serves it to the frontend.
- **Frontend**: A Next.js React application that displays real-time cryptocurrency prices and provides a trading interface.

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- npm or yarn
- MySQL 8.0 or higher

## Setting Up the Database

1. Install MySQL if you haven't already
2. Create a new database:
```sql
CREATE DATABASE cryptodb;
```
3. The application uses the following MySQL configuration:
   - URL: `jdbc:mysql://localhost:3306/cryptodb`
   - Username: `root`
   - Password: `root`

## Running the Backend

1. Navigate to the backend directory:
```
cd backend/crypto-task-backend
```

2. Build the application:
```
./gradlew build
```

3. Run the application:
```
./gradlew bootRun
```

The backend server will start on port 8080 by default.

## Running the Frontend

1. Navigate to the frontend directory:
```
cd frontend/crypto-task-frontend
```

2. Install dependencies:
```
npm install
```

3. Run the development server:
```
npm run dev
```

The frontend will be available at http://localhost:3000

## Features

- Real-time cryptocurrency price updates via WebSocket
- Support for the top 20 cryptocurrencies by market cap
- Interactive price charts
- Simulated trading functionality
- User account management

## Technologies Used

- **Backend**: Spring Boot, WebSocket, MySQL
- **Frontend**: Next.js, React, STOMP over SockJS, Chart.js
- **Real-time Data**: Kraken WebSocket API
