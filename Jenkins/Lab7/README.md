# Jenkins Lab 7 – Docker Microservices

## 1. Lab Objective

Deploy a microservices application using Docker containers, Docker networks, volumes, and bind mounts.

## 2. Application Architecture

The application consists of three services:

- **Frontend:** Provides the web interface for sending and viewing messages.
- **Backend:** Processes messages, stores them in `messages.json`, and sends notification requests.
- **Notification:** Receives notification requests and records them in its logs.

## 3. Build Docker Images

Build an image for each service:

```bash
docker build -t lab7-backend:latest ./backend
docker build -t lab7-notification:latest ./notification
docker build -t lab7-frontend:latest ./frontend
```

## 4. Create Docker Networks

Create separate bridge networks for communication between the services:

```bash
docker network create --driver bridge --subnet 172.20.1.0/24 frontend-backend
docker network create --driver bridge --subnet 172.20.2.0/24 backend-notification
```

- `frontend-backend`: Connects the Frontend and Backend.
- `backend-notification`: Connects the Backend and Notification.

## 5. Create Docker Volumes

Create volumes to preserve application logs:

```bash
docker volume create backend-logs
docker volume create notification-logs
```

## 6. Create the Backend Data Directory

Create a directory on the host to persist application messages:

```bash
mkdir -p ~/Lab7-data/backend
```

## 7. Run the Notification Container

```bash
docker run -d \
  --name notification \
  --network backend-notification \
  --network-alias notification \
  --mount type=volume,source=notification-logs,target=/app/logs \
  lab7-notification:latest
```

## 8. Run the Backend Container

```bash
docker run -d \
  --name backend \
  --network backend-notification \
  --network-alias backend \
  -e NOTIFICATION_URL=http://notification:5001/notify \
  --mount type=volume,source=backend-logs,target=/app/logs \
  --mount type=bind,source="$HOME/Lab7-data/backend",target=/app/data \
  lab7-backend:latest
```

Connect the Backend to the Frontend network:

```bash
docker network connect --alias backend frontend-backend backend
```

## 9. Run the Frontend Container

Port `8080` was already in use by Jenkins, so host port `8081` was used instead.

```bash
docker run -d \
  --name frontend \
  --network frontend-backend \
  --network-alias frontend \
  -p 8081:3000 \
  -e BACKEND_URL=http://backend:5000 \
  lab7-frontend:latest
```

## 10. Verify the Containers

Check the running containers:

```bash
docker ps
```

The following containers were running successfully:

- `frontend`
- `backend`
- `notification`

## 11. Application Output

Open the application at [http://localhost:8081](http://localhost:8081).

The frontend displayed the following interface:

**Docker Microservices Lab**

### Create a Message

Send

### Messages

- hellosss
- sara

[Refresh Messages](http://localhost:8081/)

The application successfully displayed the messages `hellosss` and `sara`.

## 12. Persistence and Logs

The Backend uses a bind mount to persist message data on the host:

```bash
cat ~/Lab7-data/backend/messages.json
```

View the service logs:

```bash
docker logs backend
docker logs notification
```

The Docker volumes preserve logs independently of the containers, while the bind mount stores message data in the host directory.

