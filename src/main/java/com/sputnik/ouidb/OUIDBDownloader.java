package com.sputnik.ouidb;

import com.sputnik.ouidb.entity.Bz2DecompressingEntity;
import com.sputnik.ouidb.exception.NoRecordsFoundException;
import com.sputnik.ouidb.model.Address;
import com.sputnik.ouidb.model.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple utility class to parse the latestOUI DB and parse it as case insensitive a Map.<br>
 * Usage:
 * <pre>
 * {@code
 * Map<String,String> ouidb = new OUIDBDownloader().getParsedDB();
 * String vendor = ouidb.get("C47130");
 * System.out.println(vendor); // Fon Technology S.L.
 * }
 * </pre>
 */
@Slf4j
public class OUIDBDownloader {

    private String[] ouiDbUrls = new String[]{"https://linuxnet.ca/ieee/oui.txt.bz2", "https://linuxnet.ca/ieee/oui.txt.gz", "https://linuxnet.ca/ieee/oui.txt"};

    public OUIDBDownloader() {}

    public OUIDBDownloader(String[] ouiDbUrls) {
        this.ouiDbUrls = ouiDbUrls;
    }

    public OUIDBDownloader(String ouiDbUrl) {
        ouiDbUrls = new String[]{ouiDbUrl};
    }

    public Map<String, Organization> getParsedDB() throws IOException {
        return parseDb(download());
    }

    protected Reader download() {
        Reader result = null;
        Iterator<String> url = Arrays.asList(ouiDbUrls).iterator();
        while (result == null && url.hasNext()) {
            String ouiDbUrl = url.next();
            log.info("Downloading OUIs DB from {}", ouiDbUrl);
            try (CloseableHttpClient cachingClient = HttpClientBuilder.create().build()) {
                HttpGet httpget = new HttpGet(ouiDbUrl);
                CloseableHttpResponse response = cachingClient.execute(httpget);
                Header[] contentTypeHeaders = response.getHeaders("Content-Type");
                if (contentTypeHeaders != null && contentTypeHeaders.length > 0 && contentTypeHeaders[0].getValue().equalsIgnoreCase("application/x-gzip")) {
                    result = new StringReader(EntityUtils.toString(new GzipDecompressingEntity(response.getEntity())));
                } else if (contentTypeHeaders != null && contentTypeHeaders.length > 0 && contentTypeHeaders[0].getValue().equalsIgnoreCase("application/x-bzip2")) {
                    result = new StringReader(EntityUtils.toString(new Bz2DecompressingEntity(response.getEntity())));
                } else {
                    result = new StringReader(EntityUtils.toString(response.getEntity()));
                }
            } catch (IOException e) {
                log.warn("Error downloading OUIs from {} - {}: {}", ouiDbUrl, e.getClass().getSimpleName(), e.getMessage());
            }
        }

        if (result == null) {
            throw new NoRecordsFoundException("No records found");
        }

        return result;
    }

    private Map<String, Organization> parseDb(Reader db) throws IOException {
        Map<String, Organization> response = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        try (BufferedReader reader = new BufferedReader(db)) {
            String line;
            int counter = 0;
            Organization organization = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("(base 16)")) {
                    String[] split = StringUtils.splitByWholeSeparator(line, "(base 16)");
                    String prefix = split[0].trim();
                    String organizationName = split[1].trim();
                    counter = 0;
                    organization = new Organization();
                    organization.setName(organizationName);
                    organization.setAddress(new Address());
                    response.put(prefix, organization);
                } else if (counter < 3 && organization != null) {
                    counter = fillAddress(line, counter, organization);
                }
            }
        }

        if (response.isEmpty()) {
            throw new NoRecordsFoundException("No records found");
        }

        return response;
    }

    private int fillAddress(String line, int counter, Organization organization) {
        if (counter == 0) {
            organization.getAddress().setLine1(line);
            counter++;
        } else if (counter == 1) {
            organization.getAddress().setLine2(line);
            counter++;
        } else if (counter == 2) {
            organization.getAddress().setCountryCode(line);
            counter++;
        }
        return counter;
    }
}

