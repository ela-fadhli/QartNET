package tn.enicarthage.qartnet.service;

import tn.enicarthage.qartnet.dto.request.AskChatbotRequest;
import tn.enicarthage.qartnet.dto.response.AskChatbotResponse;
import tn.enicarthage.qartnet.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatbotService {

    AskChatbotResponse ask(AskChatbotRequest request, String username);

    List<ChatMessageResponse> getHistory(String username);

    void resetConversation(String username);
}
