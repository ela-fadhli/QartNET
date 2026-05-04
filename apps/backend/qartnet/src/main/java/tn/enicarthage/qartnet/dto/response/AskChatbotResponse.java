package tn.enicarthage.qartnet.dto.response;

public record AskChatbotResponse(
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage
) {}
