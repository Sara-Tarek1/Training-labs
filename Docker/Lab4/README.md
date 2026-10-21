# Docker Lab 4: Image Optimization and Export/Load

## 1. Lab Overview

**Project:** ImageOptimization-  
**Application:** Spring Boot  
**Java Version:** 17  
**Application Port:** 8080  
**Working Directory:** `~/lab4/ImageOptimization-`

### Objectives
- Build and compare Docker images.
- Optimize the image using a multi-stage Dockerfile.
- Run the application using a non-root user.
- Test the application in a browser.
- Export and reload a Docker image using a TAR archive.

## 2. Original Dockerfile

The original `Dockerfile` used Maven and Java 17 to build and run the application.

```dockerfile
FROM maven:3.9.15-eclipse-temurin-17-alpine
WORKDIR /app
COPY . .
RUN mvn package
EXPOSE 8080
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
```

### Explanation
- `FROM`: selects a base image containing Maven and Java 17.
- `WORKDIR`: sets `/app` as the working directory.
- `COPY . .`: copies the project files into the image.
- `RUN mvn package`: builds the application and generates the JAR file.
- `EXPOSE 8080`: documents the application's listening port.
- `CMD`: starts the Spring Boot application.

## 3. Optimized Dockerfile

A second file, `Dockerfile.optimized`, was created using a multi-stage build.

```dockerfile
FROM maven:3.9.15-eclipse-temurin-17-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### Why is this better?

- **Multi-stage build:** separates the build environment from the runtime environment.
- **Builder stage:** uses Maven to package the application.
- **Runtime stage:** uses a lightweight Java Runtime Environment (JRE) image.
- `COPY --from=builder`: copies the generated JAR without copying the entire build environment.
- `USER appuser`: configures the application to run as a non-root user.
- `-DskipTests`: skips tests during the Maven package command.

This approach reduces the size of the final image and avoids running the application as root.

## 4. Build the Optimized Image

Command used:

```bash
docker build -f Dockerfile.optimized -t image2 .
```

- `-f Dockerfile.optimized`: selects the optimized Dockerfile.
- `-t image2`: names the resulting image `image2`.
- `.`: uses the current directory as the build context.

## 5. Image Size Comparison

| Image | Recorded Size |
|---|---:|
| Original image: `image1` | 640.06 MB |
| Optimized image: `image2` | 288.17 MB |
| Space saved | 351.89 MB |
| Approximate reduction | 55% |

The optimized image is approximately 55% smaller than the original image.

**Calculation:**

```text
640.06 MB - 288.17 MB = 351.89 MB

(351.89 / 640.06) × 100 ≈ 55%
```

*Note: The original `image1` size is the measurement recorded earlier in the lab. The image was subsequently removed.*

## 6. Run and Test the Application

The host port `8080` was already in use by the existing Jenkins container. Therefore, host port `8081` was mapped to container port `8080`.

Command used:

```bash
docker run -d --name container2-lab4 -p 8081:8080 image2
```

The application was opened in the browser at:

```text
http://localhost:8081
```

### Browser Output

```text
Hello from Dockerized Spring Boot!
```

This confirmed that the optimized Spring Boot application was responding successfully.

### Non-root User Verification

Command to check the configured runtime user:

```bash
docker exec container2-lab4 whoami
```

Expected output:

```text
appuser
```

This output should be recorded as confirmed only if the command was executed and returned `appuser`.

## 7. Stop and Remove the Test Container

Commands:

```bash
docker stop container2-lab4
docker rm container2-lab4
```

- `docker stop`: stops the test container.
- `docker rm`: removes the stopped container.

These commands target the Lab 4 container, not the separate Jenkins container.

## 8. Export the Image

Command used:

```bash
docker save -o lab-image.tar image2
```

This exported `image2` into a TAR archive named `lab-image.tar`.

The recorded archive size was approximately **85 MB**.

To check the archive:

```bash
ls -lh lab-image.tar
```

The archive size can be smaller than the image's displayed size because the saved layers are stored in an archived form.

## 9. Remove and Reload the Image

Remove the local image:

```bash
docker rmi image2
```

Reload it from the saved archive:

```bash
docker load -i lab-image.tar
```

- `docker rmi`: removes the local image.
- `docker load`: imports the image from the archive created by `docker save`.

## 10. Verify the Restored Image

Command:

```bash
docker images image2
```

The image should appear in the local image list after a successful load.

For additional details:

```bash
docker image inspect image2
```

## 11. Final Results

- Created an optimized multi-stage Dockerfile.
- Used a JRE-based runtime image.
- Configured a dedicated non-root user.
- Tested the application through `http://localhost:8081`.
- Observed the response `Hello from Dockerized Spring Boot!`.
- Recorded an image size reduction of approximately 55%.
- Exported the image to `lab-image.tar`.
- Removed and reloaded the image using Docker command

