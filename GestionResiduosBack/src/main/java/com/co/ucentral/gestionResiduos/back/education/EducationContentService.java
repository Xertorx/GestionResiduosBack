package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class EducationContentService {

    private final EducationContentRepository repository;
    private final String UPLOAD_DIR = "uploads/education/";

    @Autowired
    public EducationContentService(EducationContentRepository repository) {
        this.repository = repository;
        // Crea el directorio automáticamente si no existe al arrancar
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public EducationContent saveContent(EducationContentRequestDTO dto, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        // 1. Guardar el archivo físicamente en el PC/Servidor
        byte[] bytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // Le ponemos la fecha en milisegundos al nombre para evitar que dos archivos se llamen igual y se reemplacen
        String newFileName = System.currentTimeMillis() + fileExtension;
        Path path = Paths.get(UPLOAD_DIR + newFileName);
        Files.write(path, bytes);

        // 2. Determinar qué tipo de archivo es (para la base de datos)
        String fileType = "OTRO";
        if (fileExtension.equalsIgnoreCase(".pdf")) fileType = "PDF";
        else if (fileExtension.matches("(?i)\\.(jpg|png|jpeg|webp)")) fileType = "IMAGE";
        else if (fileExtension.matches("(?i)\\.(mp4|avi|mkv)")) fileType = "VIDEO";

        // 3. Guardar todo en PostgreSQL
        EducationContent content = EducationContent.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .fileType(fileType)
                .fileUrl(path.toString()) // Guardamos la ruta donde quedó el archivo
                .build();

        return repository.save(content);
    }

    public List<EducationContent> getAllContents() {
        return repository.findAll();
    }
    public java.util.Optional<EducationContent> getContentById(Long id) {
        return repository.findById(id);
    }

    public void deleteContent(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Contenido no encontrado con id: " + id);
        }
        repository.deleteById(id);
    }
}