# Complete Setup Guide

This guide walks through every step, copy-paste ready. Time required: **15 minutes total**.

---

## Part 1 — Create GitHub Repository (2 mins)

1. Go to https://github.com/new
2. Repository name: `book-library-cicd`
3. Visibility: **Public** (required for free SonarCloud + Docker Hub)
4. **Do NOT** add README, .gitignore, or license
5. Click **Create repository**

---

## Part 2 — Push the Code (2 mins)

On your local machine:

```bash
cd book-library-cicd
git init
git branch -M main
git add .
git commit -m "Initial commit: Book Library CI/CD demo"
git remote add origin https://github.com/YOUR_USERNAME/book-library-cicd.git
git push -u origin main
```

Refresh GitHub — code should appear.

**Important**: The first push triggers the pipeline, but it will fail because secrets aren't configured yet. That's expected.

---

## Part 3 — SonarCloud Setup (5 mins)

### Step 3.1: Create Account

1. Go to https://sonarcloud.io
2. Click **Log in** → **With GitHub** → Authorize

### Step 3.2: Import Your Repository

1. Click the **+** in top right → **Analyze new project**
2. If asked, install SonarCloud GitHub app on your account (free)
3. Find and select `book-library-cicd` → click **Set up**
4. On the next screen, choose **GitHub Actions** as the analysis method

### Step 3.3: Get Your SonarCloud Values

SonarCloud will display:

- **`SONAR_TOKEN`** — looks like `sqp_a1b2c3d4...` — **copy this**
- Project key — looks like `your-github-username_book-library-cicd` — **copy this**
- Organization key — looks like `your-github-username` — **copy this**

Keep these safe for Part 5.

### Step 3.4: Disable Automatic Analysis

1. In SonarCloud, go to your project → **Administration → Analysis Method**
2. **Turn OFF** "Automatic Analysis" (we use GitHub Actions instead)

---

## Part 4 — Docker Hub Setup (3 mins)

### Step 4.1: Create Account

1. Go to https://hub.docker.com
2. Sign up (free) → verify email

### Step 4.2: Create Access Token

1. Top right avatar → **Account Settings**
2. Left sidebar → **Personal access tokens**
3. **Generate new token**:
   - Description: `github-actions-book-library`
   - Permissions: **Read, Write, Delete**
4. Click **Generate** → **copy the token** (starts with `dckr_pat_...`)
5. **Save it now** — you can't see it again

Your Docker Hub username will be needed too — it's whatever you see at the top right after login.

---

## Part 5 — Add GitHub Secrets (3 mins)

On GitHub:

1. Go to your repo
2. **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** for each of these:

| Secret Name | Value | Where to Find |
|-------------|-------|---------------|
| `SONAR_TOKEN` | `sqp_...` | From Part 3.3 |
| `SONAR_ORGANIZATION` | Your org key | From Part 3.3 |
| `SONAR_PROJECT_KEY` | Your project key | From Part 3.3 |
| `DOCKERHUB_USERNAME` | Your Docker Hub username | hub.docker.com profile |
| `DOCKERHUB_TOKEN` | `dckr_pat_...` | From Part 4.2 |

**Total: 5 secrets.**

---

## Part 6 — Trigger the Pipeline

### Option A: Push a small change
```bash
echo "" >> README.md
git add . && git commit -m "Trigger first pipeline"
git push
```

### Option B: Manual trigger
1. GitHub repo → **Actions** tab
2. Left sidebar → **CI/CD Pipeline**
3. Right side → **Run workflow** → **Run workflow**

### Watch It Run

1. **Actions** tab → click the running workflow
2. Five jobs will run:
   - `test` (~1-2 mins first time)
   - `sonarcloud` (~2-3 mins)
   - `package` (~1 min, runs parallel with sonarcloud)
   - `docker` (~3-5 mins first time, ~30s after with cache)
   - `summary` (~5 seconds)

Total first run: ~6-8 minutes. Subsequent runs: ~3-4 minutes thanks to caching.

---

## Part 7 — Verify Everything Works

### ✅ GitHub Actions
- Actions tab → workflow run → all green checkmarks
- Click into each job → expand steps → see logs

### ✅ SonarCloud
- Go to https://sonarcloud.io
- Open your project → see Quality Gate **Passed**, with metrics:
  - Bugs: 0
  - Code Smells: a few minor ones
  - Coverage: ~85%+
  - Duplications: low

### ✅ Docker Hub
- Go to https://hub.docker.com/r/YOUR_USERNAME/book-library
- See tags: `latest`, your git SHA, `main`

### ✅ Pull and Run the Image
```bash
docker pull YOUR_USERNAME/book-library:latest
docker run -p 8080:8080 YOUR_USERNAME/book-library:latest
```

Open in browser:
- http://localhost:8080/api/books → empty array
- http://localhost:8080/actuator/health → status UP

Create a book:
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Demo Book","author":"Ashraf","rating":5}'
```

Get all books:
```bash
curl http://localhost:8080/api/books
```

**You just deployed code from your laptop to a Docker registry to a running container — all automated.**

---

## Troubleshooting

### Pipeline fails on `test` job
- Check Java version: pipeline uses JDK 17
- Run `mvn test` locally to reproduce
- Check the test logs in GitHub Actions

### SonarCloud job fails with "Project not found"
- Verify `SONAR_ORGANIZATION` and `SONAR_PROJECT_KEY` match SonarCloud exactly
- Make sure Automatic Analysis is OFF in SonarCloud settings

### SonarCloud job fails with "Not authorized"
- Verify `SONAR_TOKEN` is correct — regenerate if needed
- Make sure the token has access to the project

### Docker job fails with "denied: requested access to the resource is denied"
- Verify `DOCKERHUB_USERNAME` matches your account exactly (lowercase)
- Verify `DOCKERHUB_TOKEN` is valid (regenerate if expired)
- Check that the repo exists on Docker Hub (or use a personal namespace)

### Docker job fails on a fresh setup
- Docker Hub auto-creates the repo on first push if you have access
- If issues persist, manually create `book-library` repo on Docker Hub first

### "fetch-depth: 0" warning
- This is intentional — SonarCloud needs full git history for accurate analysis

---

## Optional Enhancements

### Add a status badge to README
Replace `YOUR_USERNAME` in the badge URL at the top of README.md.

### Switch to AWS ECR instead of Docker Hub
1. Rename `.github/workflows/ci-cd.yml` → `ci-cd.yml.disabled`
2. Rename `.github/workflows/ci-cd-ecr.yml.example` → `ci-cd-ecr.yml`
3. Add AWS secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `ECR_REPOSITORY`
4. Create the ECR repository first: `aws ecr create-repository --repository-name book-library`

### Add a deployment job
Add another job to the workflow that deploys the image to EC2, EKS, App Runner, or any cloud platform.

### Add code coverage badge
SonarCloud provides one — copy the markdown from your SonarCloud project page.

---

## You're Done

Your CI/CD pipeline is now fully functional. Every commit to `main` will:

1. Run 23 tests
2. Analyze code quality with SonarCloud
3. Build and package the JAR
4. Build a Docker image
5. Push the image to Docker Hub
6. Generate a pipeline summary

**Total time to teach all of this**: 90 minutes.
**Total cost**: $0.
**Resume value**: High.
