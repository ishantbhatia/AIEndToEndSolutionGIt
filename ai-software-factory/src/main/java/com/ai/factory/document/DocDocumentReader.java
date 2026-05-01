package com.ai.factory.document;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class DocDocumentReader implements DocumentReader {

    @Override
    public boolean supports(String fileExtension) {
        return ".doc".equalsIgnoreCase(fileExtension);
    }

    @Override
    public String read(InputStream inputStream) throws IOException {
        try (HWPFDocument doc = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
