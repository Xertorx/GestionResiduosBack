package com.co.ucentral.gestionResiduos.back.forum.service;

import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import com.co.ucentral.gestionResiduos.back.forum.dto.*;
import com.co.ucentral.gestionResiduos.back.forum.entity.*;
import com.co.ucentral.gestionResiduos.back.forum.repository.*;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private final ForumTopicRepository topicRepository;
    private final ForumCommentRepository commentRepository;
    private final ForumReplyRepository replyRepository;
    private final UserRepository userRepository;

    public ForumService(ForumTopicRepository topicRepository,
                        ForumCommentRepository commentRepository,
                        ForumReplyRepository replyRepository,
                        UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.commentRepository = commentRepository;
        this.replyRepository = replyRepository;
        this.userRepository = userRepository;
    }

    // ========== TOPICS ==========

    public List<TopicListDTO> getActiveTopics() {
        return topicRepository.findByEstadoOrderByFechaCreacionDesc(TopicStatus.ACTIVO)
                .stream().map(this::toTopicListDTO).collect(Collectors.toList());
    }

    public List<TopicListDTO> getAllTopics() {
        return topicRepository.findAllByOrderByFechaCreacionDesc()
                .stream().map(this::toTopicListDTO).collect(Collectors.toList());
    }

    public TopicDetailDTO getTopicById(Long id) {
        ForumTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado con ID: " + id));
        return toTopicDetailDTO(topic);
    }

    @Transactional
    public TopicDetailDTO createTopic(String email, CreateTopicRequest request) {
        User user = findUserByEmail(email);
        ForumTopic topic = new ForumTopic();
        topic.setTitulo(request.getTitulo());
        topic.setDescripcion(request.getDescripcion());
        topic.setAutor(user);
        topic.setEstado(TopicStatus.ACTIVO);
        topicRepository.save(topic);
        return toTopicDetailDTO(topic);
    }

    @Transactional
    public void deleteTopic(Long id, String email) {
        ForumTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado con ID: " + id));
        User user = findUserByEmail(email);
        boolean isAdmin = user.getRole() != null && "ADMINISTRADOR".equals(user.getRole().getName());
        boolean isAuthor = topic.getAutor().getDocumentNumber() == user.getDocumentNumber();
        if (!isAdmin && !isAuthor) {
            throw new AccessDeniedException("No tienes permisos para eliminar este tema");
        }
        topicRepository.delete(topic);
    }

    @Transactional
    public TopicListDTO changeTopicStatus(Long id, TopicStatus status) {
        ForumTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado con ID: " + id));
        topic.setEstado(status);
        topicRepository.save(topic);
        return toTopicListDTO(topic);
    }

    // ========== COMMENTS ==========

    public List<CommentDTO> getCommentsByTopic(Long topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new ResourceNotFoundException("Tema no encontrado con ID: " + topicId);
        }
        return commentRepository.findByTemaIdOrderByFechaCreacionAsc(topicId)
                .stream().map(this::toCommentDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO addComment(Long topicId, String email, CreateCommentRequest request) {
        ForumTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado con ID: " + topicId));
        User user = findUserByEmail(email);
        ForumComment comment = new ForumComment();
        comment.setTexto(request.getTexto());
        comment.setUsuario(user);
        comment.setTema(topic);
        commentRepository.save(comment);
        return toCommentDTO(comment);
    }

    // ========== REPLIES ==========

    @Transactional
    public ReplyDTO addReply(Long commentId, String email, CreateReplyRequest request) {
        ForumComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + commentId));
        User user = findUserByEmail(email);
        ForumReply reply = new ForumReply();
        reply.setTexto(request.getTexto());
        reply.setUsuario(user);
        reply.setComentario(comment);
        replyRepository.save(reply);
        return toReplyDTO(reply);
    }

    // ========== MAPPERS ==========

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private String getUserDisplayName(User user) {
        if (user.getNickName() != null && !user.getNickName().isBlank()) {
            return user.getNickName();
        }
        return user.getNames();
    }

    private TopicListDTO toTopicListDTO(ForumTopic topic) {
        return TopicListDTO.builder()
                .id(topic.getId())
                .titulo(topic.getTitulo())
                .descripcion(topic.getDescripcion())
                .autorNombre(getUserDisplayName(topic.getAutor()))
                .fechaCreacion(topic.getFechaCreacion().toString())
                .cantidadComentarios(commentRepository.countByTemaId(topic.getId()))
                .build();
    }

    private TopicDetailDTO toTopicDetailDTO(ForumTopic topic) {
        List<CommentDTO> comments = commentRepository.findByTemaIdOrderByFechaCreacionAsc(topic.getId())
                .stream().map(this::toCommentDTO).collect(Collectors.toList());
        return TopicDetailDTO.builder()
                .id(topic.getId())
                .titulo(topic.getTitulo())
                .descripcion(topic.getDescripcion())
                .autorNombre(getUserDisplayName(topic.getAutor()))
                .fechaCreacion(topic.getFechaCreacion().toString())
                .comentarios(comments)
                .build();
    }

    private CommentDTO toCommentDTO(ForumComment comment) {
        List<ReplyDTO> replies = comment.getRespuestas() != null
                ? comment.getRespuestas().stream().map(this::toReplyDTO).collect(Collectors.toList())
                : List.of();
        return CommentDTO.builder()
                .id(comment.getId())
                .texto(comment.getTexto())
                .usuarioNombre(getUserDisplayName(comment.getUsuario()))
                .fechaCreacion(comment.getFechaCreacion().toString())
                .respuestas(replies)
                .build();
    }

    private ReplyDTO toReplyDTO(ForumReply reply) {
        return ReplyDTO.builder()
                .id(reply.getId())
                .texto(reply.getTexto())
                .usuarioNombre(getUserDisplayName(reply.getUsuario()))
                .fechaCreacion(reply.getFechaCreacion().toString())
                .build();
    }
}

