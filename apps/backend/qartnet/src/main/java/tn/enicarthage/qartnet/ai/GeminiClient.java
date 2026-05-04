package tn.enicarthage.qartnet.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tn.enicarthage.qartnet.config.GeminiProperties;
import tn.enicarthage.qartnet.shared.enums.ChatRole;
import tn.enicarthage.qartnet.shared.exception.LlmUnavailableException;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Gemini's generateContent REST endpoint.
 * The history is converted to Gemini's "contents" payload; user → "user", model → "model".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final GeminiProperties props;

    public record HistoryTurn(ChatRole role, String content) {}

    public String generate(List<HistoryTurn> history) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new LlmUnavailableException("Gemini API key is not configured");
        }

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", props.systemPrompt() == null ? "" : props.systemPrompt()))),
                "contents", history.stream()
                        .map(t -> Map.<String, Object>of(
                                "role", t.role() == ChatRole.MODEL ? "model" : "user",
                                "parts", List.of(Map.of("text", t.content()))))
                        .toList(),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024)
        );

        RestClient client = RestClient.builder()
                .baseUrl(props.baseUrl())
                .build();

        try {
            Map<?, ?> response = client.post()
                    .uri("/models/{model}:generateContent?key={key}", props.model(), props.apiKey())
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String err = new String(res.getBody().readAllBytes());
                        log.warn("Gemini error {}: {}", res.getStatusCode(), err);
                        throw new LlmUnavailableException("Gemini call failed: " + res.getStatusCode());
                    })
                    .body(Map.class);

            return extractText(response);
        } catch (ResourceAccessException e) {
            throw new LlmUnavailableException("Gemini call timed out or network unreachable");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> response) {
        if (response == null) throw new LlmUnavailableException("Empty Gemini response");
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty())
            throw new LlmUnavailableException("Gemini returned no candidates");

        Map<String, Object> first = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) first.get("content");
        if (content == null) throw new LlmUnavailableException("Gemini candidate has no content");

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty())
            throw new LlmUnavailableException("Gemini candidate has no parts");

        Object text = parts.get(0).get("text");
        if (text == null) throw new LlmUnavailableException("Gemini part has no text");
        return text.toString();
    }
}
