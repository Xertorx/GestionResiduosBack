package com.co.ucentral.gestionResiduos.back.education;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "education_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uk_progress_user_content_section",
                columnNames = {"user_email", "content_id", "section_id"})
})
public class EducationProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private EducationContent content;

    /**
     * Si sectionId es NULL → el ciudadano marcó el CONTENIDO completo.
     * Si sectionId tiene valor → el ciudadano completó esa SECCIÓN.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = true)
    private EducationSection section;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }
}

