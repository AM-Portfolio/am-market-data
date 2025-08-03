# Database Connection Guide for Market Data Application

This guide provides detailed instructions for connecting to the various databases used in the Market Data application, both locally and in Kubernetes (AKS).

## Table of Contents
- [MongoDB Connection](#mongodb-connection)
- [PostgreSQL Connection](#postgresql-connection)
- [InfluxDB Connection](#influxdb-connection)
- [Testing Database Connections](#testing-database-connections)
- [Troubleshooting](#troubleshooting)

## MongoDB Connection

### Local Development

**Connection String Format:**
```
mongodb://<username>:<password>@localhost:27017/<database>?authSource=admin
```

**Example in application.yml:**
```yaml
MONGO_URL: mongodb://ssd2658:ssd2658@localhost:27017/market_data?authSource=admin
MONGO_DATABASE: market_data
MONGO_USERNAME: ssd2658
MONGO_PASSWORD: ssd2658
```

**Connection Test Command:**
```bash
mongosh "mongodb://ssd2658:ssd2658@localhost:27017/market_data?authSource=admin"
```

### Kubernetes (AKS) Environment

**Connection String Format:**
```
mongodb://<username>:<password>@mongodb.<namespace>.svc.cluster.local:27017/<database>?authSource=admin
```

**Example in application.yml:**
```yaml
MONGO_URL: mongodb://admin:password@mongodb.dev.svc.cluster.local:27017/market_data?authSource=admin
MONGO_DATABASE: market_data
MONGO_USERNAME: admin
MONGO_PASSWORD: password
```

**Port-Forward for Local Testing:**
```bash
kubectl port-forward svc/mongodb -n dev 27017:27017
```

## PostgreSQL Connection

### Local Development

**Connection String Format:**
```
jdbc:postgresql://localhost:<port>/<database>
```

**Example in application.yml:**
```yaml
POSTGRES_URL: jdbc:postgresql://localhost:5456
POSTGRES_DATABASE: portfolio
POSTGRES_USERNAME: postgres
POSTGRES_PASSWORD: password
```

**Connection Test Command:**
```bash
psql -h localhost -p 5456 -U postgres -d portfolio
```

### Kubernetes (AKS) Environment

**Connection String Format:**
```
jdbc:postgresql://postgresql.<namespace>.svc.cluster.local:5432/<database>
```

**Example in application.yml:**
```yaml
POSTGRES_URL: jdbc:postgresql://postgresql.dev.svc.cluster.local:5432
POSTGRES_DATABASE: portfolio
POSTGRES_USERNAME: postgres
POSTGRES_PASSWORD: password
```

**Port-Forward for Local Testing:**
```bash
kubectl port-forward svc/postgresql -n dev 5432:5432
```

## InfluxDB Connection

### Local Development

**Connection Format:**
```
http://localhost:8086
```

**Example in application.yml:**
```yaml
INFLUXDB_TOKEN: my-super-secret-auth-token
INFLUXDB_ORG: am_investment
INFLUXDB_BUCKET: market_data
INFLUXDB_URL: http://localhost:8086
```

**Connection Test Command:**
```bash
curl -G "http://localhost:8086/api/v2/health" -H "Authorization: Token my-super-secret-auth-token"
```

### Kubernetes (AKS) Environment

**Connection Format:**
```
http://influxdb.<namespace>.svc.cluster.local:8086
```

**Example in application.yml:**
```yaml
INFLUXDB_TOKEN: my-super-secret-auth-token
INFLUXDB_ORG: am_portfolio
INFLUXDB_BUCKET: market_data
INFLUXDB_URL: http://influxdb.dev.svc.cluster.local:8086
```

**Port-Forward for Local Testing:**
```bash
kubectl port-forward svc/influxdb -n dev 8086:8086
```

## Testing Database Connections

### Spring Boot Application Tests

The application includes connection test utilities for each database:

1. **MongoDB**: `MongoDBConnectionTester` runs on application startup in dev profile
2. **PostgreSQL**: JPA auto-configuration tests the connection on startup
3. **InfluxDB**: `InfluxDBConfig` establishes the connection on startup

### Manual Connection Tests

#### MongoDB
```bash
# From local machine with port-forwarding
mongosh "mongodb://admin:password@localhost:27017/market_data?authSource=admin"

# From a pod in the cluster
kubectl exec -it mongodb-0 -n dev -- mongosh "mongodb://admin:password@localhost:27017/market_data?authSource=admin"
```

#### PostgreSQL
```bash
# From local machine with port-forwarding
psql -h localhost -p 5432 -U postgres -d portfolio

# From a pod in the cluster
kubectl exec -it postgresql-0 -n dev -- psql -U postgres -d portfolio
```

#### InfluxDB
```bash
# From local machine with port-forwarding
curl -G "http://localhost:8086/api/v2/health" -H "Authorization: Token my-super-secret-auth-token"

# From a pod in the cluster
kubectl exec -it influxdb-0 -n dev -- curl -G "http://localhost:8086/api/v2/health" -H "Authorization: Token my-super-secret-auth-token"
```

## Troubleshooting

### Common MongoDB Issues

1. **Authentication Failed**: Check username/password in secrets and connection string
2. **Connection Refused**: Ensure MongoDB pod is running and service is correctly defined
3. **Database Not Found**: Verify database name and that it exists

### Common PostgreSQL Issues

1. **Connection Refused**: Check if PostgreSQL pod is running and service is correctly defined
2. **Authentication Failed**: Verify username/password in secrets
3. **Database Does Not Exist**: Create the database if it doesn't exist

### Common InfluxDB Issues

1. **Invalid Token**: Verify token in secrets matches the one in your connection
2. **Organization Not Found**: Check organization name in configuration
3. **Bucket Not Found**: Ensure bucket exists and is correctly named

### Kubernetes-Specific Troubleshooting

```bash
# Check pod status
kubectl get pods -n dev

# View pod logs
kubectl logs <pod-name> -n dev

# Check service endpoints
kubectl get endpoints -n dev

# Describe a pod for detailed status
kubectl describe pod <pod-name> -n dev

# Check persistent volume claims
kubectl get pvc -n dev
```
