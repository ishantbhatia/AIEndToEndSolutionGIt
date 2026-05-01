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
@Order(4)
public class CodeGenerationStep implements PipelineStep {

    private final OllamaAgent agent;
    private final CodeParser codeParser;
    private final CodeWriter codeWriter;

    public CodeGenerationStep(OllamaAgent agent, CodeParser codeParser, CodeWriter codeWriter) {
        this.agent = agent;
        this.codeParser = codeParser;
        this.codeWriter = codeWriter;
    }

    @Override
    public String name() {
        return "Code Generation";
    }

    @Override
    public void execute(PipelineContext ctx) throws Exception {
        String userMessage = "Requirements:\n" + ctx.getRequirements()
                + "\n\nDesign:\n" + ctx.getDesign();

        String code = agent.call(AgentPrompts.DEVELOPER, userMessage);

        ctx.setGeneratedCode(code);
        ctx.getRun().setGeneratedCode(code);

        // write files to disk
        String projectName = "project-" + ctx.getRun().getId();
        Map<String, String> files = codeParser.parse(code);
        ctx.setProjectDir(codeWriter.writeFiles(files, projectName));

        ctx.getRun().setStatus(PipelineStatus.CODE_GENERATED);
        ctx.getRun().appendLog("Generated " + files.size() + " files");
    }
}
