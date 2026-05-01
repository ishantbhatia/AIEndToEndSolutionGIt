package com.ai.factory.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses AI-generated output into a map of file path → file content.
 * Expects the format: === FILE: path === followed by a code block.
 */
@Component
public class CodeParser {

    private static final Logger log = LoggerFactory.getLogger(CodeParser.class);

    private static final Pattern CODE_BLOCK = Pattern.compile(
            "```(?:java|xml|yml|yaml|properties|sql|txt)?\\s*\\n(.*?)```",
            Pattern.DOTALL);

    public Map<String, String> parse(String aiOutput) {
        Map<String, String> files = new LinkedHashMap<>();

        String[] sections = aiOutput.split("===\\s*FILE:");
        for (String section : sections) {
            if (section.isBlank()) continue;

            int endOfPath = section.indexOf("===");
            if (endOfPath == -1) continue;

            String filePath = section.substring(0, endOfPath).trim();
            String rest = section.substring(endOfPath + 3);

            Matcher matcher = CODE_BLOCK.matcher(rest);
            if (matcher.find()) {
                files.put(filePath, matcher.group(1).trim());
            }
        }

        log.info("Parsed {} files from AI output", files.size());
        return files;
    }
}
