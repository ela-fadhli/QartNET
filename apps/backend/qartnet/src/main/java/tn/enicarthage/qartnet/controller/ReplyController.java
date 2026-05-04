package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.UpdateReplyRequest;
import tn.enicarthage.qartnet.dto.response.ReplyResponse;
import tn.enicarthage.qartnet.service.ForumService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ForumService forumService;

    @PatchMapping("/{publicId}")
    public ApiResponse<ReplyResponse> updateReply(
            @PathVariable UUID publicId,
            @Valid @RequestBody UpdateReplyRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.updateReply(publicId, req, user.getUsername()));
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReply(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal UserDetails user) {
        forumService.deleteReply(publicId, user.getUsername());
    }
}
