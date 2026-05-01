package com.ai.factory.document;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Picks the right DocumentReader based on file extension and extracts text.
 */
@Component
public class DocumentReaderFactory {

    private final List<DocumentReader> readers;

    public DocumentReaderFactory(List<DocumentReader> readers) {
        this.readers = readers;
    }

    public String readDocument(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("File must have an extension (.pdf, .doc, .docx, .txt)");
        }

        String extension = fileName.substring(fileName.lastIndexOf('.'));

        DocumentReader reader = readers.stream()
                .filter(r -> r.supports(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported file type: " + extension + ". Supported: .pdf, .doc, .docx, .txt"));

        return reader.read(file.getInputStream());
    }
}
