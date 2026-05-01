package com.ai.factory.pipeline;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ai.factory.model.PipelineRun;
import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.step.PipelineStep;
import com.ai.factory.repository.PipelineRunRepository;

/**
 * Thin orchestrator — just iterates through the ordered pipeline steps.
 * Each step is a separate @Component with its own responsibility.
 */
@Service
public class PipelineOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PipelineOrchestrator.class);

    private final List<PipelineStep> steps;
    private final PipelineRunRepository repository;

    public PipelineOrchestrator(List<PipelineStep> steps, PipelineRunRepository repository) {
        this.steps = steps;
        this.repository = repository;
    }

    @Async("pipelineExecutor")
    public void run(PipelineRun pipelineRun, String documentText) {
        PipelineContext context = new PipelineContext(pipelineRun, documentText);

        log.info("=== Pipeline START: {} ===", pipelineRun.getFileName());

        for (PipelineStep step : steps) {
            try {
                log.info("Running step: {}", step.name());
                pipelineRun.appendLog("Starting step: " + step.name());
                repository.save(pipelineRun);
                step.execute(context);
                repository.save(pipelineRun);
            } catch (Exception e) {
                log.error("Step [{}] failed", step.name(), e);
                pipelineRun.setStatus(PipelineStatus.FAILED);
                pipelineRun.setErrorMessage(step.name() + ": " + e.getMessage());
                pipelineRun.appendLog("FAILED at " + step.name() + ": " + e.getMessage());
                repository.save(pipelineRun);
                return;
            }
        }

        log.info("=== Pipeline COMPLETE: {} ===", pipelineRun.getFileName());
    }
}
