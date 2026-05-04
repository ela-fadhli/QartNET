package tn.enicarthage.qartnet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.service.ForumService;

import java.util.UUID;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ForumService forumService;

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReply(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal UserDetails user) {
        forumService.deleteReply(publicId, user.getUsername());
    }
}
