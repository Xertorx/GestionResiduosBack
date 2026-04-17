package com.co.ucentral.gestionResiduos.back.forum.repository;

import com.co.ucentral.gestionResiduos.back.forum.entity.ForumTopic;
import com.co.ucentral.gestionResiduos.back.forum.entity.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {
    List<ForumTopic> findByEstadoOrderByFechaCreacionDesc(TopicStatus estado);
    List<ForumTopic> findAllByOrderByFechaCreacionDesc();
}

