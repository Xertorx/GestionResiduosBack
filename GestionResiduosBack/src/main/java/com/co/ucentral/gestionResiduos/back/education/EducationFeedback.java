package com.co.ucentral.gestionResiduos.back.education;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "education_feedbacks", uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_user_feedback", columnNames = {"content_id", "user_email"})
})
public class EducationFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private EducationContent content;

    @Column(name = "useful", nullable = false)
    private boolean useful;

    @Column(name = "user_email")
    private String userEmail; // opcional

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

