package com.sputnik.ouidb;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.sputnik.ouidb.cli.EnvironmentDefaultProvider;
import com.sputnik.ouidb.cli.Params;
import com.sputnik.ouidb.model.Organization;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Slf4j
@RequiredArgsConstructor
public class Runner {

  private static final String DEFAULT_FILE_PERMISSIONS = "rw-rw-r--";

  private final OUIDBDownloader downloader = new OUIDBDownloader();
  private final OUIDBConverter converter = new OUIDBConverter();
  private final Params params;

  public static void main(String[] args) throws Exception {
    ExitCode exitCode;

    Params params = new Params();
    JCommander jCommander = JCommander.newBuilder()
      .programName("java -jar ouidb-to-json-publisher-jar-with-dependencies.jar")
      .addObject(params)
      .defaultProvider(new EnvironmentDefaultProvider(false))
      .build();

    try {
      jCommander.parse(args);
      if (params.isHelp()) {
        jCommander.usage();
        exitCode = ExitCode.PARAMS_ERROR;
      } else {
        log.info("Running with {}", params);
        exitCode = new Runner(params).run();
      }
    } catch (ParameterException pe) {
      log.error(pe.getMessage());
      pe.getJCommander().usage();
      exitCode = ExitCode.PARAMS_ERROR;
    }

    System.exit(exitCode.getCode());
  }

  private ExitCode run() throws IOException, GitAPIException {
    ExitCode exitCode;
    File ouiDBFile = getOuiDBFile();

    try (Git git = getGitRepo()) {
      Map<String, Organization> parsedDB = downloader.getParsedDB();
      String json = converter.convertToJson(parsedDB);
      FileWriter writer = new FileWriter(ouiDBFile);
      IOUtils.write(json, writer);
      writer.flush();
      Status status = git.status().call();
      if (!status.getModified().isEmpty()) {
        exitCode = ExitCode.THERES_CHANGES;
        File compressedGzFile = compressFileToGz(ouiDBFile);
        File compressedBz2File = compressFileToBz2(ouiDBFile);
        log.info("OUIDB file changed, uploading to git repo.");
        git.add().addFilepattern(ouiDBFile.getName()).call();
        git.add().addFilepattern(compressedGzFile.getName()).call();
        git.add().addFilepattern(compressedBz2File.getName()).call();
        git.commit().setMessage("Updated OUIDB json file").call();
        git.push().call();
      } else {
        exitCode = ExitCode.NO_CHANGES;
        log.info("No changes detected on OUIDB File, nothing to do.");
      }
    }

    log.info("Done :-)");
    return exitCode;
  }

  private File getOuiDBFile() {
    return new File(params.getDataFolder(), "ouidb.json");
  }

  private Git getGitRepo() throws GitAPIException, IOException {
    Git git;
    CredentialsProvider.setDefault(getCredentialsProvider());
    File dataPath = params.getDataFolder();
    if (!dataPath.exists()) {
      log.info("Git repo path does not exists, creating it.");
      dataPath.mkdirs();
    }
    try {
      log.info("Trying to open existing git repo in {}", dataPath);
      git = Git.open(dataPath);
      log.info("Repo opened, pulling latest changes.");
      git.pull().call();
    } catch (RepositoryNotFoundException e) {
      log.info("Repo does not exists, cloning from {}", params.getRemoteRepositoryUri());
      git = Git.cloneRepository()
        .setDirectory(dataPath)
        .setURI(params.getRemoteRepositoryUri())
        .call();
    }

    if (StringUtils.isNotBlank(params.getGitAuthorName())) {
      git.getRepository().getConfig().setString("user", null, "name", params.getGitAuthorName());
    }

    if (StringUtils.isNotBlank(params.getGitAuthorEmail())) {
      git.getRepository().getConfig().setString("user", null, "email", params.getGitAuthorEmail());
    }

    return git;
  }

  private File compressFileToGz(File file) {
    File compressedFile = new File(GzipUtils.getCompressedFileName(file.getAbsolutePath()));
    try (InputStream in = Files.newInputStream(file.toPath());
      GzipCompressorOutputStream out = new GzipCompressorOutputStream(
        new BufferedOutputStream(Files.newOutputStream(compressedFile.toPath())))) {
      setFilePermissions(compressedFile, DEFAULT_FILE_PERMISSIONS);
      IOUtils.copy(in, out);
    } catch (IOException e) {
      log.error("Error compressing file to gz", e);
    }

    return compressedFile;
  }

  private File compressFileToBz2(File file) {
    File compressedFile = new File(BZip2Utils.getCompressedFileName(file.getAbsolutePath()));
    try (InputStream in = Files.newInputStream(file.toPath());
      BZip2CompressorOutputStream out = new BZip2CompressorOutputStream(
        new BufferedOutputStream(Files.newOutputStream(compressedFile.toPath())))) {
      setFilePermissions(compressedFile, DEFAULT_FILE_PERMISSIONS);
      IOUtils.copy(in, out);
    } catch (IOException e) {
      log.error("Error compressing file to bz2", e);
    }

    return compressedFile;
  }

  private CredentialsProvider getCredentialsProvider() {
    CredentialsProvider credentialsProvider;
    if (StringUtils.isNotBlank(params.getRemoteRepositoryUsername()) || StringUtils.isNotBlank(params.getRemoteRepositoryPassword())) {
      log.info("Using username and password for git repo auth");
      credentialsProvider = new UsernamePasswordCredentialsProvider(params.getRemoteRepositoryUsername(),
        StringUtils.trimToEmpty(params.getRemoteRepositoryPassword()));
    } else {
      credentialsProvider = CredentialsProvider.getDefault();
    }

    return credentialsProvider;
  }

  private void setFilePermissions(File file, String permissions) throws IOException {
    Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString(permissions));
  }
}
