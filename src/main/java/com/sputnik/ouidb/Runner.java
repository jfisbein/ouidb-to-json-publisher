package com.sputnik.ouidb;

import com.sputnik.ouidb.model.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class Runner {
    private static OUIDBDownloader downloader = new OUIDBDownloader();
    private static OUIDBConverter converter = new OUIDBConverter();
    private File dataPath;
    private String repoRemoteUri;
    private String repoUsername;
    private String repoPassword;

    public Runner(File dataPath, String repoRemoteUri, String repoUsername, String repoPassword) {
        this.dataPath = dataPath;
        this.repoRemoteUri = repoRemoteUri;
        this.repoUsername = repoUsername;
        this.repoPassword = repoPassword;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            log.error("Expected four arguments");
            log.error("Usage java -jar ouidb-to-json-publisher.jar {DATA_FOLDER} {REMOTE_REPO_URI} {REMOTE_REPO_USERNAME} {REMOTE_REPO_PASSWORD}");
        } else {
            log.info("Running with {} {} {} {}", args);
            new Runner(new File(args[0]), args[1], args[2], args[3]).run();
        }
    }

    private void run() throws IOException, GitAPIException {
        Git git = getGitRepo();

        Map<String, Organization> parsedDB = downloader.getParsedDB();
        String json = converter.convertToJson(parsedDB);
        FileWriter writer = new FileWriter(getOuiDBFile());
        IOUtils.write(json, writer);
        writer.flush();
        Status status = git.status().call();
        if (!status.getModified().isEmpty()) {
            log.info("OUIDB file changed, uploading to git repo.");
            git.add().addFilepattern(getOuiDBFile().getName()).call();
            git.commit().setMessage("Updated OUIDB json file").call();
            git.push().call();
        } else {
            log.info("No changes detected on OUIDB File, nothing to do.");
        }
        
        log.info("Done :-)");
    }

    private File getOuiDBFile() {
        return new File(dataPath, "ouidb.json");
    }

    private Git getGitRepo() throws GitAPIException, IOException {
        Git git;
        if (!dataPath.exists()) {
            log.info("Git repo path does not exists, creating it.");
            dataPath.mkdirs();
        }
        try {
            log.info("Trying to open existing it repo in {}", dataPath);
            git = Git.open(dataPath);
            log.info("Repo opened, pulling latest changes.");
            git.pull().call();
        } catch (RepositoryNotFoundException e) {
            log.info("Repo does not exists, cloning from {}", repoRemoteUri);
            CloneCommand cloneCommand = Git.cloneRepository().setDirectory(dataPath).setURI(repoRemoteUri);

            if (StringUtils.isNotBlank(repoUsername) || StringUtils.isNotBlank(repoPassword)) {
                log.info("Using provided credentials.");
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(repoUsername, repoPassword));
            }

            git = cloneCommand.call();
        }

        return git;
    }
}
