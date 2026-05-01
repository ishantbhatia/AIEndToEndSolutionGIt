package com.ai.factory.dto;

import com.ai.factory.model.PipelineRun;
import com.ai.factory.model.PipelineStatus;

import java.time.LocalDateTime;

public record PipelineResponse(
        Long id,
        String fileName,
        PipelineStatus status,
        String extractedRequirements,
        String designDocument,
        String reviewFeedback,
        String testResult,
        String gitBranch,
        String commitHash,
        int reviewIterations,
        String errorMessage,
        String pipelineLog,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PipelineResponse from(PipelineRun run) {
        return new PipelineResponse(
                run.getId(),
                run.getFileName(),
                run.getStatus(),
                run.getExtractedRequirements(),
                run.getDesignDocument(),
                run.getReviewFeedback(),
                run.getTestResult(),
                run.getGitBranch(),
                run.getCommitHash(),
                run.getReviewIterations(),
                run.getErrorMessage(),
                run.getPipelineLog(),
                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }

    public static PipelineResponse error(String message) {
        return new PipelineResponse(
                null, null, PipelineStatus.FAILED,
                null, null, null, null, null, null,
                0, message, null, null, null
        );
    }
}
