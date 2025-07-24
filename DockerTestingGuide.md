# Docker Testing Guide for Small Business Accounting System

This guide provides instructions for using Docker to build, run, and test the Small Business Accounting System.

## Prerequisites

- Docker
- Docker Compose

## Building and Running the Application

### Option 1: Using the docker-start.sh Script (Recommended)

The easiest way to run the application is using the provided script:

```bash
# Make the script executable
chmod +x docker-start.sh

# Start the application
./docker-start.sh
```

This script will:
1. Build and start the containers using Docker Compose
2. Wait for the application to start
3. Provide information about accessing the application
4. Give instructions for stopping the application

### Option 2: Using Docker Compose Manually

If you prefer to run the application manually using Docker Compose:

1. Build and start the containers:
   ```bash
   docker-compose up -d
   ```

   This command will:
   - Build the application image using the Dockerfile
   - Pull the PostgreSQL image if it's not already available
   - Start both containers
   - Create a network for communication between containers
   - Create a volume for persistent database storage

2. Check if the containers are running:
   ```bash
   docker-compose ps
   ```

   You should see two containers running: `accounting_app` and `accounting_db`.

3. View the application logs:
   ```bash
   docker-compose logs -f app
   ```

   Press Ctrl+C to stop following the logs.

## Accessing the Application

Once the application is running, you can access it at:

- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/api-docs

## Running Tests

You can run the application's tests in a Docker container to ensure consistent test environments.

### Option 1: Using the docker-test.sh Script (Recommended)

The easiest way to run tests is using the provided script:

```bash
# Make the script executable
chmod +x docker-test.sh

# Run the tests
./docker-test.sh
```

This script will:
1. Start a PostgreSQL container for testing
2. Build and run the application in a test container
3. Show the test logs in real-time
4. Clean up the test environment when done
5. Report whether the tests passed or failed

Test reports will be available in the `./test-results` directory.

### Option 2: Using Docker Compose Manually

If you prefer to run tests manually:

```bash
# Start the test environment
docker-compose -f docker-compose.test.yml up -d

# View test logs
docker-compose -f docker-compose.test.yml logs -f test-app

# Clean up when done
docker-compose -f docker-compose.test.yml down
```

### Option 3: Running Tests in a Simple Docker Container

For a simpler approach without a separate database:

```bash
# Run all tests
docker run --rm -v $(pwd):/app -w /app eclipse-temurin:21-jdk-alpine ./mvnw test

# Run specific tests
docker run --rm -v $(pwd):/app -w /app eclipse-temurin:21-jdk-alpine ./mvnw test -Dtest=AccountControllerTest
```

Replace `AccountControllerTest` with the name of the test class you want to run.

## Common Docker Commands

### Stopping the Application

```bash
docker-compose down
```

### Rebuilding the Application

```bash
docker-compose build --no-cache
```

### Viewing Container Logs

```bash
# View logs for all containers
docker-compose logs

# View logs for a specific container
docker-compose logs app
docker-compose logs db

# Follow logs in real-time
docker-compose logs -f app
```

### Accessing the Database

```bash
# Connect to the PostgreSQL database
docker-compose exec db psql -U postgres -d accountingdb
```

### Running Commands in the Application Container

```bash
# Open a shell in the application container
docker-compose exec app sh

# Run a Maven command
docker-compose exec app ./mvnw clean package
```

## Testing with Sample Data

You can use the sample data provided in the `SampleData.md` file to test the application. Here's how to use it with Docker:

1. Start the application using Docker Compose
2. Access the Swagger UI at http://localhost:8080/swagger-ui/index.html
3. Register a user and get a JWT token
4. Use the sample data to create accounts, transactions, budgets, and invoices
5. Test the various API endpoints

## Environment Variables

The Docker Compose configuration uses the following environment variables:

### Application Container

- `SPRING_PROFILES_ACTIVE`: The Spring profile to use (default: `prod`)
- `SPRING_DATASOURCE_URL`: The JDBC URL for the database
- `SPRING_DATASOURCE_USERNAME`: The database username
- `SPRING_DATASOURCE_PASSWORD`: The database password

### Database Container

- `POSTGRES_DB`: The name of the database to create
- `POSTGRES_USER`: The username for the database
- `POSTGRES_PASSWORD`: The password for the database

You can override these variables by creating a `.env` file in the project root or by setting them in your environment.

## Troubleshooting

### Application Fails to Start

If the application fails to start, check the logs:

```bash
docker-compose logs app
```

Common issues include:
- Database connection problems
- Port conflicts
- Memory limitations

### Database Connection Issues

If the application can't connect to the database, make sure the database container is running:

```bash
docker-compose ps db
```

If it's not running, check the database logs:

```bash
docker-compose logs db
```

### Tests Fail in Docker

If tests fail when running in Docker but pass locally, it could be due to:
- Different database configuration
- Timing issues
- Resource constraints

Try increasing the timeout values in the tests or allocating more resources to Docker.