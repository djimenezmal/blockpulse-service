# Fee Market Comparator

A Spring Boot application that analyzes and compares fee markets for Bitcoin (BTC) and Kaspa cryptocurrencies using real-time WebSocket data streams.

## Features

- **Real-time Transaction Monitoring**: WebSocket connections to BTC and Kaspa networks
- **Fee Analysis**: Comprehensive fee-per-byte statistics including median, average, percentiles
- **Outlier Detection**: Identifies transactions with unusually high or low fees
- **Spam Detection**: Detects suspicious transaction patterns
- **Block Analysis**: Analyzes block fullness, fee wars, and congestion patterns
- **WebSocket API**: Real-time data streaming to frontend applications
- **REST API**: Historical data access and analysis endpoints
- **Docker Support**: Containerized deployment ready

## Architecture

### Core Components

- **WebSocket Clients**: Connect to BTC and Kaspa APIs for real-time data
- **Transaction Service**: Processes and analyzes incoming transactions
- **Fee Analysis Service**: Generates comprehensive fee market statistics
- **WebSocket Controller**: Provides real-time data streams to clients
- **REST Controller**: Exposes HTTP endpoints for data access

### Data Models

- **Transaction**: Individual transaction data with fee analysis
- **BlockStats**: Aggregated block-level statistics
- **FeeAnalysisDTO**: Comprehensive fee market analysis response

## API Endpoints

### WebSocket Endpoints

Connect to `ws://localhost:8080/ws` and subscribe to:

- `/topic/analysis/btc` - Real-time BTC fee analysis
- `/topic/analysis/kaspa` - Real-time Kaspa fee analysis
- `/topic/transactions/btc` - Individual BTC transactions
- `/topic/transactions/kaspa` - Individual Kaspa transactions
- `/topic/blocks/btc` - BTC block statistics
- `/topic/blocks/kaspa` - Kaspa block statistics

### REST API Endpoints

#### Fee Analysis
- `GET /api/v1/analysis/{currency}` - Current fee analysis
- `GET /api/v1/transactions/{currency}` - Recent transactions
- `GET /api/v1/transactions/{currency}/outliers` - Outlier transactions
- `GET /api/v1/transactions/{currency}/spam` - Spam transactions

#### Block Analysis
- `GET /api/v1/blocks/{currency}` - Recent block statistics
- `GET /api/v1/blocks/{currency}/fee-wars` - Blocks with fee wars

#### Historical Data
- `GET /api/v1/transactions/{currency}/range` - Transactions by date range
- `GET /api/v1/blocks/{currency}/range` - Blocks by date range

#### Health Check
- `GET /api/v1/health` - Application health status

## External APIs Used

### Bitcoin (BTC)
- **Primary**: Blockchain.info WebSocket API (`wss://ws.blockchain.info/inv`)
- **Alternative**: Mempool.space API (`wss://mempool.space/api/v1/ws`)

### Kaspa
- **Primary**: Kaspa Live API (`wss://api.kaspa.org/ws`)
- **Alternative**: Kaspa Explorer API (check kaspa.org for current endpoints)

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (optional)

### Local Development

1. **Clone and Build**
```bash
git clone <repository-url>
cd totalFee-market-comparator
./mvnw clean package
```

2. **Run Application**
```bash
./mvnw spring-boot:run
```

3. **Access Application**
- Application: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
- Health Check: http://localhost:8080/api/v1/health

### Docker Deployment

1. **Build and Run with Docker Compose**
```bash
docker-compose up --build
```

2. **Or Build Docker Image Manually**
```bash
docker build -t totalFee-market-comparator .
docker run -p 8080:8080 totalFee-market-comparator
```

## Configuration

### Application Properties (application.yml)

Key configuration options:

```yaml
app:
  websocket:
    reconnect-delay: 5000
    max-reconnect-attempts: 10
  analysis:
    outlier-threshold: 3.0
    spam-detection-enabled: true
    block-analysis-enabled: true
```

### Environment Variables

- `SPRING_PROFILES_ACTIVE` - Active Spring profiles
- `SERVER_PORT` - Application port (default: 8080)

## Development

### Project Structure
```
src/main/java/com/crypto/feemarketcomparator/
├── client/           # WebSocket clients for external APIs
├── config/           # Spring configuration classes
├── controller/       # REST and WebSocket controllers  
├── dto/              # Data Transfer Objects
├── model/            # JPA entities
├── repository/       # Data repositories
└── service/          # Business logic services
```

### Key Classes

- `FeeMarketComparatorApplication` - Main Spring Boot application
- `TransactionService` - Core transaction processing logic
- `FeeAnalysisService` - Fee market analysis algorithms
- `BTCWebSocketClient` - Bitcoin WebSocket client
- `KaspaWebSocketClient` - Kaspa WebSocket client

### Database Schema

The application uses H2 in-memory database with two main tables:

- `transactions` - Individual transaction records
- `block_stats` - Aggregated block statistics

## Testing

Run the test suite:
```bash
./mvnw test
```

## Monitoring

The application includes:

- Spring Boot Actuator endpoints at `/actuator/*`
- Health checks at `/api/v1/health`
- Application metrics and monitoring
- Docker health checks

## Frontend Integration

### React Example

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  onConnect: () => {
    client.subscribe('/topic/analysis/btc', (message) => {
      const analysis = JSON.parse(message.body);
      console.log('BTC Analysis:', analysis);
    });
  }
});

client.activate();
```

## Production Considerations

1. **External API Rate Limits**: Monitor and respect API rate limits
2. **Database**: Switch to persistent database (PostgreSQL, MySQL) for production
3. **Security**: Add authentication and rate limiting for public APIs
4. **Monitoring**: Implement comprehensive logging and monitoring
5. **Scaling**: Consider horizontal scaling for high-throughput scenarios

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- Check the GitHub Issues page
- Review API documentation
- Check application logs for debugging

---

**Note**: This application connects to external cryptocurrency APIs. Ensure you comply with their terms of service and rate limiting requirements.