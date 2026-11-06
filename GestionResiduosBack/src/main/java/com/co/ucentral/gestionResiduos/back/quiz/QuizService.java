package com.co.ucentral.gestionResiduos.back.quiz;

import com.co.ucentral.gestionResiduos.back.education.EducationContentRepository;
import com.co.ucentral.gestionResiduos.back.quiz.dto.*;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.co.ucentral.gestionResiduos.back.achievement.AchievementDTO;
import com.co.ucentral.gestionResiduos.back.achievement.AchievementService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final EducationContentRepository contentRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;


    public QuizService(QuizRepository quizRepository,
                       QuizAttemptRepository attemptRepository,
                       EducationContentRepository contentRepository,
                       UserRepository userRepository,
                       AchievementService achievementService) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    // ───────────────────────── CREATE ─────────────────────────
    @Transactional
    public QuizAdminResponseDTO createForContent(Long contentId, QuizRequestDTO dto) {
        if (!contentRepository.existsById(contentId)) {
            throw new RuntimeException("Contenido educativo no encontrado: " + contentId);
        }
        if (quizRepository.existsByContentId(contentId)) {
            throw new RuntimeException("Ya existe un quiz para este contenido. Edítalo o elimínalo.");
        }
        validateDto(dto);

        Quiz quiz = Quiz.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .contentId(contentId)
                .pointsPerQuestion(dto.getPointsPerQuestion() == null ? 10 : dto.getPointsPerQuestion())
                .build();

        List<QuizQuestion> questions = buildQuestions(dto, quiz);
        quiz.setQuestions(questions);

        Quiz saved = quizRepository.save(quiz);
        return toAdminDto(saved);
    }

    // ───────────────────────── UPDATE ─────────────────────────
    @Transactional
    public QuizAdminResponseDTO update(Long quizId, QuizRequestDTO dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz no encontrado: " + quizId));
        validateDto(dto);

        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        if (dto.getPointsPerQuestion() != null) {
            quiz.setPointsPerQuestion(dto.getPointsPerQuestion());
        }

        // Reemplazar preguntas (orphanRemoval limpia las viejas)
        quiz.getQuestions().clear();
        quiz.getQuestions().addAll(buildQuestions(dto, quiz));

        Quiz saved = quizRepository.save(quiz);
        return toAdminDto(saved);
    }

    // ───────────────────────── DELETE ─────────────────────────
    @Transactional
    public void delete(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz no encontrado: " + quizId));

        // Borrar primero los intentos asociados (FK constraint)
        List<QuizAttempt> attempts = attemptRepository.findAll().stream()
                .filter(a -> a.getQuiz().getId().equals(quizId))
                .toList();
        attemptRepository.deleteAll(attempts);

        // Ahora sí se puede borrar el quiz
        quizRepository.delete(quiz);
    }

    // ───────────────────────── GET ─────────────────────────
    public QuizAdminResponseDTO getAdminByContent(Long contentId) {
        Quiz quiz = quizRepository.findByContentId(contentId)
                .orElseThrow(() -> new RuntimeException("No hay quiz para este contenido."));
        return toAdminDto(quiz);
    }

    public QuizPlayResponseDTO getPlayByContent(Long contentId) {
        Quiz quiz = quizRepository.findByContentId(contentId)
                .orElseThrow(() -> new RuntimeException("No hay quiz para este contenido."));
        return toPlayDto(quiz);
    }

    public boolean existsForContent(Long contentId) {
        return quizRepository.existsByContentId(contentId);
    }

    // ───────────────────────── SUBMIT ─────────────────────────
    @Transactional
    public QuizResultDTO submitAttempt(Long quizId, String userEmail, SubmitAttemptDTO dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz no encontrado: " + quizId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Mapa rápido id -> respuesta seleccionada
        Map<Long, Integer> answersMap = dto.getAnswers().stream()
                .filter(a -> a.getQuestionId() != null && a.getSelectedIndex() != null)
                .collect(Collectors.toMap(
                        SubmitAttemptDTO.Answer::getQuestionId,
                        SubmitAttemptDTO.Answer::getSelectedIndex,
                        (a, b) -> a
                ));

        int correct = 0;
        List<QuizResultDTO.QuestionResult> perQuestion = new ArrayList<>();

        for (QuizQuestion q : quiz.getQuestions()) {
            Integer selected = answersMap.get(q.getId());
            boolean wasCorrect = selected != null && selected.equals(q.getCorrectIndex());
            if (wasCorrect) correct++;

            perQuestion.add(QuizResultDTO.QuestionResult.builder()
                    .questionId(q.getId())
                    .selectedIndex(selected)
                    .correctIndex(q.getCorrectIndex())
                    .wasCorrect(wasCorrect)
                    .build());
        }

        int total = quiz.getQuestions().size();

        // Reglas de puntaje: solo el PRIMER intento otorga puntos
        boolean firstAttempt = !attemptRepository.existsByUserAndQuiz(user, quiz);
        int pointsEarned = firstAttempt ? correct * quiz.getPointsPerQuestion() : 0;

        // Persistir intento
        attemptRepository.save(QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .correctAnswers(correct)
                .totalQuestions(total)
                .pointsEarned(pointsEarned)
                .build());

        // Sumar puntos al perfil del usuario (HU22 criterio #3)
        if (pointsEarned > 0) {
            int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
            user.setPoints(currentPoints + pointsEarned);
            userRepository.save(user);
        }

        // ── Sistema de Logros: evaluar logros después del quiz ──
        java.util.List<AchievementDTO> newAchievements = achievementService.checkAfterQuiz(user);

        return QuizResultDTO.builder()
                .correctAnswers(correct)
                .totalQuestions(total)
                .pointsEarned(pointsEarned)
                .userTotalPoints(user.getPoints() == null ? 0 : user.getPoints())
                .firstAttempt(firstAttempt)
                .perQuestion(perQuestion)
                .newAchievements(newAchievements)
                .build();
    }
    // ───────────────────── STATS DEL USUARIO ─────────────────────
    public java.util.Map<String, Object> getUserStats(String userEmail) {
        com.co.ucentral.gestionResiduos.back.user.User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        long quizzesAnswered = attemptRepository.countDistinctQuizzesByUser(user);
        long totalContents = contentRepository.count();
        int points = user.getPoints() == null ? 0 : user.getPoints();

        // Progreso = % de quizzes respondidos sobre total de contenidos
        int progress = 0;
        if (totalContents > 0) {
            progress = (int) Math.round((quizzesAnswered * 100.0) / totalContents);
            if (progress > 100) progress = 100;
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalContents", totalContents);
        result.put("quizzesAnswered", quizzesAnswered);
        result.put("points", points);
        result.put("progress", progress);
        return result;
    }
    // ───────────────────────── HELPERS ─────────────────────────
    private void validateDto(QuizRequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new RuntimeException("El título del quiz es obligatorio.");
        }
        if (dto.getQuestions() == null || dto.getQuestions().isEmpty()) {
            throw new RuntimeException("El quiz debe tener al menos una pregunta.");
        }
        for (int i = 0; i < dto.getQuestions().size(); i++) {
            QuestionRequestDTO q = dto.getQuestions().get(i);
            if (q.getText() == null || q.getText().isBlank()) {
                throw new RuntimeException("La pregunta #" + (i + 1) + " no tiene enunciado.");
            }
            if (q.getOptions() == null || q.getOptions().size() < 2) {
                throw new RuntimeException("La pregunta #" + (i + 1) + " debe tener al menos 2 opciones.");
            }
            if (q.getCorrectIndex() == null
                    || q.getCorrectIndex() < 0
                    || q.getCorrectIndex() >= q.getOptions().size()) {
                throw new RuntimeException("La pregunta #" + (i + 1) + " tiene un índice de respuesta inválido.");
            }
        }
    }

    private List<QuizQuestion> buildQuestions(QuizRequestDTO dto, Quiz parent) {
        List<QuizQuestion> result = new ArrayList<>();
        int order = 0;
        for (QuestionRequestDTO qReq : dto.getQuestions()) {
            List<QuizOption> opts = qReq.getOptions().stream()
                    .map(text -> QuizOption.builder().text(text).build())
                    .collect(Collectors.toList());

            QuizQuestion q = QuizQuestion.builder()
                    .text(qReq.getText())
                    .correctIndex(qReq.getCorrectIndex())
                    .order(order++)
                    .options(opts)
                    .quiz(parent)
                    .build();
            result.add(q);
        }
        return result;
    }

    private QuizAdminResponseDTO toAdminDto(Quiz quiz) {
        List<QuizAdminResponseDTO.QuestionAdmin> qs = quiz.getQuestions().stream()
                .map(q -> QuizAdminResponseDTO.QuestionAdmin.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .correctIndex(q.getCorrectIndex())
                        .options(q.getOptions().stream()
                                .map(QuizOption::getText)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return QuizAdminResponseDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .contentId(quiz.getContentId())
                .pointsPerQuestion(quiz.getPointsPerQuestion())
                .createdAt(quiz.getCreatedAt())
                .questions(qs)
                .build();
    }

    private QuizPlayResponseDTO toPlayDto(Quiz quiz) {
        List<QuizPlayResponseDTO.QuestionPlay> qs = quiz.getQuestions().stream()
                .map(q -> QuizPlayResponseDTO.QuestionPlay.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .options(q.getOptions().stream()
                                .map(QuizOption::getText)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return QuizPlayResponseDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .contentId(quiz.getContentId())
                .pointsPerQuestion(quiz.getPointsPerQuestion())
                .questions(qs)
                .build();
    }
}