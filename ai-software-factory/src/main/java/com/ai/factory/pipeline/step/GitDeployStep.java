package com.ai.factory.pipeline.step;

import com.ai.factory.git.GitService;
import com.ai.factory.model.PipelineStatus;
import com.ai.factory.pipeline.PipelineContext;
import org.eclipse.jgit.api.Git;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(8)
public class GitDeployStep implements PipelineStep {

    private final GitService gitService;

    public GitDeployStep(GitService gitService) {
        this.gitService = gitService;
    }

    @Override
    public String name() {
        return "Git Deploy";
    }

    @Override
    public void execute(PipelineContext ctx) throws Exception {
        var run = ctx.getRun();

        // init repo
        run.setStatus(PipelineStatus.COMMITTED);
        try (Git git = gitService.initRepo(ctx.getProjectDir())) {

            // create feature branch
            String branch = "feature/project-" + run.getId();
            gitService.createFeatureBranch(git, branch);
            run.setGitBranch(branch);

            // commit
            String hash = gitService.commitAll(git,
                    "feat: AI-generated project from " + run.getFileName());
            run.setCommitHash(hash);
            run.appendLog("Committed: " + hash);

            // push
            run.setStatus(PipelineStatus.PUSHED);
            gitService.push(git, branch);
            run.appendLog("Pushed to remote");

            // merge to prod
            run.setStatus(PipelineStatus.MERGED_TO_PROD);
            gitService.mergeToProduction(git, branch);
            run.appendLog("Merged to production");
        }
    }
}
