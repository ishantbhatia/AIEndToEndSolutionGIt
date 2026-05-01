package com.ai.factory.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple RAG service that chunks documents and retrieves context.
 * For production, swap the in-memory store with pgvector.
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private static final int CHUNK_SIZE = 2000; // characters per chunk
    private static final int OVERLAP = 200;

    private final List<String> chunks = new ArrayList<>();

    /** Split text into overlapping chunks and store them. */
    public void ingest(String text, String source) {
        chunks.clear();

        if (text.length() <= CHUNK_SIZE) {
            chunks.add(text);
        } else {
            int start = 0;
            while (start < text.length()) {
                int end = Math.min(start + CHUNK_SIZE, text.length());
                chunks.add(text.substring(start, end));
                start += CHUNK_SIZE - OVERLAP;
            }
        }

        log.info("Ingested {} chunks from {}", chunks.size(), source);
    }

    /** Return all stored chunks as context. */
    public String retrieveContext() {
        return String.join("\n\n", chunks);
    }

    public void clear() {
        chunks.clear();
    }
}
