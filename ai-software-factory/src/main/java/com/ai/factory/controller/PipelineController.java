package com.ai.factory.controller;

import com.ai.factory.document.DocumentReaderFactory;
import com.ai.factory.dto.PipelineResponse;
import com.ai.factory.model.PipelineRun;
import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineOrchestrator;
import com.ai.factory.repository.PipelineRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private static final Logger log = LoggerFactory.getLogger(PipelineController.class);

    private final DocumentReaderFactory documentReaderFactory;
    private final PipelineOrchestrator orchestrator;
    private final PipelineRunRepository repository;

    public PipelineController(DocumentReaderFactory documentReaderFactory,
                              PipelineOrchestrator orchestrator,
                              PipelineRunRepository repository) {
        this.documentReaderFactory = documentReaderFactory;
        this.orchestrator = orchestrator;
        this.repository = repository;
    }

    /** Upload a requirements doc and kick off the pipeline. */
    @PostMapping("/upload")
    public ResponseEntity<PipelineResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Received file: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

            String text = documentReaderFactory.readDocument(file);

            PipelineRun run = new PipelineRun(file.getOriginalFilename(), PipelineStatus.UPLOADED);
            run.appendLog("File uploaded: " + file.getOriginalFilename());
            run = repository.save(run);

            orchestrator.run(run, text);

            return ResponseEntity.accepted().body(PipelineResponse.from(run));
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.internalServerError().body(PipelineResponse.error(e.getMessage()));
        }
    }

    /** Get status of a pipeline run. */
    @GetMapping("/{id}")
    public ResponseEntity<PipelineResponse> getStatus(@PathVariable Long id) {
        return repository.findById(id)
                .map(run -> ResponseEntity.ok(PipelineResponse.from(run)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** List all pipeline runs. */
    @GetMapping
    public ResponseEntity<List<PipelineResponse>> listRuns() {
        List<PipelineResponse> runs = repository.findAll().stream()
                .map(PipelineResponse::from)
                .toList();
        return ResponseEntity.ok(runs);
    }
}
