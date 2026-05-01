package com.ai.factory.rag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RagServiceTest {

    private final RagService ragService = new RagService();

    @Test
    void shouldIngestAndRetrieveContext() {
        ragService.ingest("Users can register with email and password.", "req.pdf");

        String context = ragService.retrieveContext();

        assertFalse(context.isBlank());
        assertTrue(context.contains("register"));
    }

    @Test
    void shouldChunkLargeDocuments() {
        String largeText = "A".repeat(5000);
        ragService.ingest(largeText, "big.pdf");

        String context = ragService.retrieveContext();
        assertFalse(context.isBlank());
    }

    @Test
    void shouldClearStore() {
        ragService.ingest("some text", "file.txt");
        ragService.clear();

        String context = ragService.retrieveContext();
        assertTrue(context.isBlank());
    }
}
