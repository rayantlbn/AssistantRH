package com.solvia.assistantrh.controller;

import com.solvia.assistantrh.dto.comment.CommentRequest;
import com.solvia.assistantrh.dto.comment.CommentResponse;
import com.solvia.assistantrh.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Notes internes sur une candidature. Pas de PUT : un commentaire se corrige en le supprimant puis en le recréant.
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/applications/{applicationId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(@PathVariable Long applicationId, @Valid @RequestBody CommentRequest request) {
        return commentService.create(applicationId, request);
    }

    /** Du plus récent au plus ancien par défaut. */
    @GetMapping("/api/applications/{applicationId}/comments")
    public Page<CommentResponse> findByApplication(@PathVariable Long applicationId,
                                                   @PageableDefault(size = 20) Pageable pageable) {
        return commentService.findByApplication(applicationId, pageable);
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        commentService.delete(id);
    }
}
