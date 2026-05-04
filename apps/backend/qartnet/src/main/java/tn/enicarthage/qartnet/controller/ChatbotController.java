package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.AskChatbotRequest;
import tn.enicarthage.qartnet.dto.response.AskChatbotResponse;
import tn.enicarthage.qartnet.dto.response.ChatMessageResponse;
import tn.enicarthage.qartnet.service.ChatbotService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ApiResponse<AskChatbotResponse> ask(
            @Valid @RequestBody AskChatbotRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(chatbotService.ask(request, user.getUsername()));
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatMessageResponse>> history(@AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(chatbotService.getHistory(user.getUsername()));
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@AuthenticationPrincipal UserDetails user) {
        chatbotService.resetConversation(user.getUsername());
    }
}
