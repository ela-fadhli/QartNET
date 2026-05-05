package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.SendMessageRequest;
import tn.enicarthage.qartnet.dto.response.ConversationSummaryResponse;
import tn.enicarthage.qartnet.dto.response.MessageResponse;
import tn.enicarthage.qartnet.service.MessagingService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messaging")
@Validated
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;

    @GetMapping("/conversations")
    public ApiResponse<List<ConversationSummaryResponse>> listConversations(
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(messagingService.listConversations(user.getUsername()));
    }

    @PostMapping("/conversations/direct/{userPublicId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConversationSummaryResponse> openDirectConversation(
            @PathVariable UUID userPublicId,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(
                messagingService.getOrCreateDirectConversation(userPublicId, user.getUsername()));
    }

    @GetMapping("/conversations/{publicId}/messages")
    public ApiResponse<List<MessageResponse>> listMessages(
            @PathVariable UUID publicId,
            @RequestParam(required = false) String before,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(
                messagingService.listMessages(publicId, before, limit, user.getUsername()));
    }

    @PostMapping("/conversations/{publicId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MessageResponse> sendMessage(
            @PathVariable UUID publicId,
            @Valid @RequestBody SendMessageRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(
                messagingService.sendMessage(publicId, req, user.getUsername()));
    }

    @PostMapping("/conversations/{publicId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal UserDetails user) {
        messagingService.markRead(publicId, user.getUsername());
    }
}
