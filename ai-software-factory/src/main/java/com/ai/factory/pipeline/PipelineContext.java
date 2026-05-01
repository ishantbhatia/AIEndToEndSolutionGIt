package com.ai.factory.pipeline;

import com.ai.factory.model.PipelineRun;

import java.nio.file.Path;

/**
 * Mutable context passed through each pipeline step.
 * Each step reads what it needs and writes its output here.
 */
public class PipelineContext {

    private final PipelineRun run;
    private String documentText;
    private String requirements;
    private String design;
    private String generatedCode;
    private String reviewFeedback;
    private String testCode;
    private Path projectDir;

    public PipelineContext(PipelineRun run, String documentText) {
        this.run = run;
        this.documentText = documentText;
    }

    public PipelineRun getRun()                     { return run; }

    public String getDocumentText()                  { return documentText; }

    public String getRequirements()                  { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getDesign()                        { return design; }
    public void setDesign(String design)             { this.design = design; }

    public String getGeneratedCode()                 { return generatedCode; }
    public void setGeneratedCode(String code)        { this.generatedCode = code; }

    public String getReviewFeedback()                { return reviewFeedback; }
    public void setReviewFeedback(String feedback)   { this.reviewFeedback = feedback; }

    public String getTestCode()                      { return testCode; }
    public void setTestCode(String testCode)         { this.testCode = testCode; }

    public Path getProjectDir()                      { return projectDir; }
    public void setProjectDir(Path projectDir)       { this.projectDir = projectDir; }
}
