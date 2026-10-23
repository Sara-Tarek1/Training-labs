# Lab 8: Containerized Node.js and MySQL Stack Using Docker Compose

## Objective

Deploy a Node.js web application and a MySQL database using Docker Compose. Configure networking, environment variables, persistent volumes, and publish the application image to Docker Hub.

## Architecture

- **App:** Node.js application running on port 3000.
- **Database:** MySQL 8.0 with a database named `ivolve`.
- **Network:** `ivolve-net` allows the application and database containers to communicate.
- **App Logs Volume:** Persists application logs from `/app/logs`.
- **Database Volume:** Persists MySQL data from `/var/lib/mysql`.

## Technologies Used

- Docker
- Docker Compose
- Node.js
- MySQL 8.0
- Docker Hub

## Project Files

- `Dockerfile` — builds the Node.js application image.
- `docker-compose.yml` — defines the app and database services, networking, environment variables, and volumes.
- `server.js` — application server and health/readiness endpoints.
- `db.js` — database initialization logic.
- `package.json` — Node.js dependencies.
- `frontend/` — frontend application files.

## Configuration

The application uses the following environment variables:

| Variable | Description |
|---|---|
| `DB_HOST` | MySQL Compose service name (`db`) |
| `DB_USER` | MySQL username (`root`) |
| `DB_PASSWORD` | Lab database password |
| `DB_NAME` | Database name (`ivolve`) |

The database is configured using `MYSQL_ROOT_PASSWORD` and `MYSQL_DATABASE`.

> The password in the Compose file is for lab use only. Use secrets or environment files for real deployments.

## Run the Application

Build and start the services:

```bash
docker compose up --build -d
```

Check service status:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs --tail=50 app
```

## Verify Application and Database Connectivity

Test the health endpoint:

```bash
curl -i http://localhost:3000/health
```

Test the readiness endpoint:

```bash
curl -i http://localhost:3000/ready
```

A successful response should return HTTP `200 OK` and indicate that the application can query the database.

## Persistent Storage and Networking

List Docker volumes:

```bash
docker volume ls
```

List Docker networks:

```bash
docker network ls
```

The Compose project creates persistent volumes for application logs and MySQL data, and a bridge network for communication between services.

## Docker Hub

Image: `saratarek1/ivolve-app:lab8`

Build the application image:

```bash
docker compose build app
```

Push it to Docker Hub:

```bash
docker push saratarek1/ivolve-app:lab8
```

The image must be tagged and pushed using an account with permission to access the `ivolve-app` repository.

## Cleanup

Stop and remove the Compose containers and network:

```bash
docker compose down
```

The named volumes are retained by this command, so MySQL data and application logs can persist for a later run.

