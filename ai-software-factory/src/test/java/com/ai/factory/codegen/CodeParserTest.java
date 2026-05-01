package com.ai.factory.codegen;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CodeParserTest {

    private final CodeParser parser = new CodeParser();

    @Test
    void shouldParseTwoFiles() {
        String input = """
                === FILE: src/main/java/App.java ===
                ```java
                public class App {
                    public static void main(String[] args) {}
                }
                ```
                
                === FILE: pom.xml ===
                ```xml
                <project></project>
                ```
                """;

        Map<String, String> files = parser.parse(input);

        assertEquals(2, files.size());
        assertTrue(files.containsKey("src/main/java/App.java"));
        assertTrue(files.containsKey("pom.xml"));
        assertTrue(files.get("src/main/java/App.java").contains("public class App"));
    }

    @Test
    void shouldReturnEmptyMapForBlankInput() {
        Map<String, String> files = parser.parse("");
        assertTrue(files.isEmpty());
    }
}
