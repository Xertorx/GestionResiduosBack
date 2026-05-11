package com.co.ucentral.gestionResiduos.back.forum;

import com.co.ucentral.gestionResiduos.back.forum.dto.*;
import com.co.ucentral.gestionResiduos.back.forum.service.ForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    // ========== TOPICS ==========

    /** Obtener todos los temas activos (público) */
    @GetMapping("/topics")
    public ResponseEntity<List<TopicListDTO>> getActiveTopics() {
        return ResponseEntity.ok(forumService.getActiveTopics());
    }

    /** Obtener todos los temas - Admin */
    @GetMapping("/topics/all")
    public ResponseEntity<List<TopicListDTO>> getAllTopics() {
        return ResponseEntity.ok(forumService.getAllTopics());
    }

    /** Obtener detalle de un tema por ID */
    @GetMapping("/topics/{id}")
    public ResponseEntity<TopicDetailDTO> getTopicById(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getTopicById(id));
    }

    /** Crear un nuevo tema */
    @PostMapping("/topics")
    public ResponseEntity<TopicDetailDTO> createTopic(@Valid @RequestBody CreateTopicRequest request) {
        String email = getAuthenticatedEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(forumService.createTopic(email, request));
    }

    /** Eliminar un tema (autor o admin) */
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        String email = getAuthenticatedEmail();
        forumService.deleteTopic(id, email);
        return ResponseEntity.noContent().build();
    }

    /** Cambiar estado de un tema - Admin */
    @PatchMapping("/topics/{id}/status")
    public ResponseEntity<TopicListDTO> changeTopicStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(forumService.changeTopicStatus(id, request.getEstado()));
    }

    // ========== COMMENTS ==========

    /** Obtener comentarios de un tema */
    @GetMapping("/topics/{topicId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByTopic(@PathVariable Long topicId) {
        return ResponseEntity.ok(forumService.getCommentsByTopic(topicId));
    }

    /** Agregar comentario a un tema */
    @PostMapping("/topics/{topicId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long topicId,
            @Valid @RequestBody CreateCommentRequest request) {
        String email = getAuthenticatedEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(forumService.addComment(topicId, email, request));
    }

    // ========== REPLIES ==========

    /** Agregar respuesta a un comentario */
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ReplyDTO> addReply(
            @PathVariable Long commentId,
            @Valid @RequestBody CreateReplyRequest request) {
        String email = getAuthenticatedEmail();
        return ResponseEntity.status(HttpStatus.CREATED).body(forumService.addReply(commentId, email, request));
    }

    // ========== UTILS ==========

    private String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}

