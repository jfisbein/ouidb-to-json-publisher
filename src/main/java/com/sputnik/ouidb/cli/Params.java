package com.sputnik.ouidb.cli;

import com.beust.jcommander.Parameter;
import java.io.File;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Params {

  @Parameter(names = "-data", description = "Data folder", required = true, converter = FileConverter.class, order = 1)
  private File dataFolder;

  @Parameter(names = "-repo-url", description = "Remote repository URI", required = true, order = 2)
  private String remoteRepositoryUri;

  @Parameter(names = "-repo-username", description = "Remote repository username", order = 3)
  private String remoteRepositoryUsername;

  @Parameter(names = "-repo-password", description = "Remote repository password", order = 4)
  private String remoteRepositoryPassword;

  @Parameter(names = "-git-author-name", description = "Git author user name", order = 5)
  private String gitAuthorName;

  @Parameter(names = "-git-author-email", description = "Git author email", order = 6)
  private String gitAuthorEmail;

  @Parameter(names = {"--help", "-h"}, help = true, description = "show this help message", order = 0)
  private boolean help;
}
