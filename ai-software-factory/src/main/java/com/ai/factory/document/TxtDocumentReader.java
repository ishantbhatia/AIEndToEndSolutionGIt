package com.ai.factory.document;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TxtDocumentReader implements DocumentReader {

    @Override
    public boolean supports(String fileExtension) {
        return ".txt".equalsIgnoreCase(fileExtension);
    }

    @Override
    public String read(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
