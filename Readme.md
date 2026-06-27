# oui-to-json-publisher
![Java CI with Maven](https://github.com/jfisbein/ouidb-to-json-publisher/workflows/Java%20CI%20with%20Maven/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jfisbein_ouidb-to-json-publisher&metric=alert_status)](https://sonarcloud.io/dashboard?id=jfisbein_ouidb-to-json-publisher)

Simple tool that does the following steps:

+ Clones the specified git repo.
+ Downloads the latest OUI database from `http://standards-oui.ieee.org/oui/oui.txt`.
+ Parses and converts it to JSON.
+ If anything changed compared to what is already in the repo, it commits and pushes:
  + `ouidb.json` — the parsed JSON (plus `ouidb.json.gz` and `ouidb.json.bz2`).
  + `oui.txt` — the raw IEEE file verbatim, so the repo also acts as a **mirror** (plus `oui.txt.gz` and `oui.txt.bz2`).

### Usage

Parameters can be passed as CLI flags or as environment variables (dashes become underscores,
e.g. `-repo-url` → `REPO_URL`).

| Parameter | Env var | Required | Description |
|---|---|---|---|
| `-data` | `DATA` | yes | Local folder for the git working copy |
| `-repo-url` | `REPO_URL` | yes | Remote git repository URI |
| `-repo-username` | `REPO_USERNAME` | no | Git username (on GitHub, the token goes here) |
| `-repo-password` | `REPO_PASSWORD` | no | Git password / token |
| `-git-author-name` | `GIT_AUTHOR_NAME` | no | Commit author name |
| `-git-author-email` | `GIT_AUTHOR_EMAIL` | no | Commit author email |

#### Directly
```
java -jar ouidb-to-json-publisher-jar-with-dependencies.jar \
  -data /path/to/data-folder \
  -repo-url https://github.com/jfisbein/ouidb-json \
  -repo-username {YOUR_GITHUB_USER_TOKEN}
```

#### Using docker
```
docker run \
 -e "REPO_URL=https://github.com/jfisbein/ouidb-json" \
 -e "REPO_USERNAME={YOUR_GITHUB_USER_TOKEN}" \
 jfisbein/ouidb-to-json-publisher
```
note: on GitHub, when using a GitHub user token, you don't have to set the password.

### A note on the IEEE source and where to run this

`standards-oui.ieee.org` sits behind an anti-bot WAF that **drops connections from datacenter /
cloud IP ranges** (you get `ConnectException` / `ClosedChannelException`, not an HTTP error). This
makes the download fail intermittently from CI runners such as GitHub Actions.

To cope with this:

+ The downloader sends browser-like headers and **retries up to 5 times with exponential backoff**
  plus connect/request timeouts (see `OUIDBDownloader`). This handles momentary blips but cannot
  defeat a sustained IP-level block — the connection never reaches the HTTP layer.
+ Therefore the scheduled publisher should run from a host whose IP the WAF does **not** block
  (e.g. a home/residential or otherwise "clean" server), not from GitHub-hosted runners. The
  published `oui.txt` mirror in the output repo also gives downstream consumers a reliable,
  WAF-free source.
