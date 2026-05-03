package tn.enicarthage.qartnet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.enicarthage.qartnet.dto.response.ReplyResponse;
import tn.enicarthage.qartnet.dto.response.ThreadDetailResponse;
import tn.enicarthage.qartnet.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {

    @GetMapping("/{publicId}")
    public ApiResponse<ThreadDetailResponse> getThread(...) { ... }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteThread(...) { ... }

    @PostMapping("/{publicId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReplyResponse> createReply(...) { ... }
}
