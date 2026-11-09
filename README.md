# Assignment 9 — Cloud & Containers

## Application
Concurrent Reservation Service: a Java 21 / Spring Boot 3.5.6 HTTP API built with Maven.

Operations demonstrated:
- `GET /api/resources`: retrieve available CAMERA, LAPTOP, and PROJECTOR resources.
- `POST /api/reservations`: create a reservation and modify availability.

## Files
- `Dockerfile`: multistage Java 21 image build and runtime startup
- `.dockerignore`: excludes local build output and unnecessary files
- `pom.xml` and `src/`: application source code, tests, and configuration
- `.gitignore`: prevents committing generated files and credentials

## Prerequisites
Docker Desktop with Linux containers (or another Linux Docker Engine) and an internet connection for downloading images and Maven dependencies. A GitHub repository and a Render account are needed to reproduce the cloud deployment.

## Build and run locally (Windows PowerShell)
From the directory containing `Dockerfile`:

```powershell
docker build -t a9-reservations:1 .
docker run -d --name a9-local -p 127.0.0.1:18090:8080 -e RESERVATION_STORAGE=file a9-reservations:1
docker ps
Invoke-RestMethod 'http://127.0.0.1:18090/api/resources'
```

Create a test reservation:

```powershell
$body = @{ resourceId = 'LAPTOP'; customerName = 'Local Test'; quantity = 1 } | ConvertTo-Json
Invoke-RestMethod -Uri 'http://127.0.0.1:18090/api/reservations' -Method Post -ContentType 'application/json' -Body $body
```

To stop and remove the container:

```powershell
docker rm -f a9-local
```

## Container configuration investigations
**Port publishing:** Run the image without `-p`. The container has port 8080 internally, but Windows cannot access it at localhost:18090 unless that port is published. Recreate it with `-p 127.0.0.1:18090:8080` to restore access.

**Persistent storage:** Container-local reservation data does not survive `docker rm -f` followed by container recreation. To retain local data, create a Docker named volume:

```powershell
docker volume create a9-reservations-data
docker run -d --name a9-volume -p 127.0.0.1:18090:8080 --mount type=volume,source=a9-reservations-data,target=/app/data -e RESERVATION_STORAGE=file a9-reservations:1
```

After removing the container, recreate it using the same `--mount` option to preserve reservations. Run only one container at a time on host port 18090.

## Cloud deployment to Render
1. Push the source files, `pom.xml`, `Dockerfile`, and `.dockerignore` to a GitHub repository. Do not commit credentials, `.env`, generated `target/`, or local runtime data.
2. In the Render dashboard, create **New → Web Service** and connect the GitHub repository.
3. Select **Docker** as the language/runtime, `main` as the branch, and leave the Root Directory blank because the Dockerfile is at the repository root.
4. Choose the **Free** compute plan and set the environment variable `RESERVATION_STORAGE=file`.
5. Deploy. The Dockerfile starts the Java HTTP server on `${PORT:-8080}` and binds it to `0.0.0.0` so Render can route traffic to it.
6. Verify the public API with the actual Render URL (replacing the example base URL if a different service name was assigned):

```powershell
$cloud = 'https://assignment9-cloudcontainers.onrender.com'
Invoke-RestMethod "$cloud/api/resources"
$cloudBody = @{ resourceId = 'PROJECTOR'; customerName = 'Cloud Test'; quantity = 1 } | ConvertTo-Json
Invoke-RestMethod -Uri "$cloud/api/reservations" -Method Post -ContentType 'application/json' -Body $cloudBody
```

## Cloud service model and limitations
Render Web Service is a managed Platform as a Service (PaaS): Render operates the underlying hosting infrastructure and public routing; the developer manages application code, Docker configuration, dependencies, and data behavior. The Free service can spin down after inactivity and has ephemeral local storage. File-backed reservations are not reliably retained across restarts or redeployments. This project is a classroom demonstration, not a production reservation system.

## Evidence
The consolidated Word report for Deliverables 2–5 is submitted separately to Canvas.
