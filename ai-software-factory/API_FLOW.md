# API Code Flow — Step by Step

## Entry Point

```
POST http://localhost:8080/api/pipeline/upload
Body: multipart file (sample-requirements.txt)
```

---

## Step 1: PipelineController.upload()

**File:** `controller/PipelineController.java`

```java
@PostMapping("/upload")
public ResponseEntity<PipelineResponse> upload(@RequestParam("file") MultipartFile file) {
    // 1a. Read the file content
    String text = documentReaderFactory.readDocument(file);

    // 1b. Save a new pipeline run to PostgreSQL
    PipelineRun run = new PipelineRun(file.getOriginalFilename(), PipelineStatus.UPLOADED);
    run = repository.save(run);

    // 1c. Kick off the pipeline in a background thread
    orchestrator.run(run, text);

    // 1d. Return immediately with status UPLOADED (pipeline runs async)
    return ResponseEntity.accepted().body(PipelineResponse.from(run));
}
```

**What happens:** User uploads a file → controller reads it → saves to DB → starts async pipeline → returns ID to user.

---

## Step 2: DocumentReaderFactory.readDocument()

**File:** `document/DocumentReaderFactory.java`

```java
public String readDocument(MultipartFile file) {
    String extension = fileName.substring(fileName.lastIndexOf('.'));  // ".txt"

    // Finds the right reader: TxtDocumentReader, PdfDocumentReader, DocxDocumentReader, etc.
    DocumentReader reader = readers.stream()
            .filter(r -> r.supports(extension))
            .findFirst()
            .orElseThrow();

    return reader.read(file.getInputStream());  // Returns plain text
}
```

**What happens:** Checks file extension → picks the right reader → extracts plain text.

- `.txt` → `TxtDocumentReader` (reads bytes as string)
- `.pdf` → `PdfDocumentReader` (uses Apache PDFBox)
- `.docx` → `DocxDocumentReader` (uses Apache POI)
- `.doc` → `DocDocumentReader` (uses Apache POI)

---

## Step 3: PipelineOrchestrator.run()

**File:** `pipeline/PipelineOrchestrator.java`

```java
@Async("pipelineExecutor")  // Runs in a background thread pool
public void run(PipelineRun pipelineRun, String documentText) {
    PipelineContext context = new PipelineContext(pipelineRun, documentText);

    // Loops through all 8 steps in order (@Order annotation)
    for (PipelineStep step : steps) {
        step.execute(context);       // Run the step
        repository.save(pipelineRun); // Save progress to DB after each step
    }
}
```

**What happens:** Creates a shared context object → runs each step sequentially → saves to DB after each step. If any step fails, marks the run as FAILED and stops.

The `steps` list is auto-injected by Spring, ordered by `@Order(1)` through `@Order(8)`:

```
@Order(1) DocumentIngestionStep
@Order(2) RequirementsStep
@Order(3) DesignStep
@Order(4) CodeGenerationStep
@Order(5) CodeReviewStep
@Order(6) TestGenerationStep
@Order(7) TestExecutionStep
@Order(8) GitDeployStep
```

---

## Step 4: DocumentIngestionStep (Order 1)

**File:** `pipeline/step/DocumentIngestionStep.java`

```java
public void execute(PipelineContext ctx) {
    ragService.clear();
    ragService.ingest(ctx.getDocumentText(), ctx.getRun().getFileName());
}
```

**What happens:** Takes the raw text → splits into overlapping chunks (2000 chars, 200 overlap) → stores in memory for RAG retrieval later.

---

## Step 5: RequirementsStep (Order 2)

**File:** `pipeline/step/RequirementsStep.java`

```java
public void execute(PipelineContext ctx) {
    String context = ragService.retrieveContext();  // Get all chunks
    String userMessage = "Context:\n" + context + "\n\nAnalyze these requirements:\n" + ctx.getDocumentText();

    // Call Ollama with Requirements Analyst persona
    String requirements = agent.call(AgentPrompts.REQUIREMENTS_ANALYST, userMessage);

    ctx.setRequirements(requirements);  // Store in context for next steps
    ctx.getRun().setExtractedRequirements(requirements);  // Store in DB
}
```

**What happens:** Retrieves RAG context → sends to Ollama with "You are a Requirements Analyst" system prompt → gets structured requirements back → stores in context + DB.

**Ollama HTTP call (behind the scenes):**
```
POST http://localhost:11434/api/chat
{
  "model": "llama3",
  "messages": [
    {"role": "system", "content": "You are a Requirements Analyst..."},
    {"role": "user", "content": "Context: ... Analyze these requirements: ..."}
  ]
}
```

---

## Step 6: DesignStep (Order 3)

**File:** `pipeline/step/DesignStep.java`

```java
public void execute(PipelineContext ctx) {
    String design = agent.call(
        AgentPrompts.ARCHITECT,  // "You are a Software Architect..."
        "Create a technical design for:\n\n" + ctx.getRequirements()
    );
    ctx.setDesign(design);
}
```

**What happens:** Takes requirements from previous step → sends to Ollama with Architect persona → gets tech stack, DB schema, API design back.

---

## Step 7: CodeGenerationStep (Order 4)

**File:** `pipeline/step/CodeGenerationStep.java`

```java
public void execute(PipelineContext ctx) {
    String code = agent.call(
        AgentPrompts.DEVELOPER,  // "You are a Software Developer..."
        "Requirements:\n" + ctx.getRequirements() + "\n\nDesign:\n" + ctx.getDesign()
    );

    // Parse AI output into individual files
    Map<String, String> files = codeParser.parse(code);
    // files = {"src/main/java/App.java": "public class App...", "pom.xml": "<project>..."}

    // Write files to disk
    ctx.setProjectDir(codeWriter.writeFiles(files, "project-" + ctx.getRun().getId()));
    // Writes to /tmp/ai-factory-workspace/project-9/
}
```

**What happens:** Sends requirements + design to Ollama → gets code with `=== FILE: path ===` markers → `CodeParser` splits into file map → `CodeWriter` writes each file to disk.

---

## Step 8: CodeReviewStep (Order 5)

**File:** `pipeline/step/CodeReviewStep.java`

```java
public void execute(PipelineContext ctx) {
    for (int i = 1; i <= maxIterations; i++) {  // Up to 3 iterations

        // Review
        String review = agent.call(AgentPrompts.REVIEWER, "Requirements:\n" + ... + "\n\nCode:\n" + currentCode);

        // If review says PASS → stop
        if (review.contains("PASS") && !review.contains("NEEDS_IMPROVEMENT")) {
            return;
        }

        // Otherwise, improve
        currentCode = agent.call(AgentPrompts.IMPROVER, "Review:\n" + review + "\n\nCode:\n" + currentCode);

        // Rewrite files to disk
        files = codeParser.parse(currentCode);
        codeWriter.writeFiles(files, projectName);
    }
}
```

**What happens:** Reviewer agent checks code → if issues found → Improver agent fixes them → rewrites files → repeat up to 3 times or until PASS.

---

## Step 9: TestGenerationStep (Order 6)

**File:** `pipeline/step/TestGenerationStep.java`

```java
public void execute(PipelineContext ctx) {
    String testCode = agent.call(
        AgentPrompts.TEST_ENGINEER,  // "You are a Test Engineer..."
        "Requirements:\n" + ... + "\n\nCode:\n" + ctx.getGeneratedCode()
    );

    Map<String, String> testFiles = codeParser.parse(testCode);
    codeWriter.writeFiles(testFiles, projectName);
}
```

**What happens:** Test Engineer agent generates JUnit test → writes test file to disk.

---

## Step 10: TestExecutionStep (Order 7)

**File:** `pipeline/step/TestExecutionStep.java`

```java
public void execute(PipelineContext ctx) {
    TestResult result = testRunner.runTests(ctx.getProjectDir());
    // Runs: mvn test -q in /tmp/ai-factory-workspace/project-9/
}
```

**What happens:** Runs `mvn test` as a subprocess in the generated project directory → captures output → records pass/fail.

---

## Step 11: GitDeployStep (Order 8)

**File:** `pipeline/step/GitDeployStep.java`

```java
public void execute(PipelineContext ctx) {
    Git git = gitService.initRepo(ctx.getProjectDir());
    // git init → initial commit → create main + prod branches

    gitService.createFeatureBranch(git, "feature/project-9");
    // git checkout -b feature/project-9

    gitService.commitAll(git, "feat: AI-generated project from sample-requirements.txt");
    // git add . → git commit

    gitService.push(git, branch);
    // git push origin feature/project-9 → to GitHub

    gitService.mergeToProduction(git, branch);
    // git checkout prod → git merge feature/project-9 → git push origin prod
}
```

**What happens:** Init git repo → create feature branch → commit all files → push to GitHub → merge into prod branch → push prod.

---

## Checking Status

```
GET http://localhost:8080/api/pipeline/9
```

Returns the current status, logs, requirements, design, review feedback, test results, commit hash — everything stored in PostgreSQL.

---

## Full Flow Diagram

```
User uploads file
       │
       ▼
PipelineController.upload()
       │
       ├── DocumentReaderFactory → reads PDF/DOC/TXT
       ├── Save PipelineRun to PostgreSQL
       └── Start async pipeline
              │
              ▼
       PipelineOrchestrator.run()  (background thread)
              │
              ├── Step 1: DocumentIngestionStep → chunk text into RAG store
              │
              ├── Step 2: RequirementsStep → Ollama (Requirements Analyst)
              │                                POST localhost:11434/api/chat
              │
              ├── Step 3: DesignStep → Ollama (Architect)
              │                         POST localhost:11434/api/chat
              │
              ├── Step 4: CodeGenerationStep → Ollama (Developer)
              │                                 POST localhost:11434/api/chat
              │                                 → CodeParser → CodeWriter → disk
              │
              ├── Step 5: CodeReviewStep → Ollama (Reviewer + Improver loop)
              │                             POST localhost:11434/api/chat (x2-6)
              │
              ├── Step 6: TestGenerationStep → Ollama (Test Engineer)
              │                                 POST localhost:11434/api/chat
              │
              ├── Step 7: TestExecutionStep → mvn test (subprocess)
              │
              └── Step 8: GitDeployStep → git init → commit → push → merge
                                           → GitHub remote
```
