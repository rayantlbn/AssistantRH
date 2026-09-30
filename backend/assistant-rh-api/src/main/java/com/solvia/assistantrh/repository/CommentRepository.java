package com.solvia.assistantrh.repository;

import com.solvia.assistantrh.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
}
