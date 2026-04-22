package com.co.ucentral.gestionResiduos.back.education;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "education_contents")
public class EducationContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    // ── Lista de archivos adjuntos (antes era un solo fileUrl/fileType) ──
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "education_content_files",
            joinColumns = @JoinColumn(name = "content_id")
    )
    @Builder.Default
    private List<EducationFile> files = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}