#!/bin/bash

echo "=== Tomcat Service Status Check ==="

# Wait for Tomcat service to be ready
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=30
    local attempt=1

    echo "Waiting for $service_name to be ready..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://$host:$port" > /dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi

        echo "⏳ Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 5
        attempt=$((attempt + 1))
    done

    echo "❌ $service_name failed to start within timeout"
    return 1
}

# Check if we're in the main container and Tomcat service exists
if command -v docker-compose > /dev/null 2>&1; then
    echo "Docker Compose is available"

    # Start services if not running
    echo "Starting Tomcat, MySQL, and phpMyAdmin services..."
    docker-compose up -d tomcat mysql phpmyadmin

    # Wait for services to be ready
    wait_for_service "localhost" "8081" "Tomcat"
    wait_for_service "localhost" "3306" "MySQL"
    wait_for_service "localhost" "8082" "phpMyAdmin"else
    echo "Docker Compose not available in this container"
    echo "Services should be managed from the host environment"
fi

# Build and deploy application
echo ""
echo "=== Building and Deploying Application ==="
cd /app
ant clean build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"

    # Check if WAR file exists
    if [ -f "dist/legacy-app.war" ]; then
        echo "✅ WAR file ready: dist/legacy-app.war"
        echo "📦 File size: $(ls -lh dist/legacy-app.war | awk '{print $5}')"
    else
        echo "❌ WAR file not found!"
    fi
else
    echo "❌ Build failed!"
    exit 1
fi

echo ""
echo "=== Service Information ==="
echo "🌐 Tomcat Server: http://localhost:8081"
echo "📱 Application: http://localhost:8081/legacy-app"
echo "🗄️  Database Admin: http://localhost:8082"
echo ""
echo "=== Development Environment Ready! ==="
