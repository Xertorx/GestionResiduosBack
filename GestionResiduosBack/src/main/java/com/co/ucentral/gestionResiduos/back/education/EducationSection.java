package com.co.ucentral.gestionResiduos.back.education;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "education_sections")
public class EducationSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private EducationContent content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "education_section_files", joinColumns = @JoinColumn(name = "section_id"))
    @Builder.Default
    private List<EducationFile> files = new ArrayList<>();
}

