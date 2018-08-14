package com.sputnik.ouidb;

import com.sputnik.ouidb.model.Organization;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class Runner {
    private static OUIDBDownloader downloader = new OUIDBDownloader();
    private static OUIDBConverter converter = new OUIDBConverter();
    private File dataPath;

    public Runner(File dataPath) {
        this.dataPath = dataPath;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Expected one argument");
        } else {
            new Runner(new File(args[0])).run();
        }
    }

    private void run() throws IOException {
        Optional<String> existingDBDigest = getExistingDBDigest();

        Map<String, Organization> parsedDB = downloader.getParsedDB();
        String json = converter.convertToJson(parsedDB);
        String sha1 = getSha1(json.getBytes());
        if (!existingDBDigest.orElse("").equalsIgnoreCase(sha1)) {
            // File has changed
            FileWriter writer = new FileWriter(getOuiDBFile());
            IOUtils.write(json, writer);
            writer.flush();
            //TODO upload to git repo
        }
    }

    private String getSha1(byte[] byteArray) {
        return DigestUtils.sha1Hex(byteArray);
    }

    private Optional<String> getExistingDBDigest() throws IOException {
        String digest = null;
        if (getOuiDBFile().exists()) {
            digest = DigestUtils.sha1Hex(IOUtils.toString(new FileReader(getOuiDBFile())));
        }

        return Optional.ofNullable(digest);
    }

    private File getOuiDBFile() {
        return new File(dataPath, "ouidb.json");
    }
}
