package com.ai.factory.pipeline.step;

import com.ai.factory.pipeline.PipelineContext;

/**
 * A single step in the pipeline.
 */
public interface PipelineStep {

    /** Human-readable name for logging. */
    String name();

    /** Execute this step, reading/writing from the context. */
    void execute(PipelineContext context) throws Exception;
}
