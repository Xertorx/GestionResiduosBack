package com.co.ucentral.gestionResiduos.back.config;

import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.Neighborhood;
import com.co.ucentral.gestionResiduos.back.Geography.neighborhood.NeighborhoodRepository;
import com.co.ucentral.gestionResiduos.back.reporte.category.ReportCategory;
import com.co.ucentral.gestionResiduos.back.reporte.category.ReportCategoryRepository;
import com.co.ucentral.gestionResiduos.back.role.Role;
import com.co.ucentral.gestionResiduos.back.role.RoleRepository;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final ReportCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NeighborhoodRepository neighborhoodRepository;

    public DataInitializer(RoleRepository roleRepository, ReportCategoryRepository categoryRepository,
                           UserRepository userRepository, PasswordEncoder passwordEncoder,
                           NeighborhoodRepository neighborhoodRepository) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.neighborhoodRepository = neighborhoodRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initReportCategories();
        initAdminUser();
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

    private void initAdminUser() {
        String adminEmail = "admin@gestionresiduos.com";

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            logger.info("No se encontró usuario administrador. Creando usuario admin por defecto...");

            Role adminRole = roleRepository.findByName("ADMINISTRADOR")
                    .orElseThrow(() -> new RuntimeException("No se encontró el rol ADMINISTRADOR. Asegúrese de que los roles se inicialicen primero."));

            Neighborhood neighborhood = neighborhoodRepository.findAll(PageRequest.of(0, 1))
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró ningún barrio en la base de datos. Debe existir al menos un barrio para crear el usuario admin."));

            User admin = new User();
            admin.setDocumentNumber(999999999);
            admin.setNames("Admin");
            admin.setLastName("Sistema");
            admin.setNickName("admin");
            admin.setDocumentType("CC");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setStatus("VERIFICADO");
            admin.setPhoneNumber("0000000000");
            admin.setRole(adminRole);
            admin.setNeighborhoodId(neighborhood);
            admin.setCreatedAt(new Date(System.currentTimeMillis()));
            admin.setUpdatedAt(new Date(System.currentTimeMillis()));

            userRepository.save(admin);
            logger.info("Usuario administrador creado exitosamente. Email: {} | Contraseña: admin123 | Barrio asignado: {}", adminEmail, neighborhood.getName());
        } else {
            logger.info("El usuario administrador ya existe. No se requiere inicialización.");
        }
    }
}

