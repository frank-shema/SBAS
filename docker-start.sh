#!/bin/bash

# Exit on error
set -e

echo "Starting Small Business Accounting System..."
echo "Building and starting containers..."

# Build and start the containers
docker-compose up -d

echo "Waiting for the application to start..."
sleep 10  # Initial wait for containers to initialize

# Check if the application is running
MAX_RETRIES=12
RETRY_COUNT=0
APP_URL="http://localhost:8080"

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    echo "Checking if application is up (attempt $(($RETRY_COUNT + 1))/$MAX_RETRIES)..."
    
    if curl -s "$APP_URL/swagger-ui/index.html" > /dev/null; then
        echo -e "\n\033[0;32mApplication is running!\033[0m"
        echo -e "\033[1mAccess URLs:\033[0m"
        echo -e "  Local:       \033[4m$APP_URL\033[0m"
        echo -e "  Swagger UI:  \033[4m$APP_URL/swagger-ui/index.html\033[0m"
        echo -e "  API Docs:    \033[4m$APP_URL/api-docs\033[0m"
        echo -e "\nTo stop the application, run: \033[1mdocker-compose down\033[0m"
        exit 0
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 5
done

echo -e "\n\033[0;31mApplication did not start within the expected time.\033[0m"
echo "Check the logs for more information: docker-compose logs -f app"
echo "To stop the application, run: docker-compose down"
exit 1