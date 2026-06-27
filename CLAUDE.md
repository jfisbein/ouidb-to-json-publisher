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

1. **`OUIDBDownloader`** — fetches `oui.txt` from `http://standards-oui.ieee.org/oui/oui.txt` via Java `HttpClient`, returns a `Reader`. The IEEE host sits behind an anti-bot WAF that intermittently drops connections, so the downloader sends Chrome-like headers and retries up to 5 times with exponential backoff (configurable via `setRetryPolicy(maxAttempts, retryDelay)`) plus connect/request timeouts. It intentionally does **not** send `Accept-Encoding`, because the JDK `HttpClient` would not auto-decompress a gzipped response.
2. **`OUIDBParser`** — parses the raw IEEE text format line-by-line into `Map<String, Organization>` (case-insensitive TreeMap keyed by OUI prefix like `"C47130"`).
3. **`OUIDBNormalizer`** — static utility methods applied during parsing to normalize prefixes, organization names, and address lines (title-case, suffix standardization, etc.).
4. **`OUIDBConverter`** — converts the map to a pretty-printed JSON array sorted by prefix using Gson.
5. **`Runner`** — entry point; wires the above together, writes `ouidb.json` plus `.gz` and `.bz2` compressed variants to a local git repo folder, then commits and pushes only if the file changed.

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
