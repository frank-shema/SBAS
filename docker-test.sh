#!/bin/bash

# Exit on error
set -e

echo "Starting test environment..."

# Build and start the test containers
docker-compose -f docker-compose.test.yml up -d

echo "Running tests..."

# Follow the logs of the test container
docker-compose -f docker-compose.test.yml logs -f test-app

# Get the exit code of the test container
TEST_EXIT_CODE=$(docker-compose -f docker-compose.test.yml ps -q test-app | xargs docker inspect -f '{{.State.ExitCode}}')

# Clean up
echo "Cleaning up test environment..."
docker-compose -f docker-compose.test.yml down

# Exit with the same code as the test container
if [ "$TEST_EXIT_CODE" -eq "0" ]; then
    echo -e "\n\033[0;32mTests passed successfully!\033[0m"
    echo "Test reports are available in the ./test-results directory"
    exit 0
else
    echo -e "\n\033[0;31mTests failed with exit code $TEST_EXIT_CODE\033[0m"
    echo "Check the test reports in the ./test-results directory for more information"
    exit $TEST_EXIT_CODE
fi