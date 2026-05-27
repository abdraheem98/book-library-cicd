# 90-Minute Session Plan — CI/CD with Spring Boot

Designed for **dev/AI students** as a **live demo** session.

## Goal
By the end, students understand: what CI/CD is, why it matters, how to test with mocks, why code quality matters, and how containerized apps get shipped automatically.

---

## Time Breakdown

### 0:00 – 0:10 — Setup & Hook (10 mins)

**Hook**: Show a finished pipeline running. Click into it. Show:
- 23 tests passing in 8 seconds
- SonarCloud dashboard with grade A
- Docker Hub showing the published image
- Tell them: *"By the end of this session, you'll understand every piece of this pipeline."*

**Setup recap**: Briefly show the screen — your GitHub repo, your SonarCloud account, your Docker Hub. Make sure everyone can see your screen clearly.

---

### 0:10 – 0:20 — What is CI/CD? (10 mins)

Cover these concepts visually (use whiteboard or slides):

1. **CI (Continuous Integration)**
   - Multiple developers committing code
   - Need automated tests to catch breaks early
   - "Fail fast" principle

2. **CD (Continuous Delivery/Deployment)**
   - Every passing commit becomes a deployable artifact
   - Containerization makes it portable
   - The same image runs in dev, staging, prod

3. **The 4 stages of any CI/CD pipeline**:
   - **Test** — Does it work?
   - **Quality** — Is it clean?
   - **Build** — Package it
   - **Publish** — Make it available

**Key message**: CI/CD is what separates hobby projects from professional software.

---

### 0:20 – 0:35 — The Application & Tests (15 mins)

Open the code. Walk through:

**The app (5 mins)**:
- `BookController` → REST endpoints
- `BookService` → business logic (the **important** part)
- `BookRepository` → data access (interface + in-memory implementation)
- Why this layered architecture matters

**The tests (10 mins)** — THIS IS THE KEY PART:

Open `BookServiceTest.java`. Highlight:

```java
@Mock
private BookRepository bookRepository;

@InjectMocks
private BookService bookService;
```

Explain: *"Mockito creates a fake repository. We're testing the service in isolation — no database needed."*

Show 3 specific tests:

1. **Happy path test** (`shouldCreateBook`):
   ```java
   when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
   Book result = bookService.createBook(newBook);
   verify(bookRepository, times(1)).save(newBook);
   ```
   Explain: `when().thenReturn()` defines what the mock does. `verify()` confirms it was called.

2. **Exception test** (`shouldThrowWhenNotFound`):
   ```java
   when(bookRepository.findById(999L)).thenReturn(Optional.empty());
   assertThrows(BookNotFoundException.class, () -> bookService.getBookById(999L));
   ```
   Explain: Easy to test error paths with mocks — no need to simulate real DB failures.

3. **Behavior verification** (`shouldThrowWhenDeletingNonExistent`):
   ```java
   verify(bookRepository, never()).deleteById(anyLong());
   ```
   Explain: We can verify a method was **never** called — proves the safety check works.

**Run tests live**: `mvn test` → all green in seconds.

---

### 0:35 – 0:50 — Build the Pipeline Live (15 mins)

Open `.github/workflows/ci-cd.yml`. Walk through each job:

**Job 1: test**
- `actions/checkout@v4` — gets the code
- `actions/setup-java@v4` — installs JDK
- `mvn test` — runs all 23 tests
- Upload artifacts

**Job 2: sonarcloud** (parallel branch)
- `needs: test` — only runs if tests pass
- Sends coverage data to SonarCloud
- Quality gate check

**Job 3: package** (parallel branch)
- Builds the JAR
- Uploads as downloadable artifact

**Job 4: docker**
- `needs: [test, sonarcloud, package]` — waits for ALL three
- Only runs on push to main (not PRs — security!)
- Builds multi-stage image
- Pushes to Docker Hub

**Key teaching moments**:
- Show `if: github.event_name == 'push' && github.ref == 'refs/heads/main'` — explain conditional execution
- Show the `needs:` chain — explain dependency graph
- Show `cache-from: type=gha` — explain why caching matters

---

### 0:50 – 1:10 — Run It and Show the Results (20 mins)

**Push a change** live:
```bash
# Edit the README
echo "" >> README.md
git add . && git commit -m "Demo trigger"
git push
```

Switch to GitHub Actions tab. Show in real time:
1. Pipeline starts immediately
2. Test job runs (~2 mins) — show the green checkmarks
3. SonarCloud and Package run in parallel
4. Docker job builds the image (~3 mins first run, faster with cache)

**While it runs, show**:
- **SonarCloud dashboard** (sonarcloud.io) — code smells, bugs, coverage, hotspots
- **Docker Hub** (hub.docker.com/r/your-username/book-library) — image tags
- **GitHub Actions summary page** — the markdown summary you generated

**The "wow" moment**:
```bash
# On a fresh terminal
docker pull YOUR_USERNAME/book-library:latest
docker run -p 8080:8080 YOUR_USERNAME/book-library:latest
```
Open `http://localhost:8080/api/books` in browser. **The app just deployed from code to a runnable container automatically.**

---

### 1:10 – 1:25 — Failure Scenarios (15 mins)

This is the most important part. Show why CI/CD matters.

**Scenario A: Broken Test (5 mins)**

Edit `BookServiceTest.java`:
```java
// Find this:
assertEquals(4.0, avg, 0.01);
// Change to:
assertEquals(99.0, avg, 0.01);
```

Push. Watch:
- Test job goes red
- ALL downstream jobs are skipped (gray)
- Docker Hub gets NO new image
- The bad code never reaches production

Fix it, push again, show green.

**Scenario B: Code Quality Issue (5 mins)**

Add bad code to `BookService.java`:
```java
public void unusedMethod() {
    String x = "never used";
    System.out.println("debug print");
}
```

Push. Open SonarCloud after the scan:
- New code smells appear
- The dashboard shows exactly which lines
- Coverage percentage drops

Remove the bad code, push, watch it return to green.

**Scenario C: Manual Trigger (3 mins)**

Show **workflow_dispatch**:
- Actions tab → Run workflow → Run
- Useful for emergency rebuilds, scheduled tasks, on-demand operations

**Scenario D: The Cost of NOT Having This (2 mins)**

Tell a brief story: *"Without CI/CD, a developer pushes code Friday evening. Monday morning, the bug is in production. Without tests, they don't know what's broken. Without mocks, tests take 10 minutes each because they hit the real database. Without containers, the app runs on the developer's machine but not on the server. This pipeline solves all of those problems."*

---

### 1:25 – 1:30 — Wrap-Up & Q&A (5 mins)

**What they learned**:
- JUnit 5 + Mockito for fast, isolated unit tests
- SonarCloud for objective code quality measurement
- GitHub Actions YAML for automation
- Docker for portable deployment
- The full CI/CD chain from commit to publish

**Where to go next**:
- Add deployment to AWS EC2, EKS, or App Runner
- Add integration tests with TestContainers
- Add security scanning (Trivy, Snyk)
- Add automatic dependency updates (Dependabot)

**Resources they can take**:
- The full GitHub repo (public, free to fork)
- This README + SETUP.md + SESSION-PLAN.md
- Spring Boot Reference: spring.io
- Mockito docs: site.mockito.org
- SonarCloud docs: docs.sonarcloud.io

---

## Tips for a Smooth Live Demo

1. **Pre-run the pipeline once** before the session to warm up caches — second runs are 3x faster
2. **Have the SonarCloud dashboard pre-loaded** in a browser tab
3. **Have Docker Hub pre-loaded** in a browser tab
4. **Use a large terminal font** — at least 16pt for projection
5. **Use GitHub's web editor** for quick file changes during demos — faster than switching to local IDE
6. **Keep `git status` and `git log --oneline` handy** to show what changed
7. **If something breaks**, that's a TEACHING moment, not a failure — debug live, they'll learn more

---

## Common Questions Students Will Ask

**Q: Can this run on AWS?**
A: Yes — see the bonus `ci-cd-ecr.yml.example` for the AWS ECR variant. Replace Docker Hub with Amazon ECR; the rest stays the same.

**Q: What about Jenkins?**
A: Same concepts, different syntax. Jenkins uses `Jenkinsfile` (Groovy). GitHub Actions uses YAML. Modern projects favor GitHub Actions for zero setup.

**Q: How much does this cost?**
A: For public repos: $0. GitHub Actions, SonarCloud, and Docker Hub all have generous free tiers.

**Q: What if I have multiple environments (dev/staging/prod)?**
A: Use GitHub Environments with protection rules. Each environment can require manual approval before deploy.

**Q: How do we secure secrets?**
A: GitHub Secrets are encrypted at rest, decrypted only at runtime, never appear in logs. For production, use OIDC federation with cloud providers — no long-lived credentials.

**Q: What's the difference between unit tests and integration tests?**
A: Unit tests use mocks (like our BookServiceTest). Integration tests use real components (like BookControllerTest with MockMvc). Both have their place.
