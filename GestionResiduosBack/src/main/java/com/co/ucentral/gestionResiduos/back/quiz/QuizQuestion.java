package com.co.ucentral.gestionResiduos.back.quiz;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String text;

    /** Índice 0-based de la opción correcta dentro de `options`. */
    @Column(name = "correct_index", nullable = false)
    private Integer correctIndex;

    @Column(name = "question_order")
    private Integer order;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "quiz_question_options",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @OrderColumn(name = "option_order")
    @Builder.Default
    private List<QuizOption> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonBackReference
    private Quiz quiz;
}