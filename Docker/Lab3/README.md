# Containerization with Docker
## Lab 3: Creating a Docker Image for a Java Application

### Objective
Build and run a Java Calculator application using Docker.
Part 1 builds the application inside Docker using Maven and Java 17.

### 1. Project Structure
```text
lab3/
├── Dockerfile
├── README.md
├── pom.xml
└── src/
    ├── main/java/com/example/calculator/
    │   ├── Main.java
    │   └── Calculator.java
    └── test/java/com/example/calculator/
        └── CalculatorTest.java
```

### 2. Dockerfile
```dockerfile
FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

RUN groupadd -r appuser && \
    useradd -r -m -g appuser appuser && \
    chown appuser:appuser /app

COPY --chown=appuser:appuser pom.xml .
COPY --chown=appuser:appuser src ./src

USER appuser

RUN mvn package

EXPOSE 8080

CMD ["java", "-jar", "target/calculator.jar"]
```

### 3. Explanation
- `FROM`: Uses a Maven image with Java 17.
- `WORKDIR`: Sets `/app` as the working directory.
- `RUN`: Creates a non-root user and group and sets directory ownership.
- `COPY`: Copies the Maven configuration and source code into the image.
- `USER`: Runs subsequent build and application commands as `appuser`.
- `RUN mvn package`: Builds the application and runs the configured tests.
- `EXPOSE 8080`: Documents the port specified by the lab.
- `CMD`: Runs the generated JAR when the container starts.

### 4. Build the Docker Image
```bash
docker build -t app1 .
```

The build completed successfully. Maven packaged the application inside Docker.

### 5. Check the Image Size
```bash
docker images app1
```

Recorded output:
- Image: `app1:latest`
- Disk usage: 814 MB
- Content size: 251 MB

### 6. Run the Container
```bash
docker run -it --name container1 app1
```

Test input:
- First number: 6
- Second number: 5

Expected and observed output:
```text
a = 6.0, b = 5.0
Sum: 11.0
Subtract: 1.0
Multiply: 30.0
Divide: 1.2
```

The calculator ran successfully inside the container.

### 7. Cleanup
After testing, remove the stopped container and image:

```bash
docker rm container1
docker rmi app1
```

### Important Notes
- The application is a console-based calculator, not a web server.
- `EXPOSE 8080` documents the requested port but does not create an HTTP service.
- A Maven warning about writing to `/root/.m2` appeared at container startup. The calculator still ran successfully.
- The image size can vary depending on the image version and Docker environment.

### Part 1 Status
- [x] Create a Dockerfile using Maven and Java 17.
- [x] Create a working directory and non-root user.
- [x] Copy source code and build with Maven.
- [x] Document port 8080.
- [x] Build image `app1` and record its size.
- [x] Run `container1` and verify the calculator.
- [ ] Remove the container and image.

sara@DESKTOP-VDHI02P:~/lab3$ docker run -it --name container1 app1
mkdir: cannot create directory ‘/root’: Permission denied
Can not write to /root/.m2/copy_reference_file.log. Wrong volume permissions? Carrying on ...
Enter first number: 6
Enter second number: 5
a = 6.0, b = 5.0
Sum: 11.0
Subtract: 1.0
Multiply: 30.0
Divide: 1.2
