package com.ai.factory.pipeline.step;

import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineContext;
import com.ai.factory.testrunner.TestRunnerService;
import com.ai.factory.testrunner.TestRunnerService.TestResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(7)
public class TestExecutionStep implements PipelineStep {

    private final TestRunnerService testRunner;

    public TestExecutionStep(TestRunnerService testRunner) {
        this.testRunner = testRunner;
    }

    @Override
    public String name() {
        return "Test Execution";
    }

    @Override
    public void execute(PipelineContext ctx) {
        TestResult result = testRunner.runTests(ctx.getProjectDir());
        ctx.getRun().setTestResult(result.output());

        if (result.passed()) {
            ctx.getRun().setStatus(PipelineStatus.TESTS_PASSED);
            ctx.getRun().appendLog("Tests PASSED");
        } else {
            ctx.getRun().appendLog("Tests FAILED (continuing): " + result.output());
        }
    }
}
