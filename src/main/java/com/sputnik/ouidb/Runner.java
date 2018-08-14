package com.sputnik.ouidb;

import com.sputnik.ouidb.model.Organization;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Runner {
    private static OUIDBDownloader downloader = new OUIDBDownloader();
    private static OUIDBConverter converter = new OUIDBConverter();
    private File dataPath;
    private String repoRemoteUri;

    public Runner(File dataPath, String repoRemoteUri) {
        this.dataPath = dataPath;
        this.repoRemoteUri = repoRemoteUri;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Expected two argument");
        } else {
            new Runner(new File(args[0]), args[1]).run();
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
            //TODO upload to git repo
            git.add().addFilepattern(getOuiDBFile().getName()).call();
            git.commit().setMessage("Updated OUIDB json file").call();
            git.push().call();
        }
    }

    private File getOuiDBFile() {
        return new File(dataPath, "ouidb.json");
    }

    private Git getGitRepo() throws GitAPIException, IOException {
        Git git;
        if (!dataPath.exists()) {
            dataPath.mkdirs();
        }
        try {
            git = Git.open(dataPath);
            git.pull().call();
        } catch (RepositoryNotFoundException e) {
            git = Git.cloneRepository().setDirectory(dataPath).setURI(repoRemoteUri).call();
        }

        return git;
    }
}
