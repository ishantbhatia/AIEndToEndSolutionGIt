package com.ai.factory.pipeline.step;

import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineContext;
import com.ai.factory.rag.RagService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DocumentIngestionStep implements PipelineStep {

    private final RagService ragService;

    public DocumentIngestionStep(RagService ragService) {
        this.ragService = ragService;
    }

    @Override
    public String name() {
        return "Document Ingestion";
    }

    @Override
    public void execute(PipelineContext ctx) {
        ctx.getRun().setStatus(PipelineStatus.PARSING);
        ragService.clear();
        ragService.ingest(ctx.getDocumentText(), ctx.getRun().getFileName());
        ctx.getRun().appendLog("Document ingested into RAG pipeline");
    }
}
