package com.sputnik.ouidb;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class OUIDBNormalizer {
    public static String normalizeOrganizationName(String orgName) {
        if (orgName != null) {
            orgName = orgName.trim();
            orgName = StringUtils.replaceIgnoreCase(orgName, "Inc.orporated", "Inc.");
        }

        return orgName;
    }

    public static String normalizePrefix(String prefix) {
        if (prefix != null) {
            prefix = prefix.trim();
            prefix = prefix.replace("-", "");
            prefix = prefix.toUpperCase();
        }

        return prefix;
    }

    public static String normalize(String text) {
        String normalizedText = null;
        if (text != null) {
            normalizedText = text.trim();
            normalizedText = StringUtils.removeEnd(normalizedText, ",");
            normalizedText = StringUtils.replaceIgnoreCase(normalizedText, ",.Ltd", ". Ltd");

            // Add space after ',' if missing
            normalizedText = RegExUtils.replaceAll(normalizedText, "\\,(\\w{1})", ", $1");

            // Remove space before ',' if present
            normalizedText = RegExUtils.replaceAll(normalizedText, " \\,", ",");

            normalizedText = normalizedText.trim();
            while (normalizedText.contains("   ")) {
                normalizedText = StringUtils.replace(normalizedText, "   ", "  ");
            }
        }

        return normalizedText;
    }
}
