package com.ai.factory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_runs")
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private PipelineStatus status;

    @Column(columnDefinition = "TEXT")
    private String extractedRequirements;

    @Column(columnDefinition = "TEXT")
    private String designDocument;

    @Column(columnDefinition = "TEXT")
    private String generatedCode;

    @Column(columnDefinition = "TEXT")
    private String reviewFeedback;

    @Column(columnDefinition = "TEXT")
    private String testCode;

    @Column(columnDefinition = "TEXT")
    private String testResult;

    private String gitBranch;
    private String commitHash;
    private int reviewIterations;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String pipelineLog;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PipelineRun() {
    }

    public PipelineRun(String fileName, PipelineStatus status) {
        this.fileName = fileName;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void appendLog(String message) {
        String timestamp = LocalDateTime.now().toString();
        String entry = "[" + timestamp + "] " + message + "\n";
        this.pipelineLog = (this.pipelineLog == null ? "" : this.pipelineLog) + entry;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public PipelineStatus getStatus() { return status; }
    public void setStatus(PipelineStatus status) { this.status = status; }

    public String getExtractedRequirements() { return extractedRequirements; }
    public void setExtractedRequirements(String extractedRequirements) { this.extractedRequirements = extractedRequirements; }

    public String getDesignDocument() { return designDocument; }
    public void setDesignDocument(String designDocument) { this.designDocument = designDocument; }

    public String getGeneratedCode() { return generatedCode; }
    public void setGeneratedCode(String generatedCode) { this.generatedCode = generatedCode; }

    public String getReviewFeedback() { return reviewFeedback; }
    public void setReviewFeedback(String reviewFeedback) { this.reviewFeedback = reviewFeedback; }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    public String getTestResult() { return testResult; }
    public void setTestResult(String testResult) { this.testResult = testResult; }

    public String getGitBranch() { return gitBranch; }
    public void setGitBranch(String gitBranch) { this.gitBranch = gitBranch; }

    public String getCommitHash() { return commitHash; }
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }

    public int getReviewIterations() { return reviewIterations; }
    public void setReviewIterations(int reviewIterations) { this.reviewIterations = reviewIterations; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getPipelineLog() { return pipelineLog; }
    public void setPipelineLog(String pipelineLog) { this.pipelineLog = pipelineLog; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
