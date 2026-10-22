# Lab 6: Docker Environment Variables and Resource Management

**Name:** Sara Tarek  
**Repository:** [Training-labs](https://github.com/Sara-Tarek1/Training-labs)  
**Lab Directory:** `kubernetes/Lab6`

## 1. Objective

The objective of this lab is to practice Docker environment variables, manage container resources, monitor running containers, and understand different ways to configure environment variables in Docker.

## 2. Prerequisites

- Docker installed and running.
- Python 3.12.
- Basic knowledge of Docker commands.
- Git and GitHub.

## 3. Application Setup

Cloned the Python Flask application from the source repository:

```bash
git clone https://github.com/Ibrahim-Adel15/PythonApp.git app-source
```

The application uses two environment variables:

- `APP_NAME`: Specifies the application name.
- `APP_ENV`: Specifies the deployment environment.

The application listens on port `5000` and provides the following endpoints:

- `/` — Returns the application name, environment, and welcome message.
- `/health` — Returns the application health status.

## 4. Step 1: Create the Dockerfile

Created a Dockerfile using the `python:3.12-slim` base image, installed the required dependencies, copied the application files, and exposed port `5000`.

```dockerfile
FROM python:3.12-slim

WORKDIR /app

COPY app-source/requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY app-source/app.py .

EXPOSE 5000

CMD ["python", "app.py"]
```

## 5. Step 2: Build the First Docker Image

Built the Docker image named `ivolve1`.

```bash
docker build -t ivolve1 .
```

**Result:** The image was built successfully and used to run the first application container.

## 6. Step 3: Run Container 1 with Environment Variables and Resource Limits

Started a container named `container1`, configured environment variables, mapped port `5000`, and limited the container to one CPU and 256 MB of memory.

```bash
docker run -d \
  --name container1 \
  -p 5000:5000 \
  -e APP_NAME=iVolveApp1 \
  -e APP_ENV=stg \
  --cpus=1 \
  --memory=256m \
  ivolve1
```

### Verify the Application

```bash
curl http://localhost:5000
```

**Observed output:**

```json
{
  "application": "iVolveApp1",
  "environment": "stg",
  "message": "Welcome to the Docker Env & Resource Management Lab!"
}
```

### Verify Environment Variables

```bash
docker exec container1 printenv APP_NAME APP_ENV
```

Expected values:

```text
iVolveApp1
stg
```

### Monitor Resource Usage

```bash
docker stats container1
```

**Observed resource usage during the check:**

```text
NAME         CPU %   MEM USAGE / LIMIT
container1   0.01%   24.73MiB / 256MiB
```

*Resource usage is a snapshot and may change while the container is running.*

## 7. Step 4: Use an Environment File

Created a file named `env.txt` containing:

```text
APP_NAME=iVolveApp2
APP_ENV=prod
```

Started a second lab container using the environment file. Port `5001` was used on the host because port `5000` was already occupied by the first container.

```bash
docker run -d \
  --name lab6-container2 \
  -p 5001:5000 \
  --env-file env.txt \
  ivolve1
```

### Verify Environment Variables

```bash
docker exec lab6-container2 printenv APP_NAME APP_ENV
```

Expected values:

```text
iVolveApp2
prod
```

### Test the Application

```bash
curl http://localhost:5001
```

**Observed output:**

```json
{
  "application": "iVolveApp2",
  "environment": "prod",
  "message": "Welcome to the Docker Env & Resource Management Lab!"
}
```

## 8. Step 5: Define Environment Variables in a Dockerfile

Created a separate file named `Dockerfile.dev` to configure environment variables directly in the image.

```dockerfile
FROM python:3.12-slim

WORKDIR /app

COPY app-source/requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY app-source/app.py .

ENV APP_NAME=iVolveApp3
ENV APP_ENV=dev

EXPOSE 5000

CMD ["python", "app.py"]
```

Built the new image:

```bash
docker build -f Dockerfile.dev -t ivolve3 .
```

Started the third lab container:

```bash
docker run -d \
  --name lab6-container3 \
  -p 5002:5000 \
  ivolve3
```

### Verify Environment Variables

```bash
docker exec lab6-container3 printenv APP_NAME APP_ENV
```

Expected values:

```text
iVolveApp3
dev
```

### Test the Application

```bash
curl http://localhost:5002
```

Expected response:

```json
{
  "application": "iVolveApp3",
  "environment": "dev",
  "message": "Welcome to the Docker Env & Resource Management Lab!"
}
```

## 9. Results and Observations

| Container | Image | APP_NAME | APP_ENV | Host Port |
|---|---|---|---|---|
| `container1` | `ivolve1` | `iVolveApp1` | `stg` | `5000` |
| `lab6-container2` | `ivolve1` | `iVolveApp2` | `prod` | `5001` |
| `lab6-container3` | `ivolve3` | `iVolveApp3` | `dev` | `5002` |

The lab demonstrated three methods for configuring Docker environment variables:

1. Passing variables directly through the `docker run -e` option.
2. Loading variables from an environment file using `--env-file`.
3. Defining default variables in a Dockerfile using the `ENV` instruction.

The first container was configured with a CPU limit of one CPU and a memory limit of 256 MB. Docker resource usage was inspected using `docker stats`.

Different host ports allowed the three lab containers to run without conflicting over port `5000` inside the containers.

## 10. Conclusion

This lab demonstrated how to build Docker images, run containers with different environment configurations, apply CPU and memory limits, and monitor resource usage. It also showed how to test a containerized Flask application and verify the configured environment variables.

## 11. Technologies Used

- Docker
- Python 3.12
- Flask 3.1.2
- Linux / WSL Ubuntu
- Git and GitHub
# Lab 6: Docker Environment Variables and Resource Management

**Name:** Sara Tarek  
**Repository:** [Training-labs](https://github.com/Sara-Tarek1/Training-labs)  
**Lab Directory:** `kubernetes/Lab6`

## 1. Objective

The objective of this lab is to practice Docker environment variables, manage container resources, monitor running containers, and understand different ways to configure environment variables in Docker.

## 2. Prerequisites

- Docker installed and running.
- Python 3.12.
- Basic knowledge of Docker commands.
- Git and GitHub.

## 3. Application Setup

Cloned the Python Flask application from the source repository:

```bash
git clone https://github.com/Ibrahim-Adel15/PythonApp.git app-source
```

The application uses two environment variables:

- `APP_NAME`: Specifies the application name.
- `APP_ENV`: Specifies the deployment environment.

The application listens on port `5000` and provides the following endpoints:

- `/` — Returns the application name, environment, and welcome message.
- `/health` — Returns the application health status.

## 4. Step 1: Create the Dockerfile

Created a Dockerfile using the `python:3.12-slim` base image, installed the required dependencies, copied the application files, and exposed port `5000`.

```dockerfile
FROM python:3.12-slim

WORKDIR /app

COPY app-source/requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY app-source/app.py .

EXPOSE 5000

CMD ["python", "app.py"]
```

## 5. Step 2: Build the First Docker Image

Built the Docker image named `ivolve1`.

```bash
docker build -t ivolve1 .
```

**Result:** The image was built successfully and used to run the first application container.

## 6. Step 3: Run Container 1 with Environment Variables and Resource Limits

Started a container named `container1`, configured environment variables, mapped port `5000`, and limited the container to one CPU and 256 MB of memory.

```bash
docker run -d \
  --name container1 \
  -p 5000:5000 \
  -e APP_NAME=iVolveApp1 \
  -e APP_ENV=stg \
  --cpus=1 \
  --memory=256m \
  ivolve1
```

### Verify the Application

```bash
curl http://localhost:5000
```

**Observed output:**

```json
{
  "application": "iVolveApp1",
  "environment": "stg",
  "message": "Welcome to the Docker Env & Resource Management Lab!"
}
```

### Verify Environment Variables

```bash
docker exec container1 printenv APP_NAME APP_ENV
```

Expected values:

```text
iVolveApp1
stg
```

### Monitor Resource Usage

```bash
docker stats container1
```

**Observed resource usage during the check:**

```text
NAME         CPU %   MEM USAGE / LIMIT
container1   0.01%   24.73MiB / 256MiB
```

*Resource usage is a snapshot and may change while the container is running.*

## 7. Step 4: Use an Environment File

Created a file named `env.txt` containing:

```text
APP_NAME=iVolveApp2
APP_ENV=prod
```

Started a second lab container using the environment file. Port `5001` was used on the host because port `5000` was already occupied by the first container.

```bash
docker run -d \
  --name lab6-container2 \
  -p 5001:5000 \
  --env-file env.txt \
  ivolve1
```

### Verify Environment Variables

```bash
docker exec lab6-container2 printenv APP_NAME APP_ENV
```

Expected values:

```text
iVolveApp2
prod
```

### Test the Application

```bash
curl http://localhost:5001
```

**Observed output:**

```json
{
  "application": "iVolveApp2",
  "environment": "prod",
  "message": "Welcome to the Docker Env & Resource Management Lab!"
}
```

## 8. Step 5: Define Environment Variables in a Dockerfile

Created a separate file named `Dockerfile.dev` to configure environment variables directly in the image.

```dockerfile
FROM python:3.12-slim

WORKDIR /app

COPY app-source/requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY app-source/app.py .

ENV APP_NAME=iVolveApp3
ENV APP_ENV=dev

EXPOSE 5000

CMD ["python", "app.py"]
```

Built the new image:

```bash
docker build -f Dockerfile.dev -t ivolve3 .
```

Started the third lab container:

```bash
docker run -d \
  --name lab6-container3 \
  -p 5002:5000 \
  ivolve3
```

### Verify Environment Variables

```bash
docker exec lab6-container3 printenv APP_NAME APP_ENV
```

Expected values:

```text
iVolveApp3
dev
```

### Test the Application

```bash
curl http://localhost:5002
```

Expected response:

```json
{
  "application": "iVolveApp3",
  "environment": "dev",
  "message": "Welcome to the Docker Env & Resource Management Lab!"
}
```

## 9. Results and Observations

| Container | Image | APP_NAME | APP_ENV | Host Port |
|---|---|---|---|---|
| `container1` | `ivolve1` | `iVolveApp1` | `stg` | `5000` |
| `lab6-container2` | `ivolve1` | `iVolveApp2` | `prod` | `5001` |
| `lab6-container3` | `ivolve3` | `iVolveApp3` | `dev` | `5002` |

The lab demonstrated three methods for configuring Docker environment variables:

1. Passing variables directly through the `docker run -e` option.
2. Loading variables from an environment file using `--env-file`.
3. Defining default variables in a Dockerfile using the `ENV` instruction.

The first container was configured with a CPU limit of one CPU and a memory limit of 256 MB. Docker resource usage was inspected using `docker stats`.

Different host ports allowed the three lab containers to run without conflicting over port `5000` inside the containers.

## 10. Conclusion

This lab demonstrated how to build Docker images, run containers with different environment configurations, apply CPU and memory limits, and monitor resource usage. It also showed how to test a containerized Flask application and verify the configured environment variables.

## 11. Technologies Used

- Docker
- Python 3.12
- Flask 3.1.2
- Linux / WSL Ubuntu
- Git and GitHub

