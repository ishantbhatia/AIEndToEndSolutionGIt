package com.ai.factory.pipeline.step;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ai.factory.agent.AgentPrompts;
import com.ai.factory.agent.OllamaAgent;
import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineContext;

@Component
@Order(3)
public class DesignStep implements PipelineStep {

    private final OllamaAgent agent;

    public DesignStep(OllamaAgent agent) {
        this.agent = agent;
    }

    @Override
    public String name() {
        return "Architecture Design";
    }

    @Override
    public void execute(PipelineContext ctx) {
        String design = agent.call(
                AgentPrompts.ARCHITECT,
                "Create a technical design for:\n\n" + ctx.getRequirements());

        ctx.setDesign(design);
        ctx.getRun().setDesignDocument(design);
        ctx.getRun().setStatus(PipelineStatus.DESIGN_COMPLETE);
        ctx.getRun().appendLog("Technical design completed");
    }
}
