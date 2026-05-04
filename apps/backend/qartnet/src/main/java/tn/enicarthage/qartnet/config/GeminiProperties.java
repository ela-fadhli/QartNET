package tn.enicarthage.qartnet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        String systemPrompt,
        int historyLimit,
        int requestTimeoutSeconds
) {
    public GeminiProperties {
        if (model == null || model.isBlank()) model = "gemini-1.5-flash";
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        if (historyLimit <= 0) historyLimit = 20;
        if (requestTimeoutSeconds <= 0) requestTimeoutSeconds = 15;
    }
}
