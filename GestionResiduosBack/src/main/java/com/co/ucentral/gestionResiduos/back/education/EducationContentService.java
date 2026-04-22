package com.co.ucentral.gestionResiduos.back.education;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EducationContentService {

    private final EducationContentRepository repository;
    private final String UPLOAD_DIR = "uploads/education/";

    @Autowired
    public EducationContentService(EducationContentRepository repository) {
        this.repository = repository;
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // ── HU21 mejorada: guardar contenido con MÚLTIPLES archivos ──
    @Transactional
    public EducationContent saveContent(EducationContentRequestDTO dto, MultipartFile[] files) throws IOException {

        List<EducationFile> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new RuntimeException("Nombre de archivo inválido: " + originalFilename);
            }

            // 1. Nombre único para evitar colisiones (timestamp + índice)
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = System.currentTimeMillis() + "_" + savedFiles.size() + fileExtension;
            Path path = Paths.get(UPLOAD_DIR + newFileName);
            Files.write(path, file.getBytes());

            // 2. Determinar tipo
            String fileType = "OTRO";
            if (fileExtension.equalsIgnoreCase(".pdf")) fileType = "PDF";
            else if (fileExtension.matches("(?i)\\.(jpg|png|jpeg|webp)")) fileType = "IMAGE";
            else if (fileExtension.matches("(?i)\\.(mp4|avi|mkv)")) fileType = "VIDEO";

            // 3. Agregar a la lista
            savedFiles.add(EducationFile.builder()
                    .fileUrl("/" + path.toString().replace("\\", "/"))
                    .fileType(fileType)
                    .build());
        }

        if (savedFiles.isEmpty()) {
            throw new RuntimeException("No se recibieron archivos válidos");
        }

        EducationContent content = EducationContent.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .files(savedFiles)
                .build();

        return repository.save(content);
    }

    public List<EducationContent> getAllContents() {
        return repository.findAll();
    }

    public Optional<EducationContent> getContentById(Long id) {
        return repository.findById(id);
    }

    // ── NUEVO: editar solo metadata (no toca archivos) ──
    @Transactional
    public EducationContent updateContent(Long id, EducationContentRequestDTO dto) {
        EducationContent content = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con id: " + id));

        content.setTitle(dto.getTitle());
        content.setDescription(dto.getDescription());
        content.setCategory(dto.getCategory());

        return repository.save(content);
    }

    public void deleteContent(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Contenido no encontrado con id: " + id);
        }
        repository.deleteById(id);
    }
}