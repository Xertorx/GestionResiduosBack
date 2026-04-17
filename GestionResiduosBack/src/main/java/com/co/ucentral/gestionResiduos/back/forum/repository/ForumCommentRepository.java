package com.co.ucentral.gestionResiduos.back.forum.repository;

import com.co.ucentral.gestionResiduos.back.forum.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByTemaIdOrderByFechaCreacionAsc(Long temaId);
    int countByTemaId(Long temaId);
}

