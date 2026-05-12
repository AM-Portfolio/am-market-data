$env:REDIS_HOSTNAME="localhost"
$env:REDIS_PORT="6379"
$env:POSTGRES_URL="jdbc:postgresql://localhost:5456"
$env:INFLUXDB_URL="http://localhost:8087"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"

# Load other variables from .env
Get-Content .env | ForEach-Object {
    if ($_ -match '^(?![#\s])([^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        if (-not (Test-Path "env:$name")) {
            Set-Item "env:$name" $value
        }
    }
}

mvn -pl market-data-app spring-boot:run
