package com.co.ucentral.gestionResiduos.back.config;

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

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
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
}

