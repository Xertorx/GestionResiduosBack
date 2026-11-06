package com.co.ucentral.gestionResiduos.back.achievement;

import com.co.ucentral.gestionResiduos.back.quiz.QuizAttemptRepository;
import com.co.ucentral.gestionResiduos.back.reporte.ReportRepository;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    /**
     * Evalúa y otorga logros después de completar un quiz.
     * Retorna la lista de logros NUEVOS obtenidos en esta acción.
     */
    @Transactional
    public List<AchievementDTO> checkAfterQuiz(User user) {
        List<AchievementDTO> newlyUnlocked = new ArrayList<>();
        long quizCount = quizAttemptRepository.countDistinctQuizzesByUser(user);

        // Primer quiz completado
        newlyUnlocked.addAll(tryUnlock(user, "FIRST_QUIZ", quizCount >= 1));
        // 3 quizzes completados
        newlyUnlocked.addAll(tryUnlock(user, "QUIZZES_3", quizCount >= 3));
        // 5 quizzes completados
        newlyUnlocked.addAll(tryUnlock(user, "QUIZZES_5", quizCount >= 5));
        // 10 quizzes completados
        newlyUnlocked.addAll(tryUnlock(user, "QUIZZES_10", quizCount >= 10));

        // Verificar logros por puntos
        checkPointsAchievements(user, newlyUnlocked);

        return newlyUnlocked;
    }

    /**
     * Evalúa y otorga logros después de crear un reporte.
     * Retorna la lista de logros NUEVOS obtenidos en esta acción.
     */
    @Transactional
    public List<AchievementDTO> checkAfterReport(User user) {
        List<AchievementDTO> newlyUnlocked = new ArrayList<>();
        long reportCount = reportRepository.findByUserDocumentNumber(user.getDocumentNumber()).size();

        // Primer reporte
        newlyUnlocked.addAll(tryUnlock(user, "FIRST_REPORT", reportCount >= 1));
        // 3 reportes
        newlyUnlocked.addAll(tryUnlock(user, "REPORTS_3", reportCount >= 3));
        // 5 reportes
        newlyUnlocked.addAll(tryUnlock(user, "REPORTS_5", reportCount >= 5));
        // 10 reportes
        newlyUnlocked.addAll(tryUnlock(user, "REPORTS_10", reportCount >= 10));

        return newlyUnlocked;
    }

    /**
     * Verifica logros por puntos acumulados.
     */
    private void checkPointsAchievements(User user, List<AchievementDTO> newlyUnlocked) {
        int points = user.getPoints() == null ? 0 : user.getPoints();
        newlyUnlocked.addAll(tryUnlock(user, "POINTS_50", points >= 50));
        newlyUnlocked.addAll(tryUnlock(user, "POINTS_100", points >= 100));
        newlyUnlocked.addAll(tryUnlock(user, "POINTS_500", points >= 500));
    }

    /**
     * Intenta desbloquear un logro. Si la condición se cumple y el usuario
     * aún no lo tiene, lo otorga y suma los puntos del logro al usuario.
     */
    private List<AchievementDTO> tryUnlock(User user, String condicion, boolean conditionMet) {
        List<AchievementDTO> result = new ArrayList<>();
        if (!conditionMet) return result;

        achievementRepository.findByCondicion(condicion).ifPresent(achievement -> {
            if (!userAchievementRepository.existsByUserAndAchievement(user, achievement)) {
                // Otorgar logro
                UserAchievement ua = UserAchievement.builder()
                        .user(user)
                        .achievement(achievement)
                        .build();
                userAchievementRepository.save(ua);

                // Sumar puntos del logro al usuario
                int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
                user.setPoints(currentPoints + achievement.getPuntosOtorgados());
                userRepository.save(user);

                log.info("Logro '{}' otorgado a usuario {} (+{} pts)",
                        achievement.getNombre(), user.getEmail(), achievement.getPuntosOtorgados());

                result.add(toDTO(achievement, true, ua.getFechaObtenido().toString()));
            }
        });

        return result;
    }

    /**
     * Obtener todos los logros con el estado del usuario (unlocked/locked).
     */
    public List<AchievementDTO> getAllForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserOrderByFechaObtenidoDesc(user);

        return allAchievements.stream().map(a -> {
            UserAchievement ua = userAchievements.stream()
                    .filter(u -> u.getAchievement().getId().equals(a.getId()))
                    .findFirst().orElse(null);

            return AchievementDTO.builder()
                    .id(a.getId())
                    .nombre(a.getNombre())
                    .descripcion(a.getDescripcion())
                    .condicion(a.getCondicion())
                    .puntosOtorgados(a.getPuntosOtorgados())
                    .icono(a.getIcono())
                    .unlocked(ua != null)
                    .fechaObtenido(ua != null ? ua.getFechaObtenido().toString() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Obtener solo los logros desbloqueados del usuario.
     */
    public List<AchievementDTO> getUnlockedForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return userAchievementRepository.findByUserOrderByFechaObtenidoDesc(user).stream()
                .map(ua -> toDTO(ua.getAchievement(), true, ua.getFechaObtenido().toString()))
                .collect(Collectors.toList());
    }

    private AchievementDTO toDTO(Achievement a, boolean unlocked, String fecha) {
        return AchievementDTO.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .descripcion(a.getDescripcion())
                .condicion(a.getCondicion())
                .puntosOtorgados(a.getPuntosOtorgados())
                .icono(a.getIcono())
                .unlocked(unlocked)
                .fechaObtenido(fecha)
                .build();
    }
}