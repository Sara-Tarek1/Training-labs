# Trivy Docker Image Security Lab

## Overview
This lab demonstrates how to build a Docker image for a Node.js application, scan it for vulnerabilities using Trivy, and generate an HTML security report.

## Technologies Used
- Docker
- Node.js
- Express.js
- Trivy
- Alpine Linux

## Application Endpoints
- `/` — Returns a welcome message.
- `/health` — Returns the application health status as JSON.

## Build the Docker Image

```bash
docker build --no-cache -t ivolve-app:v2 .
```

## Run the Container

```bash
docker run -d --name ivolve-test -p 3000:3000 ivolve-app:v2
```
Legend:
- '-': Not scanned
- '0': Clean (no security findings detected)
## Test the Application

Test the main endpoint:

```bash
curl http://localhost:3000
```

Expected response:

```text
Welcome to the Trivy Security Lab!
```

Test the health endpoint:

```bash
curl http://localhost:3000/health
```

Expected response:

```json
{"status":"UP"}
```

## Scan the Docker Image

Scan for HIGH and CRITICAL vulnerabilities:

```bash
trivy image --scanners vuln --severity HIGH,CRITICAL ivolve-app:v2
```

The scan completed with no HIGH or CRITICAL vulnerabilities detected.

## Generate the HTML Report

```bash
trivy image --scanners vuln --severity HIGH,CRITICAL --format template --template "@html.tpl" --output ivolve-report-v2.html ivolve-app:v2
```

Open the generated `ivolve-report-v2.html` file in a browser to review the scan results.

## Security Improvements
- Used an Alpine-based Node.js image.
- Upgraded Alpine packages during the image build.
- Installed production dependencies only.
- Removed npm and npx from the final image after dependency installation.
- Ran the application as the non-root `node` user.
- Scanned the final image using Trivy.

## Project Files
- `Dockerfile` — Defines the Docker image build steps.
- `app.js` — Node.js Express application.
- `package.json` and `package-lock.json` — Application dependencies.
- `html.tpl` — Trivy HTML report template.
- `ivolve-report-v2.html` — Generated vulnerability scan report.

## Result
The application runs successfully, and the final image scan reported no detected HIGH or CRITICAL vulnerabilities. This result applies to the selected severity levels and scan performed; it does not mean that every possible vulnerability is absent.
