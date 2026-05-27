# Book Library API — Complete CI/CD Pipeline Demo

A production-grade Spring Boot REST API with a complete CI/CD pipeline demonstrating **JUnit + Mockito testing**, **SonarCloud code quality**, **Docker containerization**, and **automated image publishing** to Docker Hub (with AWS ECR variant included).

Built for a **90-minute training session** for dev/AI students.

[![CI/CD Pipeline](https://github.com/YOUR_USERNAME/book-library-cicd/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/YOUR_USERNAME/book-library-cicd/actions)

---

## What This Demonstrates

Every concept from your requirements list:

| Requirement | Where It's Implemented |
|-------------|-------------------------|
| **Automated Testing (JUnit + Mockito)** | `src/test/java/.../BookServiceTest.java` (15 tests with mocks) |
| **Build stops on test failure** | `needs: test` chain in workflow |
| **Code Quality (SonarCloud)** | `sonarcloud` job — checks code smells, vulnerabilities, coverage |
| **Automated Docker Push** | `docker` job pushes to Docker Hub on every main commit |
| **AWS ECR support** | Bonus workflow in `.github/workflows/ci-cd-ecr.yml.example` |

---

## The Application

A REST API for managing a library of books:

| Method | Endpoint | Purpose |
|--------|----------|---------|
| `POST` | `/api/books` | Add a new book |
| `GET` | `/api/books` | List all books |
| `GET` | `/api/books?author=X` | Filter by author |
| `GET` | `/api/books/{id}` | Get one book |
| `PATCH` | `/api/books/{id}/rating` | Update rating |
| `DELETE` | `/api/books/{id}` | Delete a book |
| `GET` | `/api/books/stats/average-rating` | Average rating across all books |
| `GET` | `/actuator/health` | Health check |

---

## Pipeline Architecture

```
   git push to main
        │
        ▼
   ┌─────────────────┐
   │  test (JUnit +  │  ← Runs all 23 tests with Mockito
   │     Mockito)    │
   └────────┬────────┘
            │
       ┌────┴────┐
       ▼         ▼
   ┌────────┐ ┌────────┐
   │ Sonar  │ │Package │  ← Quality scan + JAR build (parallel)
   │ Cloud  │ │  JAR   │
   └────┬───┘ └────┬───┘
        └────┬────┘
             ▼
       ┌──────────┐
       │  Docker  │  ← Build image + push to Docker Hub
       │  Build & │
       │   Push   │
       └────┬─────┘
            │
            ▼
       ┌──────────┐
       │ Summary  │
       └──────────┘
```

**Key behavior**: If `test` fails, **nothing downstream runs**. The Docker image never gets built, never gets published. Your production stays safe.

---

## Project Structure

```
book-library-cicd/
├── src/
│   ├── main/java/com/example/library/
│   │   ├── BookLibraryApplication.java    # Spring Boot entry point
│   │   ├── controller/BookController.java # REST endpoints
│   │   ├── service/BookService.java       # Business logic (tested with mocks)
│   │   ├── repository/                    # Data layer
│   │   ├── model/Book.java                # Entity
│   │   └── exception/                     # Error handling
│   ├── main/resources/
│   │   └── application.properties
│   └── test/java/com/example/library/
│       ├── service/BookServiceTest.java       # 15 Mockito tests
│       └── controller/BookControllerTest.java # 8 MockMvc tests
├── .github/workflows/
│   ├── ci-cd.yml                          # Main pipeline (Docker Hub)
│   └── ci-cd-ecr.yml.example              # Bonus: AWS ECR variant
├── Dockerfile                             # Multi-stage build
├── pom.xml                                # Maven config + plugins
├── docs/
│   ├── SESSION-PLAN.md                    # 90-min session breakdown
│   └── SETUP.md                           # Full setup guide
└── README.md
```

---

## Quick Setup (10 minutes)

### Prerequisites
- GitHub account
- Docker Hub account (free at hub.docker.com)
- SonarCloud account (free at sonarcloud.io — sign in with GitHub)

### Step 1: Create the GitHub Repository

1. GitHub → **New repository** → name it `book-library-cicd`
2. **Public** (required for free SonarCloud)
3. **Don't** initialize with README

### Step 2: Push the Code

```bash
cd book-library-cicd
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/book-library-cicd.git
git push -u origin main
```

### Step 3: Set Up SonarCloud

1. Go to https://sonarcloud.io → **Log in with GitHub**
2. Click **+** (top right) → **Analyze new project**
3. Select your `book-library-cicd` repository → **Set up**
4. Choose **With GitHub Actions** as the analysis method
5. SonarCloud will show you two values — **copy them**:
   - `SONAR_TOKEN` (a long string)
   - Your **Organization** key (looks like `yourusername`)
   - Your **Project key** (looks like `yourusername_book-library-cicd`)

### Step 4: Set Up Docker Hub Token

1. Go to https://hub.docker.com → log in
2. Top right avatar → **Account Settings** → **Personal access tokens**
3. **Generate new token**:
   - Name: `github-actions`
   - Permissions: **Read, Write, Delete**
4. **Copy the token** (starts with `dckr_pat_...`)

### Step 5: Add GitHub Secrets

Repo → **Settings → Secrets and variables → Actions → New repository secret**

Add these **five secrets**:

| Secret Name | Value |
|-------------|-------|
| `SONAR_TOKEN` | From SonarCloud setup |
| `SONAR_ORGANIZATION` | Your SonarCloud organization key |
| `SONAR_PROJECT_KEY` | Your SonarCloud project key |
| `DOCKERHUB_USERNAME` | Your Docker Hub username |
| `DOCKERHUB_TOKEN` | The token from Step 4 |

### Step 6: Trigger the Pipeline

Either push any change or trigger manually:
- **Actions** tab → **CI/CD Pipeline** → **Run workflow**

Watch all 5 jobs run. The Docker image will appear at:
`https://hub.docker.com/r/YOUR_USERNAME/book-library`

---

## Running Locally (Optional)

### With Maven
```bash
mvn spring-boot:run
```

### With Docker
```bash
docker build -t book-library:local .
docker run -p 8080:8080 book-library:local
```

### Test the API
```bash
# Create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert Martin","rating":5}'

# List books
curl http://localhost:8080/api/books

# Health check
curl http://localhost:8080/actuator/health
```

---

## Demo Scenarios for the Session

### Scenario 1: Happy Path (5 mins)
Push a small change. Show the pipeline:
1. Tests pass
2. SonarCloud scan completes (open the dashboard live)
3. JAR is packaged
4. Docker image is built and pushed
5. Open Docker Hub → show the new image with a fresh timestamp

### Scenario 2: Test Failure Blocks Deploy (5 mins)
Break a test in `BookServiceTest.java`:
```java
// Change this:
assertEquals(4.0, avg, 0.01);
// To this:
assertEquals(99.0, avg, 0.01);  // Wrong!
```
Push. Show:
- `test` job fails (red X)
- All downstream jobs are **skipped**
- Docker Hub does NOT get a new image
- Your production environment is **safe**

### Scenario 3: Code Quality Issue (5 mins)
Add bad code on purpose in `BookService.java`:
```java
public void badMethod() {
    String unused = "this is never used";
    if (true) {  // Code smell: constant condition
        System.out.println("debug");  // Code smell: System.out
    }
}
```
Push. Show the SonarCloud dashboard light up with:
- Code smells
- Coverage drop (no tests for `badMethod`)
- Quality gate status

### Scenario 4: Pull the Image and Run (3 mins)
On any machine with Docker:
```bash
docker pull YOUR_USERNAME/book-library:latest
docker run -p 8080:8080 YOUR_USERNAME/book-library:latest
```
Open `http://localhost:8080/api/books` — the app deployed from CI works anywhere.

---

## What Makes This Production-Grade

- **Multi-stage Docker build** → small final image (only JRE, no Maven)
- **Non-root user** in container → security best practice
- **Health checks** built into Dockerfile
- **JaCoCo coverage** integrated with SonarCloud
- **Layer caching** in Docker builds (`cache-from: type=gha`)
- **Maven dependency caching** in CI (saves 1-2 mins per run)
- **Proper job dependencies** with `needs:`
- **Concurrency control** prevents duplicate runs
- **Image tagged with commit SHA** for traceability
- **Validation** with `@Valid` and Jakarta Bean Validation
- **Global exception handling** with `@RestControllerAdvice`

---

## Resume Bullet Points (For Your Students)

After completing this hands-on project, students can say:

- *"Built a Spring Boot REST API with 23 unit tests using JUnit 5 and Mockito, achieving 90%+ code coverage"*
- *"Designed a 5-stage CI/CD pipeline in GitHub Actions with parallel quality gates and conditional deployment"*
- *"Integrated SonarCloud for automated code quality analysis, blocking deploys on critical issues"*
- *"Containerized the application with a multi-stage Dockerfile and automated publishing to Docker Hub"*

---

## License

MIT
