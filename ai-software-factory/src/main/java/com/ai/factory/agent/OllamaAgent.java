package com.ai.factory.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Calls Ollama via Spring AI auto-configured ChatClient.
 * Model is set in application.yml under spring.ai.ollama.chat.options.model
 */
@Component
public class OllamaAgent {

    private static final Logger log = LoggerFactory.getLogger(OllamaAgent.class);

    private final ChatClient chatClient;

    public OllamaAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        log.info("OllamaAgent initialized with auto-configured ChatClient");
    }

    public String call(String systemPrompt, String userMessage) {
        log.info("Calling Ollama ({} chars input)", userMessage.length());

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        log.info("Ollama returned {} chars", response != null ? response.length() : 0);
        return response;
    }
}
