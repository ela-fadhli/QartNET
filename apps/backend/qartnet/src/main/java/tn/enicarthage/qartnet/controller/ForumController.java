package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.CreateReplyRequest;
import tn.enicarthage.qartnet.dto.request.CreateThreadRequest;
import tn.enicarthage.qartnet.dto.response.*;
import tn.enicarthage.qartnet.service.IForumService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final IForumService forumService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(forumService.getCategories()));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        return ResponseEntity.ok(ApiResponse.success(forumService.getTags()));
    }

    @GetMapping("/threads")
    public ResponseEntity<ApiResponse<Page<ThreadSummaryResponse>>> getThreads(
            @RequestParam(required = false) UUID category,
            @RequestParam(required = false) UUID tag,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(forumService.getThreads(category, tag, pageable)));
    }

    @GetMapping("/threads/{publicId}")
    public ResponseEntity<ApiResponse<ThreadDetailResponse>> getThread(@PathVariable UUID publicId) {
        return ResponseEntity.ok(ApiResponse.success(forumService.getThread(publicId)));
    }

    @PostMapping("/threads")
    public ResponseEntity<ApiResponse<ThreadSummaryResponse>> createThread(
            Authentication auth,
            @Valid @RequestBody CreateThreadRequest request) {
        UUID caller = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(forumService.createThread(caller, request)));
    }

    @PostMapping("/threads/{publicId}/replies")
    public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
            Authentication auth,
            @PathVariable UUID publicId,
            @Valid @RequestBody CreateReplyRequest request) {
        UUID caller = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(forumService.createReply(caller, publicId, request)));
    }

    @DeleteMapping("/replies/{publicId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            Authentication auth,
            @PathVariable UUID publicId) {
        UUID caller = UUID.fromString(auth.getName());
        forumService.deleteReply(caller, publicId);
        return ResponseEntity.ok(ApiResponse.success("Reply deleted", null));
    }
}
