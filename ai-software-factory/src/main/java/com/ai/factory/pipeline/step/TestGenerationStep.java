package com.ai.factory.pipeline.step;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ai.factory.agent.AgentPrompts;
import com.ai.factory.agent.OllamaAgent;
import com.ai.factory.codegen.CodeParser;
import com.ai.factory.codegen.CodeWriter;
import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineContext;

@Component
@Order(6)
public class TestGenerationStep implements PipelineStep {

    private final OllamaAgent agent;
    private final CodeParser codeParser;
    private final CodeWriter codeWriter;

    public TestGenerationStep(OllamaAgent agent, CodeParser codeParser, CodeWriter codeWriter) {
        this.agent = agent;
        this.codeParser = codeParser;
        this.codeWriter = codeWriter;
    }

    @Override
    public String name() {
        return "Test Generation";
    }

    @Override
    public void execute(PipelineContext ctx) throws Exception {
        String testCode = agent.call(
                AgentPrompts.TEST_ENGINEER,
                "Requirements:\n" + ctx.getRequirements() + "\n\nCode:\n" + ctx.getGeneratedCode());

        ctx.setTestCode(testCode);
        ctx.getRun().setTestCode(testCode);

        // write test files
        String projectName = "project-" + ctx.getRun().getId();
        Map<String, String> testFiles = codeParser.parse(testCode);
        codeWriter.writeFiles(testFiles, projectName);

        ctx.getRun().setStatus(PipelineStatus.TESTS_GENERATED);
        ctx.getRun().appendLog("Test case generated");
    }
}
