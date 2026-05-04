package tn.enicarthage.qartnet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.model.RepositoryCommitEntity;
import tn.enicarthage.qartnet.model.RepositoryFileEntity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitAnalysisService {

    private final GitStorageService gitStorageService;

    private Repository openRepository(String owner, String name) throws IOException {
        String repoPath = owner + "/" + name;
        if (!repoPath.endsWith(".git")) {
            repoPath += ".git";
        }
        File repoDir = gitStorageService.getBasePath().resolve(repoPath).toFile();
        return new FileRepositoryBuilder()
                .setGitDir(repoDir)
                .build();
    }

    public List<String> getBranches(String owner, String name) {
        List<String> branches = new ArrayList<>();
        try (Repository repository = openRepository(owner, name)) {
            List<Ref> refs = new Git(repository).branchList().call();
            for (Ref ref : refs) {
                branches.add(repository.shortenRemoteBranchName(ref.getName()));
            }
        } catch (Exception e) {
            log.error("Error listing branches for {}/{}: {}", owner, name, e.getMessage());
        }
        if (branches.isEmpty()) branches.add("main"); // Default if empty
        return branches;
    }

    public List<RepositoryFileEntity> getFiles(String owner, String name, String branch) {
        List<RepositoryFileEntity> files = new ArrayList<>();
        try (Repository repository = openRepository(owner, name)) {
            ObjectId lastCommitId = repository.resolve(branch);
            if (lastCommitId == null) return files;

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(lastCommitId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(false);
                    while (treeWalk.next()) {
                        RepositoryFileEntity file = new RepositoryFileEntity();
                        file.setName(treeWalk.getNameString());
                        file.setType(treeWalk.isSubtree() ? "directory" : "file");
                        
                        // In a real app, we would get the last commit for THIS specific file
                        file.setMessage(commit.getShortMessage());
                        file.setUpdatedAt("Recently"); 
                        
                        files.add(file);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error listing files for {}/{} branch {}: {}", owner, name, branch, e.getMessage());
        }
        return files;
    }

    public String getFileContent(String owner, String name, String branch, String filePath) {
        try (Repository repository = openRepository(owner, name)) {
            ObjectId lastCommitId = repository.resolve(branch);
            if (lastCommitId == null) return null;

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(lastCommitId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, tree)) {
                    if (treeWalk != null) {
                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        return new String(loader.getBytes(), StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error reading file {} for {}/{} branch {}: {}", filePath, owner, name, branch, e.getMessage());
        }
        return null;
    }

    public List<RepositoryCommitEntity> getCommits(String owner, String name, String branch) {
        List<RepositoryCommitEntity> commits = new ArrayList<>();
        try (Repository repository = openRepository(owner, name)) {
            Git git = new Git(repository);
            Iterable<RevCommit> commitsLog = git.log().add(repository.resolve(branch)).call();
            for (RevCommit commit : commitsLog) {
                RepositoryCommitEntity commitEntity = new RepositoryCommitEntity();
                commitEntity.setHash(commit.getName().substring(0, 7));
                commitEntity.setMessage(commit.getShortMessage());
                commitEntity.setAuthor(commit.getAuthorIdent().getName());
                commitEntity.setDate(commit.getAuthorIdent().getWhen().toString());
                populateCommitDiff(repository, commit, commitEntity);
                commits.add(commitEntity);
            }
        } catch (Exception e) {
            log.error("Error listing commits for {}/{} branch {}: {}", owner, name, branch, e.getMessage());
        }
        return commits;
    }

    public RepositoryCommitEntity getCommitDetails(String owner, String name, String hash) {
        try (Repository repository = openRepository(owner, name);
             RevWalk revWalk = new RevWalk(repository)) {
            ObjectId objectId = repository.resolve(hash);
            if (objectId == null) return null;
            RevCommit commit = revWalk.parseCommit(objectId);
            RepositoryCommitEntity commitEntity = new RepositoryCommitEntity();
            commitEntity.setHash(commit.getName().substring(0, 7));
            commitEntity.setMessage(commit.getShortMessage());
            commitEntity.setAuthor(commit.getAuthorIdent().getName());
            commitEntity.setDate(commit.getAuthorIdent().getWhen().toString());
            populateCommitDiff(repository, commit, commitEntity);
            return commitEntity;
        } catch (Exception e) {
            log.error("Error getting commit {} for {}/{}: {}", hash, owner, name, e.getMessage());
            return null;
        }
    }

    public String createBranch(String owner, String name, String branchName, String fromBranch) {
        String from = (fromBranch == null || fromBranch.isBlank()) ? "main" : fromBranch.trim();
        try (Repository repository = openRepository(owner, name);
             Git git = new Git(repository)) {
            git.branchCreate()
                    .setName(branchName)
                    .setStartPoint(from)
                    .call();
            return "Branch " + branchName + " created from " + from;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create branch: " + e.getMessage(), e);
        }
    }

    public String mergeBranch(String owner, String name, String sourceBranch, String targetBranch) {
        try (Repository repository = openRepository(owner, name);
             RevWalk revWalk = new RevWalk(repository)) {
            ObjectId sourceId = repository.resolve(sourceBranch);
            ObjectId targetId = repository.resolve(targetBranch);
            if (sourceId == null || targetId == null) {
                throw new IllegalArgumentException("Invalid source or target branch");
            }

            RevCommit sourceCommit = revWalk.parseCommit(sourceId);
            RevCommit targetCommit = revWalk.parseCommit(targetId);
            if (!revWalk.isMergedInto(targetCommit, sourceCommit)) {
                throw new IllegalStateException("Non fast-forward merge not supported in MVP");
            }

            RefUpdate refUpdate = repository.updateRef("refs/heads/" + targetBranch);
            refUpdate.setNewObjectId(sourceCommit.getId());
            refUpdate.setExpectedOldObjectId(targetCommit.getId());
            RefUpdate.Result result = refUpdate.update();
            if (result == RefUpdate.Result.FAST_FORWARD || result == RefUpdate.Result.NEW || result == RefUpdate.Result.FORCED) {
                return "Merged " + sourceBranch + " into " + targetBranch + " (fast-forward)";
            }
            throw new IllegalStateException("Merge failed: " + result.name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge branches: " + e.getMessage(), e);
        }
    }

    public void streamZip(String owner, String name, String branch, OutputStream outputStream) throws IOException {
        try (Repository repository = openRepository(owner, name);
             ZipOutputStream zipOut = new ZipOutputStream(outputStream);
             RevWalk revWalk = new RevWalk(repository)) {

            ObjectId commitId = repository.resolve(branch);
            if (commitId == null) return;

            RevCommit commit = revWalk.parseCommit(commitId);
            RevTree tree = commit.getTree();

            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);

                while (treeWalk.next()) {
                    String path = treeWalk.getPathString();
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    zipOut.putNextEntry(new ZipEntry(path));
                    loader.copyTo(zipOut);
                    zipOut.closeEntry();
                }
            }
            zipOut.finish();
        }
    }

    private void populateCommitDiff(Repository repository, RevCommit commit, RepositoryCommitEntity target) throws IOException, GitAPIException {
        if (commit.getParentCount() == 0) {
            target.setAdditions(0);
            target.setDeletions(0);
            target.setModifiedPaths(new ArrayList<>());
            return;
        }

        RevCommit parent;
        try (RevWalk walk = new RevWalk(repository)) {
            parent = walk.parseCommit(commit.getParent(0).getId());
        }
        Set<String> paths = new HashSet<>();
        int additions = 0;
        int deletions = 0;

        try (DiffFormatter formatter = new DiffFormatter(new ByteArrayOutputStream())) {
            formatter.setRepository(repository);
            formatter.setDetectRenames(true);
            List<DiffEntry> entries = formatter.scan(parent.getTree(), commit.getTree());
            for (DiffEntry entry : entries) {
                String path = entry.getNewPath() != null && !DiffEntry.DEV_NULL.equals(entry.getNewPath())
                        ? entry.getNewPath()
                        : entry.getOldPath();
                paths.add(path);
                EditList edits = formatter.toFileHeader(entry).toEditList();
                for (Edit edit : edits) {
                    additions += edit.getEndB() - edit.getBeginB();
                    deletions += edit.getEndA() - edit.getBeginA();
                }
            }
        }
        target.setModifiedPaths(new ArrayList<>(paths));
        target.setAdditions(additions);
        target.setDeletions(deletions);
    }
}
