package com.ai.factory.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Writes parsed code files to the workspace directory.
 */
@Component
public class CodeWriter {

    private static final Logger log = LoggerFactory.getLogger(CodeWriter.class);

    @Value("${factory.workspace.base-dir}")
    private String baseDir;

    /** Write all files to disk and return the project root path. */
    public Path writeFiles(Map<String, String> files, String projectName) throws IOException {
        Path projectDir = Paths.get(baseDir, projectName);
        Files.createDirectories(projectDir);

        for (Map.Entry<String, String> entry : files.entrySet()) {
            Path filePath = projectDir.resolve(entry.getKey());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, entry.getValue());
        }

        log.info("Wrote {} files to {}", files.size(), projectDir);
        return projectDir;
    }
}
