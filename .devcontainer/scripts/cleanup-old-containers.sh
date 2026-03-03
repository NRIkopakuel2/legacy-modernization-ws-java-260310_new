#!/bin/bash
# Cleanup script to remove old/orphaned containers before starting dev environment
# This prevents port conflicts and ensures clean startup

set -e

echo "🧹 Cleaning up old/orphaned containers..."

# Remove any old phpMyAdmin containers that don't match current config
echo "Checking for old phpMyAdmin containers..."
docker ps -a --filter "name=phpmyadmin" --format "{{.Names}}" 2>/dev/null | while read container; do
    if [ "$container" != "legacy-phpmyadmin" ]; then
        echo "  ⚠️  Removing old container: $container"
        docker rm -f "$container" 2>/dev/null || true
    else
        echo "  ✅ Current container found: $container"
    fi
done

# Remove orphaned containers from old compose files
echo "Removing orphaned containers..."
cd "$(dirname "$0")/.." || exit 1
docker compose -f compose.services.yaml down --remove-orphans 2>/dev/null || true

echo "✅ Cleanup complete!"
