package com.co.ucentral.gestionResiduos.back.config;

import com.co.ucentral.gestionResiduos.back.reporte.category.ReportCategory;
import com.co.ucentral.gestionResiduos.back.reporte.category.ReportCategoryRepository;
import com.co.ucentral.gestionResiduos.back.role.Role;
import com.co.ucentral.gestionResiduos.back.role.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final ReportCategoryRepository categoryRepository;

    public DataInitializer(RoleRepository roleRepository, ReportCategoryRepository categoryRepository) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initReportCategories();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            logger.info("No se encontraron roles en la base de datos. Creando roles por defecto...");

            Role admin = new Role();
            admin.setName("ADMINISTRADOR");
            admin.setDescription("Rol de administrador del sistema");
            roleRepository.save(admin);

            Role ciudadano = new Role();
            ciudadano.setName("CIUDADANO");
            ciudadano.setDescription("Rol de ciudadano registrado");
            roleRepository.save(ciudadano);

            logger.info("Roles creados exitosamente: ADMINISTRADOR, CIUDADANO");
        } else {
            logger.info("La tabla de roles ya contiene registros. No se requiere inicialización.");
        }
    }

    private void initReportCategories() {
        if (categoryRepository.count() == 0) {
            logger.info("No se encontraron categorías de reportes. Creando categorías por defecto...");

            createCategory("Acumulación de basura", "Puntos donde se acumula basura de forma no controlada");
            createCategory("Residuos peligrosos", "Presencia de residuos peligrosos o tóxicos en vía pública");
            createCategory("Escombros y RCD", "Residuos de construcción y demolición abandonados");
            createCategory("Incumplimiento de recolección", "La empresa de aseo no realizó la recolección programada");
            createCategory("Contenedor dañado", "Contenedor o punto ecológico en mal estado");
            createCategory("Vertimiento ilegal", "Vertimiento de residuos líquidos o sólidos en fuentes hídricas");
            createCategory("Otro", "Otro tipo de reporte no clasificado");

            logger.info("Categorías de reportes creadas exitosamente.");
        } else {
            logger.info("La tabla de categorías de reportes ya contiene registros. No se requiere inicialización.");
        }
    }

    private void createCategory(String name, String description) {
        ReportCategory category = new ReportCategory();
        category.setName(name);
        category.setDescription(description);
        category.setStatus("ACTIVO");
        categoryRepository.save(category);
    }
}

