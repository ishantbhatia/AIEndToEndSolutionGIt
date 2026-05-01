package com.ai.factory.pipeline.step;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ai.factory.agent.AgentPrompts;
import com.ai.factory.agent.OllamaAgent;
import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineContext;
import com.ai.factory.rag.RagService;

@Component
@Order(2)
public class RequirementsStep implements PipelineStep {

    private final OllamaAgent agent;
    private final RagService ragService;

    public RequirementsStep(OllamaAgent agent, RagService ragService) {
        this.agent = agent;
        this.ragService = ragService;
    }

    @Override
    public String name() {
        return "Requirements Analysis";
    }

    @Override
    public void execute(PipelineContext ctx) {
        String context = ragService.retrieveContext();
        String userMessage = "Context:\n" + context + "\n\nAnalyze these business requirements:\n" + ctx.getDocumentText();

        String requirements = agent.call(AgentPrompts.REQUIREMENTS_ANALYST, userMessage);

        ctx.setRequirements(requirements);
        ctx.getRun().setExtractedRequirements(requirements);
        ctx.getRun().setStatus(PipelineStatus.REQUIREMENTS_EXTRACTED);
        ctx.getRun().appendLog("Requirements extracted");
    }
}
