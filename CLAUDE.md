# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build (produces fat jar in target/)
mvn package

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=OUIDBParserTest

# Run a single test method
mvn test -Dtest=OUIDBParserTest#parseDb

# Run the application
java -jar target/ouidb-to-json-publisher-jar-with-dependencies.jar \
  -data /path/to/data-folder \
  -repo-url https://github.com/user/repo.git \
  -repo-username user \
  -repo-password token
```

## Architecture

This tool downloads the IEEE OUI database, converts it to JSON, and publishes it to a git repository. The pipeline is:

1. **`OUIDBDownloader`** — fetches `oui.txt` from `http://standards-oui.ieee.org/oui/oui.txt` via Java `HttpClient`. `downloadAsString()` returns the raw text (with retries), `parse(String)` parses it, and `getParsedDB()`/`download()` build on these. The IEEE host sits behind an anti-bot WAF that intermittently drops connections, so the downloader sends Chrome-like headers and retries up to 5 times with exponential backoff (configurable via `setRetryPolicy(maxAttempts, retryDelay)`) plus connect/request timeouts. It intentionally does **not** send `Accept-Encoding`, because the JDK `HttpClient` would not auto-decompress a gzipped response.
2. **`OUIDBParser`** — parses the raw IEEE text format line-by-line into `Map<String, Organization>` (case-insensitive TreeMap keyed by OUI prefix like `"C47130"`).
3. **`OUIDBNormalizer`** — static utility methods applied during parsing to normalize prefixes, organization names, and address lines (title-case, suffix standardization, etc.).
4. **`OUIDBConverter`** — converts the map to a pretty-printed JSON array sorted by prefix using Gson.
5. **`Runner`** — entry point; downloads the raw text once, writes both `oui.txt` (raw mirror) and `ouidb.json` plus their `.gz`/`.bz2` variants to a local git repo folder, then commits and pushes only if something changed. Change detection stages the source files and checks `getAdded()` + `getChanged()` so first-time (untracked) files are detected too.

### CLI Parameters

Parsed by JCommander (`Params.java`). All parameters can also be supplied as environment variables with dashes replaced by underscores (e.g., `-repo-url` → `REPO_URL`), handled by `EnvironmentDefaultProvider`.

Required: `-data` (local folder), `-repo-url` (remote git URI).
Optional: `-repo-username`, `-repo-password`, `-git-author-name`, `-git-author-email`.

### Exit Codes

Defined in `ExitCode.java`: `NO_CHANGES` (no diff detected), `THERES_CHANGES` (committed and pushed), `PARAMS_ERROR` (bad CLI args).

### Key Libraries

- **Lombok** — `@Slf4j`, `@Getter`, `@RequiredArgsConstructor`, `@UtilityClass`
- **JGit** — clones/pulls/commits/pushes the output git repository
- **Apache Commons** (lang3, text, io, compress) — string utilities, IO helpers, gzip/bzip2 compression
- **Gson** — JSON serialization
- **JCommander** — CLI argument parsing
- **JUnit 5 + AssertJ + JavaFaker** — testing

## Deployment / Operations

The output repo is https://github.com/jfisbein/ouidb-json. Beyond `ouidb.json`, the publisher also commits the raw `oui.txt` (+ `.gz`/`.bz2`) so that repo doubles as a **mirror** of the IEEE file — useful because there is no reliable third-party `oui.txt` mirror.

**Why it does not run on GitHub Actions:** `standards-oui.ieee.org` is behind a WAF that blocks datacenter/cloud IP ranges at the TCP level (`ConnectException`/`ClosedChannelException`). GitHub-hosted runners only succeeded ~50% of the time, and the retry/backoff logic cannot defeat a sustained IP-level block (the connection never reaches the HTTP layer). The nightly GitHub Action in `ouidb-json` is therefore **disabled**.

**Where it runs instead:** the nightly job runs on the maintainer's Oracle Cloud host `ampere-03` (a "clean" IP the WAF does not block), under `~/ouidb-publisher/` (docker-compose using `:edge`, scheduled via cron at 06:00 UTC). The host is ARM64 and the published image is amd64-only, so it runs under qemu/binfmt emulation (`platform: linux/amd64`).

**Docker image:** built and pushed by `.github/workflows/maven.yml` on push to `master` (tag `:edge`) and on git tags (versioned tags + `:latest`). It is amd64-only (no `platforms:` set in the build).
