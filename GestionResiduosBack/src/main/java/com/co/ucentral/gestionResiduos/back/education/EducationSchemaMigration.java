package com.co.ucentral.gestionResiduos.back.education;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ejecuta migraciones SQL manuales al inicio de la aplicación.
 * Corrige columnas legacy (file_url, file_type) en education_contents
 * que quedaron con NOT NULL del esquema anterior.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EducationSchemaMigration {

    private final JdbcTemplate jdbc;

    @PostConstruct
    public void migrate() {
        try {
            jdbc.execute(
                "ALTER TABLE education_contents " +
                "ALTER COLUMN file_url DROP NOT NULL"
            );
            log.info("[EducationMigration] file_url → nullable OK");
        } catch (Exception e) {
            // La columna ya no existe o ya es nullable — ignorar
            log.debug("[EducationMigration] file_url skip: {}", e.getMessage());
        }

        try {
            jdbc.execute(
                "ALTER TABLE education_contents " +
                "ALTER COLUMN file_type DROP NOT NULL"
            );
            log.info("[EducationMigration] file_type → nullable OK");
        } catch (Exception e) {
            log.debug("[EducationMigration] file_type skip: {}", e.getMessage());
        }
    }
}

