package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import tn.enicarthage.qartnet.dto.request.SendMessageRequest;
import tn.enicarthage.qartnet.service.MessagingService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MessageWsController {

    private final MessagingService messagingService;

    @MessageMapping("/conversation.{publicId}.send")
    public void sendOverWs(
            @DestinationVariable("publicId") UUID conversationPublicId,
            @Valid @Payload SendMessageRequest req,
            @AuthenticationPrincipal UserDetails user) {
        messagingService.sendMessage(conversationPublicId, req, user.getUsername());
    }
}
