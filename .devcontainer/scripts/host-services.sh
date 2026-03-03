#!/bin/bash

echo "=== Host Services Management Script ==="

# Function to check if docker-compose is available
check_docker_compose() {
    if ! command -v docker-compose >/dev/null 2>&1; then
        echo "❌ docker-compose not found. Please install Docker Compose."
        return 1
    fi
    return 0
}

# Function to start host services
start_services() {
    echo "🚀 Starting host services..."

    if check_docker_compose; then
        cd /app
        docker-compose up -d tomcat mysql phpmyadmin

        if [ $? -eq 0 ]; then
            echo "✅ Host services started successfully"
            return 0
        else
            echo "❌ Failed to start host services"
            return 1
        fi
    fi
}

# Function to check service status
check_services() {
    echo "📊 Checking service status..."

    # Check if containers are running
    if command -v docker-compose >/dev/null 2>&1; then
        cd /app
        echo "Docker Compose services:"
        docker-compose ps
    fi

    echo ""
    echo "Port accessibility check:"

    # Check Tomcat (any HTTP response means server is running)
    if curl -s http://localhost:8081 >/dev/null 2>&1; then
        echo "✅ Tomcat (8081): Accessible"
    else
        echo "⚠️  Tomcat (8081): Not accessible"
    fi

    # Check phpMyAdmin
    if curl -s -f http://localhost:8082 >/dev/null 2>&1; then
        echo "✅ phpMyAdmin (8082): Accessible"
    else
        echo "⚠️  phpMyAdmin (8082): Not accessible"
    fi

    # Check MySQL (basic connection test)
    if command -v telnet >/dev/null 2>&1; then
        if timeout 3 telnet localhost 3306 >/dev/null 2>&1; then
            echo "✅ MySQL (3306): Port accessible"
        else
            echo "⚠️  MySQL (3306): Port not accessible"
        fi
    else
        echo "ℹ️  MySQL (3306): Cannot test (telnet not available)"
    fi
}

# Function to stop services
stop_services() {
    echo "🛑 Stopping host services..."

    if check_docker_compose; then
        cd /app
        docker-compose down
        echo "✅ Host services stopped"
    fi
}

# Main execution
case "${1:-start}" in
    "start")
        start_services
        echo ""
        check_services
        ;;
    "status"|"check")
        check_services
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        stop_services
        echo ""
        start_services
        echo ""
        check_services
        ;;
    *)
        echo "Usage: $0 {start|status|stop|restart}"
        echo "  start   - Start host services (default)"
        echo "  status  - Check service status"
        echo "  stop    - Stop host services"
        echo "  restart - Restart host services"
        exit 1
        ;;
esac
