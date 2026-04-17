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
@Table(name = "education_contents")
public class EducationContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String fileType; // Ejemplo: "PDF", "IMAGE", "VIDEO"

    @Column(nullable = false)
    private String fileUrl; // La ruta donde guardaremos el archivo en el servidor

    private String category; // Para agrupar por temas de reciclaje

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
