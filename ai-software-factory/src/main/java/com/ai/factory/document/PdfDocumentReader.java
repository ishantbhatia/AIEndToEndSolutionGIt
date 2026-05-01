package com.ai.factory.document;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class PdfDocumentReader implements DocumentReader {

    @Override
    public boolean supports(String fileExtension) {
        return ".pdf".equalsIgnoreCase(fileExtension);
    }

    @Override
    public String read(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }
}
