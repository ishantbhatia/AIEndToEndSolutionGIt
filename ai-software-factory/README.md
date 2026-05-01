# AI Software Factory

End-to-end AI-powered software factory using **Ollama** (local LLM) and **PostgreSQL**.

Upload a business requirements document (PDF, DOC, DOCX, TXT) → the system automatically analyzes, designs, codes, reviews, tests, and deploys.

## Pipeline

```
Upload → Ingest (RAG) → Requirements → Design → Code → Review → Improve → Test → Git → Prod
```

| Step | Agent | What happens |
|------|-------|-------------|
| 1 | — | Parse PDF/DOC/DOCX, chunk into RAG store |
| 2 | Requirements Analyst | Structured requirements from raw text |
| 3 | Architect | Technical design, DB schema, API spec |
| 4 | Developer | Generate complete Spring Boot project |
| 5 | Reviewer | Review for bugs, security, quality |
| 6 | Improver | Fix issues (up to 3 iterations) |
| 7 | Test Engineer | Generate JUnit test case |
| 8 | — | Run `mvn test` |
| 9 | — | Git commit → push → merge to prod |

## Project Structure

```
com.ai.factory
├── controller/        REST API endpoints
├── dto/               Response records
├── exception/         Global error handling
├── model/             JPA entities
├── repository/        Spring Data repos
├── document/          PDF/DOC/DOCX/TXT readers (one per format)
├── agent/             Ollama agent + prompt definitions
├── rag/               RAG chunking and retrieval
├── codegen/           Parse AI output → write files to disk
├── git/               JGit operations (init, commit, push, merge)
├── testrunner/        Run mvn test on generated projects
├── pipeline/          Orchestrator + step interface
│   └── step/          One class per pipeline step
└── config/            Async config
```

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- Ollama with a model pulled (e.g. `ollama pull llama3`)

### Run locally

```bash
# Start PostgreSQL (if not running)
# Start Ollama: ollama serve

# Pull a model
ollama pull llama3

# Set env vars
export DB_HOST=localhost DB_PORT=5432 DB_NAME=factorydb DB_USER=factory DB_PASSWORD=factory123
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=llama3

cd ai-software-factory
mvn spring-boot:run
```

### Run with Docker Compose

```bash
docker-compose up --build

# Then pull a model into the Ollama container:
docker exec -it ai-software-factory-ollama-1 ollama pull llama3
```

## API

```bash
# Upload requirements and start pipeline
curl -X POST http://localhost:8080/api/pipeline/upload -F "file=@samples/sample-requirements.txt"

# Check status
curl http://localhost:8080/api/pipeline/1

# List all runs
curl http://localhost:8080/api/pipeline

# Health check
curl http://localhost:8080/api/health
```

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama server URL |
| `OLLAMA_MODEL` | `llama3` | Model to use |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `factorydb` | Database name |
| `DB_USER` | `factory` | Database user |
| `DB_PASSWORD` | `factory123` | Database password |
| `GIT_REMOTE_URL` | empty | Git remote for push |
| `FACTORY_WORKSPACE` | `/tmp/ai-factory-workspace` | Where generated projects are written |
