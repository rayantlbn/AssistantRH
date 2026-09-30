package com.solvia.assistantrh.dto.comment;

import java.time.Instant;

public record CommentResponse(Long id, Long applicationId, String content, Instant createdAt) {
}
