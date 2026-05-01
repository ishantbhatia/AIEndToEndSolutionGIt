package com.ai.factory.pipeline.step;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ai.factory.agent.AgentPrompts;
import com.ai.factory.agent.OllamaAgent;
import com.ai.factory.codegen.CodeParser;
import com.ai.factory.codegen.CodeWriter;
import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineContext;

@Component
@Order(5)
public class CodeReviewStep implements PipelineStep {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewStep.class);

    private final OllamaAgent agent;
    private final CodeParser codeParser;
    private final CodeWriter codeWriter;

    @Value("${factory.pipeline.max-review-iterations:3}")
    private int maxIterations;

    public CodeReviewStep(OllamaAgent agent, CodeParser codeParser, CodeWriter codeWriter) {
        this.agent = agent;
        this.codeParser = codeParser;
        this.codeWriter = codeWriter;
    }

    @Override
    public String name() {
        return "Code Review";
    }

    @Override
    public void execute(PipelineContext ctx) throws Exception {
        String currentCode = ctx.getGeneratedCode();

        for (int i = 1; i <= maxIterations; i++) {
            ctx.getRun().setReviewIterations(i);
            ctx.getRun().setStatus(PipelineStatus.REVIEW_IN_PROGRESS);

            // review
            String review = agent.call(
                    AgentPrompts.REVIEWER,
                    "Requirements:\n" + ctx.getRequirements() + "\n\nCode:\n" + currentCode);

            ctx.setReviewFeedback(review);
            ctx.getRun().setReviewFeedback(review);
            ctx.getRun().appendLog("Review iteration " + i + " done");

            // check if passed
            String upper = review.toUpperCase();
            if (upper.contains("PASS") && !upper.contains("NEEDS_IMPROVEMENT") && !upper.contains("FAIL")) {
                ctx.getRun().appendLog("Code review PASSED");
                return;
            }

            // improve
            currentCode = agent.call(
                    AgentPrompts.IMPROVER,
                    "Review:\n" + review + "\n\nCode:\n" + currentCode);

            ctx.setGeneratedCode(currentCode);
            ctx.getRun().setGeneratedCode(currentCode);

            // rewrite files
            String projectName = "project-" + ctx.getRun().getId();
            Map<String, String> files = codeParser.parse(currentCode);
            ctx.setProjectDir(codeWriter.writeFiles(files, projectName));
            ctx.getRun().setStatus(PipelineStatus.IMPROVEMENTS_APPLIED);
            ctx.getRun().appendLog("Improvements applied (iteration " + i + ")");
        }

        log.info("Max review iterations reached");
    }
}
