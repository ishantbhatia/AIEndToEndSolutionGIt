package com.ai.factory.git;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Git operations using JGit.
 *
 * Configure via application.yml or env vars:
 *   GIT_REMOTE_URL  = https://github.com/ishantbhatia/AIEndToEndSolution.git
 *   GIT_USERNAME    = ishantbhatia
 *   GIT_PASSWORD    = <your GitHub personal access token>
 */
@Service
public class GitService {

    private static final Logger log = LoggerFactory.getLogger(GitService.class);

    @Value("${factory.git.remote-url:}")
    private String remoteUrl;

    @Value("${factory.git.username:}")
    private String username;

    // For GitHub, this should be a Personal Access Token (PAT), not your account password
    @Value("${factory.git.password:}")
    private String token;

    @Value("${factory.git.author-name:Ishant Bhatia}")
    private String authorName;

    @Value("${factory.git.author-email:ishantbhatia@example.com}")
    private String authorEmail;

    @Value("${factory.git.main-branch:main}")
    private String mainBranch;

    @Value("${factory.git.prod-branch:prod}")
    private String prodBranch;

    private CredentialsProvider getCredentials() {
        if (username != null && !username.isBlank() && token != null && !token.isBlank()) {
            return new UsernamePasswordCredentialsProvider(username, token);
        }
        log.warn("Git credentials not configured — push/pull will only work for public repos or SSH");
        return null;
    }

    /** Init repo, create initial commit, create main + prod branches. */
    public Git initRepo(Path projectDir) throws GitAPIException, IOException {
        Git git = Git.init().setDirectory(projectDir.toFile()).call();

        git.add().addFilepattern(".").call();
        git.commit()
                .setAuthor(authorName, authorEmail)
                .setMessage("Initial commit by AI Software Factory")
                .call();

        // rename default branch to main
        git.branchRename()
                .setOldName(git.getRepository().getBranch())
                .setNewName(mainBranch)
                .call();

        // create prod branch from main
        git.branchCreate().setName(prodBranch).call();

        log.info("Initialized repo at {} with branches [{}, {}]", projectDir, mainBranch, prodBranch);
        return git;
    }

    /** Create and checkout a feature branch. */
    public String createFeatureBranch(Git git, String branchName) throws GitAPIException {
        git.checkout().setCreateBranch(true).setName(branchName).call();
        log.info("Created branch: {}", branchName);
        return branchName;
    }

    /** Stage everything, commit, return the hash. */
    public String commitAll(Git git, String message) throws GitAPIException {
        git.add().addFilepattern(".").call();
        RevCommit commit = git.commit()
                .setAuthor(authorName, authorEmail)
                .setMessage(message)
                .call();
        String hash = commit.getId().getName();
        log.info("Committed: {}", hash.substring(0, 8));
        return hash;
    }

    /** Push a branch to the remote. Skips if no remote URL or no credentials. */
    public void push(Git git, String branchName) throws GitAPIException {
        if (remoteUrl == null || remoteUrl.isBlank()) {
            log.warn("No remote URL configured — skipping push");
            return;
        }

        // add remote origin if not already set
        try {
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new URIish(remoteUrl))
                    .call();
        } catch (Exception e) {
            log.debug("Remote origin may already exist: {}", e.getMessage());
        }

        var pushCmd = git.push().setRemote("origin").add(branchName);

        CredentialsProvider creds = getCredentials();
        if (creds != null) {
            pushCmd.setCredentialsProvider(creds);
        }

        pushCmd.call();
        log.info("Pushed branch {} to {}", branchName, remoteUrl);
    }

    /** Merge feature branch into prod, then push prod. */
    public void mergeToProduction(Git git, String featureBranch) throws GitAPIException, IOException {
        git.checkout().setName(prodBranch).call();

        Ref featureRef = git.getRepository().findRef(featureBranch);
        if (featureRef == null) {
            throw new IllegalStateException("Branch not found: " + featureBranch);
        }

        git.merge()
                .include(featureRef)
                .setFastForward(MergeCommand.FastForwardMode.FF)
                .setMessage("Merge " + featureBranch + " into " + prodBranch)
                .call();

        log.info("Merged {} into {}", featureBranch, prodBranch);
        push(git, prodBranch);
    }
}
