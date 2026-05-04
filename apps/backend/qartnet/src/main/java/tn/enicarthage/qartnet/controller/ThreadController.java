package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.CreateReplyRequest;
import tn.enicarthage.qartnet.dto.response.ReplyResponse;
import tn.enicarthage.qartnet.dto.response.ThreadDetailResponse;
import tn.enicarthage.qartnet.service.ForumService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final ForumService forumService;

    @GetMapping("/{publicId}")
    public ApiResponse<ThreadDetailResponse> getThread(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.getThread(publicId, user.getUsername()));
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteThread(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal UserDetails user) {
        forumService.deleteThread(publicId, user.getUsername());
    }

    @PostMapping("/{publicId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReplyResponse> createReply(
            @PathVariable UUID publicId,
            @Valid @RequestBody CreateReplyRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.createReply(publicId, req, user.getUsername()));
    }
}
