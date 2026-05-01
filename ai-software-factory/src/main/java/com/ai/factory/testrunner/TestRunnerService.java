package com.ai.factory.testrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class TestRunnerService {

    private static final Logger log = LoggerFactory.getLogger(TestRunnerService.class);

    @Value("${factory.pipeline.test-timeout-seconds:120}")
    private int timeoutSeconds;

    public record TestResult(boolean passed, String output) {}

    /** Run `mvn test` in the given project directory. */
    public TestResult runTests(Path projectDir) {
        log.info("Running tests in {}", projectDir);
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "test", "-q");
            pb.directory(projectDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            String output = readOutput(process);

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new TestResult(false, "Timed out after " + timeoutSeconds + "s\n" + output);
            }

            boolean passed = process.exitValue() == 0;
            log.info("Tests {}", passed ? "PASSED" : "FAILED");
            return new TestResult(passed, output);

        } catch (IOException | InterruptedException e) {
            log.error("Test execution failed", e);
            return new TestResult(false, "Error: " + e.getMessage());
        }
    }

    private String readOutput(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
