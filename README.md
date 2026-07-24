# UnFumble

AI-powered profile photo generator — turn any uploaded photo into the right image for the job: passport photos, LinkedIn headshots, Instagram-ready pictures, job application photos, and more.

Built as a learning project by two developers exploring AI from two different ecosystems — **Java/Spring Boot** and **Python/FastAPI** — working together on one product.

---

## What It Does

1. User uploads one or more photos and picks a use-case (passport, LinkedIn, Instagram, etc.)
2. If multiple photos are uploaded, the system picks the best one using a vision-capable AI model
3. The selected photo + use-case is sent for AI-powered image editing (background, crop, lighting, style — tailored to the use-case)
4. The finished photo is stored and served back to the user

---

## Architecture

```
React Frontend
      │
 
  - Auth & user management
  - Upload validation
  - Job tracking (PENDING → PROCESSING → DONE)
  - Multi-photo ranking (     ▼
Java Spring Boot Gateway  (:8080)Spring AI + vision model)
  - Calls the Python AI Engine
      │
      ▼
Python FastAPI AI Engine  (:8001)
  - Use-case-specific prompt building
  - Image-editing AI API call
  - Async processing via Celery
  - Stores result in object storage
      │
      ▼
   MinIO (S3-compatible) · Postgres · Redis
```

**Java owns:** orchestration, users, jobs, and pre-processing intelligence (ranking photos before they're processed).
**Python owns:** the image-editing AI integration, prompt engineering per use-case, and async task processing.

Full request flow, contracts, and decision history are in [`docs/`](./docs).

---

## Tech Stack

| Layer | Tech |
|---|---|
| Gateway | Java 21, Spring Boot 3.3.x, Spring AI, Spring Security, Spring Data JPA |
| AI Engine | Python 3.12, FastAPI, Celery, Pydantic v2 |
| Frontend | React (placeholder — coming soon) |
| Database | PostgreSQL |
| Queue / Broker | Redis |
| Object Storage | MinIO (local, S3-compatible) |
| AI | Vision model for ranking (Java) · Image-editing model for generation (Python) |

Everything runs locally and free during development — no paid infrastructure required.

---

## Project Structure

```
headshot/
├── services/
│   ├── gateway/        # Java Spring Boot — orchestration, auth, jobs
│   ├── ai-engine/       # Python FastAPI — image editing AI, Celery workers
│   └── frontend/        # React app (placeholder)
├── infra/
│   └── docker-compose.yml   # postgres, redis, minio
├── docs/
│   └── contracts/       # shared API contracts between gateway and ai-engine
└── .github/workflows/    # CI for both services
```

---

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21 + Maven
- Python 3.12 + [uv](https://docs.astral.sh/uv/)
- Node.js (for the frontend, once it's built out)

### 1. Quick Start (All-in-One Command)

We have a script that spins up the infrastructure, Python AI Engine, and Java Gateway all at once in the background.

```bash
git clone https://github.com/MoggStack/Unfumble.git
cd Unfumble
./start.sh
```

*(Press `Ctrl+C` to gracefully shut down all services at once).*

---

### 2. Manual Start (Alternative)

If you prefer to start them individually:

**A. Start Infrastructure**
```bash
cd infra && docker compose up -d && cd ..
```
*(Starts Postgres at `:5432`, Redis at `:6379`, and MinIO at `:9000`)*

**B. Run the AI Engine (Python)**
```bash
cd services/ai-engine
cp .env.example .env
uv venv && source .venv/bin/activate
uv pip install -e .
uvicorn app.main:app --reload --port 8001
```

**C. Run the Gateway (Java)**
```bash
cd services/gateway
./mvnw spring-boot:run
```

**D. Create the MinIO bucket**
Open `http://localhost:9001` (login: `minioadmin` / `minioadmin`) and create a bucket named `unfumble-images`, or via CLI:
```bash
docker run --rm --network infra_default minio/mc \
  alias set local http://minio:9000 minioadmin minioadmin && \
  mc mb local/unfumble-images
```

---

## Environment Variables

Each service has its own `.env.example` — copy it to `.env` and fill in:

- `GEMINI_API_KEY` (free from Google AI Studio) — needed for AI photo ranking and image processing
- Database, Redis, and MinIO credentials default to the local Docker Compose values

Never commit `.env` files — only `.env.example`.

---

## Contributing (Team Workflow)

This is a two-person project with a clean ownership split:

- **Java dev** → commits only inside `services/gateway/`
- **Python dev** → commits only inside `services/ai-engine/`
- Shared files (`infra/`, `docs/contracts/`) require both people to review

**Branching:**

```
main                ← protected, always deployable
  feat/java/...     ← Java feature branches
  feat/python/...   ← Python feature branches
```

Workflow: branch from `main` → commit → push → open a PR → 1 approval required → merge.

No direct pushes to `main`.

---

## Roadmap & Responsibilities

To make it perfectly clear who is building what, here is the detailed breakdown of our current roadmap:

### ☕ Java Responsibilities (`services/gateway/`)
*The "Orchestrator" and "Front Door" of the application.*
- [x] **Auth (Signup/Login):** Implement Spring Security with JWT/sessions.
- [x] **Photo Upload API:** Endpoints to receive uploads and save to MinIO.
- [x] **Multi-Photo Ranking:** Use Spring AI to analyze multiple uploaded photos and pick the best one for the chosen use-case before sending it to Python.
- [x] **Job Tracking:** Create JPA entities to track job state (`PENDING`, `PROCESSING`, `DONE`).
- [ ] **AI Engine Client:** HTTP client to trigger the Python AI engine and an endpoint to receive callbacks when jobs are complete.

### 🐍 Python Responsibilities (`services/ai-engine/`)
*The "AI Brain" handling heavy image processing.*
- [ ] **Async Task Setup:** Configure Celery with Redis so AI generation happens asynchronously without blocking HTTP requests.
- [ ] **Prompt Engineering:** Logic to convert the requested "use-case" (e.g., LinkedIn vs Instagram) into a highly specific AI prompt.
- [ ] **Image-Editing AI Integration:** Call the actual third-party image generation model (via API) with the image and prompt.
- [ ] **Save & Callback:** Save the generated image back to MinIO and ping the Java Gateway to notify it that the job is `DONE`.

### 🤝 Shared/Other Responsibilities
- [ ] **API Contracts:** Define exact JSON structures in `docs/contracts/` for how Java and Python communicate.
- [ ] **React Frontend:** Build the UI for upload, use-case selection, and job status polling.
- [ ] **Deployment:** Setup production infrastructure.

---

## License

TBD