# Azure AKS Deployment for Market Data Services

This directory contains Kubernetes deployment files for the core infrastructure services required by the AM Portfolio Market Data application.

## Services Included

- **MongoDB**: Document database for storing market data
- **Redis**: In-memory cache for fast data access
- **Kafka**: Message broker for event streaming
- **InfluxDB**: Time-series database for metrics storage

## Prerequisites

- Azure CLI installed and configured
- kubectl installed and configured
- Access to an Azure Kubernetes Service (AKS) cluster

## Deployment Instructions

### 1. Connect to your AKS Cluster

```bash
# Login to Azure
az login

# Set your subscription
az account set --subscription <your-subscription-id>

# Get AKS credentials
az aks get-credentials --resource-group <your-resource-group> --name <your-cluster-name>
```

### 2. Create the Namespace

```bash
kubectl apply -f deploy-all.yaml
```

This will create the `market-data` namespace and deploy MongoDB. For other services, deploy them individually:

### 3. Deploy Individual Services

#### MongoDB

```bash
kubectl apply -f mongodb/secret.yaml
kubectl apply -f mongodb/deployment.yaml
kubectl apply -f mongodb/service.yaml
```

#### Redis

```bash
kubectl apply -f redis/secret.yaml
kubectl apply -f redis/configmap.yaml
kubectl apply -f redis/deployment.yaml
kubectl apply -f redis/service.yaml
```

#### Kafka (includes ZooKeeper)

```bash
kubectl apply -f kafka/secret.yaml
kubectl apply -f kafka/jaas-configmap.yaml
kubectl apply -f kafka/zookeeper.yaml
kubectl apply -f kafka/zookeeper-service.yaml
kubectl apply -f kafka/kafka.yaml
kubectl apply -f kafka/kafka-service.yaml
```

#### InfluxDB

```bash
kubectl apply -f influxdb/secret.yaml
kubectl apply -f influxdb/configmap.yaml
kubectl apply -f influxdb/deployment.yaml
kubectl apply -f influxdb/service.yaml
```

### 4. Verify Deployments

```bash
kubectl get all -n market-data
```

## Configuration

### Storage Classes

These deployments use `default` storage class, which is available in Azure AKS. If you're using a different storage class, update the `storageClassName` field in the StatefulSet definitions.

### Resource Limits

Resource requests and limits are set conservatively. Adjust them based on your workload requirements:

- MongoDB: 256Mi-1Gi memory, 200m-500m CPU
- Redis: 128Mi-512Mi memory, 100m-300m CPU
- Kafka: 512Mi-1Gi memory, 200m-500m CPU
- InfluxDB: 512Mi-1Gi memory, 200m-500m CPU

### Security Notes

- For production deployments, replace the base64-encoded secrets with Azure Key Vault integration
- Enable network policies to restrict communication between services
- Consider using Azure Private Link for external database services

## Integration with Grafana Agent

To monitor these services with Grafana Cloud:

1. Deploy the Grafana Agent using the configuration in `../monitoring/agent-config.yaml`
2. Add service discovery for these Kubernetes services in the agent config

## Troubleshooting

### Checking Logs

```bash
kubectl logs -f -l app=mongodb -n dev
kubectl logs -f -l app=redis -n dev
kubectl logs -f -l app=kafka -n dev
kubectl logs -f -l app=influxdb -n dev
```

### Accessing Services

```bash
# Port forward to access services locally
kubectl port-forward svc/mongodb 27017:27017 -n dev
kubectl port-forward svc/redis 6379:6379 -n dev
kubectl port-forward svc/kafka 9092:9092 -n dev
kubectl port-forward svc/influxdb 8086:8086 -n dev
```
