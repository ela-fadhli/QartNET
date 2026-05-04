package tn.enicarthage.qartnet.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.request.*;
import tn.enicarthage.qartnet.dto.response.*;
import tn.enicarthage.qartnet.service.ForumService;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/forums")
@Validated
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ForumSummaryResponse> createForum(
            @Valid @RequestBody CreateForumRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.createForum(req, user.getUsername()));
    }

    @GetMapping
    public ApiResponse<Page<ForumSummaryResponse>> getForums(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.success(forumService.getForums(q, page, size));
    }

    @GetMapping("/{slug}")
    public ApiResponse<ForumDetailResponse> getForum(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.getForum(slug, user.getUsername()));
    }

    @PatchMapping("/{slug}")
    public ApiResponse<ForumSummaryResponse> updateForum(
            @PathVariable String slug,
            @Valid @RequestBody UpdateForumRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.updateForum(slug, req, user.getUsername()));
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForum(@PathVariable String slug, @AuthenticationPrincipal UserDetails user) {
        forumService.deleteForum(slug, user.getUsername());
    }

    // Categories
    @GetMapping("/{slug}/categories")
    public ApiResponse<List<ForumCategoryResponse>> getCategories(@PathVariable String slug) {
        return ApiResponse.success(forumService.getCategories(slug));
    }

    @PostMapping("/{slug}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ForumCategoryResponse> createCategory(
            @PathVariable String slug,
            @Valid @RequestBody CreateForumCategoryRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.createCategory(slug, req, user.getUsername()));
    }

    @DeleteMapping("/{slug}/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @PathVariable String slug,
            @PathVariable UUID catId,
            @AuthenticationPrincipal UserDetails user) {
        forumService.deleteCategory(slug, catId, user.getUsername());
    }

    // Members
    @GetMapping("/{slug}/members")
    public ApiResponse<List<ForumMemberResponse>> getMembers(@PathVariable String slug) {
        return ApiResponse.success(forumService.getMembers(slug));
    }

    @PostMapping("/{slug}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ForumMemberResponse> addMember(
            @PathVariable String slug,
            @Valid @RequestBody AddForumMemberRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.addMember(slug, req, user.getUsername()));
    }

    @DeleteMapping("/{slug}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable String slug,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails user) {
        forumService.removeMember(slug, userId, user.getUsername());
    }

    // Threads
    @GetMapping("/{slug}/threads")
    public ApiResponse<Page<ThreadSummaryResponse>> getThreads(
            @PathVariable String slug,
            @RequestParam(required = false) UUID category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.success(forumService.getThreads(slug, category, page, size));
    }

    @PostMapping("/{slug}/threads")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ThreadDetailResponse> createThread(
            @PathVariable String slug,
            @Valid @RequestBody CreateThreadRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.success(forumService.createThread(slug, req, user.getUsername()));
    }
}
