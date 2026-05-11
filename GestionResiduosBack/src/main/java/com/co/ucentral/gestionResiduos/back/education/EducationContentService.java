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
import java.util.stream.Collectors;

@Service
public class EducationContentService {

    private final EducationContentRepository repository;
    private final EducationSectionRepository sectionRepository;
    private final String UPLOAD_DIR = "uploads/education/";

    @Autowired
    public EducationContentService(EducationContentRepository repository,
                                   EducationSectionRepository sectionRepository) {
        this.repository = repository;
        this.sectionRepository = sectionRepository;
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // ──────────────────────────── MAPPERS ────────────────────────────

    private EducationFileDTO toFileDTO(EducationFile f) {
        return EducationFileDTO.builder()
                .fileUrl(f.getFileUrl())
                .fileType(f.getFileType())
                .build();
    }

    private EducationSectionResponseDTO toSectionDTO(EducationSection s) {
        List<EducationFileDTO> files = s.getFiles() == null ? List.of() :
                s.getFiles().stream().map(this::toFileDTO).collect(Collectors.toList());
        return EducationSectionResponseDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .description(s.getDescription())
                .files(files)
                .build();
    }

    public EducationContentResponseDTO toContentDTO(EducationContent c) {
        List<EducationFileDTO> files = c.getFiles() == null ? List.of() :
                c.getFiles().stream().map(this::toFileDTO).collect(Collectors.toList());
        List<EducationSectionResponseDTO> sections = c.getSections() == null ? List.of() :
                c.getSections().stream().map(this::toSectionDTO).collect(Collectors.toList());
        return EducationContentResponseDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .createdAt(c.getCreatedAt())
                .files(files)
                .sections(sections)
                .build();
    }

    // ──────────────────────────── HU21: guardar contenido con MÚLTIPLES archivos ────────────────────────────

    @Transactional
    public EducationContentResponseDTO saveContent(EducationContentRequestDTO dto, MultipartFile[] files) throws IOException {

        List<EducationFile> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new RuntimeException("Nombre de archivo inválido: " + originalFilename);
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = System.currentTimeMillis() + "_" + savedFiles.size() + fileExtension;
            Path path = Paths.get(UPLOAD_DIR + newFileName);
            Files.write(path, file.getBytes());

            String fileType = "OTRO";
            if (fileExtension.equalsIgnoreCase(".pdf")) fileType = "PDF";
            else if (fileExtension.matches("(?i)\\.(jpg|png|jpeg|webp)")) fileType = "IMAGE";
            else if (fileExtension.matches("(?i)\\.(mp4|avi|mkv)")) fileType = "VIDEO";

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

        return toContentDTO(repository.save(content));
    }

    public List<EducationContentResponseDTO> getAllContents() {
        return repository.findAll().stream()
                .map(c -> {
                    // Cargar secciones para cada contenido
                    List<EducationSection> sections = sectionRepository.findByContentId(c.getId());
                    c.setSections(sections);
                    return toContentDTO(c);
                })
                .collect(Collectors.toList());
    }

    public Optional<EducationContentResponseDTO> getContentById(Long id) {
        return repository.findById(id).map(content -> {
            List<EducationSection> sections = sectionRepository.findByContentId(id);
            content.setSections(sections);
            return toContentDTO(content);
        });
    }

    public List<EducationSectionResponseDTO> getSectionsByContent(Long contentId) {
        return sectionRepository.findByContentId(contentId).stream()
                .map(this::toSectionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EducationSectionResponseDTO addSection(Long contentId, EducationSectionRequestDTO dto, MultipartFile[] files) throws IOException {
        EducationContent content = repository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con id: " + contentId));

        List<EducationFile> savedFiles = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || !originalFilename.contains(".")) continue;
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFileName = System.currentTimeMillis() + "_sec_" + savedFiles.size() + fileExtension;
                Path path = Paths.get(UPLOAD_DIR + newFileName);
                Files.write(path, file.getBytes());
                String fileType = "OTRO";
                if (fileExtension.equalsIgnoreCase(".pdf")) fileType = "PDF";
                else if (fileExtension.matches("(?i)\\.(jpg|png|jpeg|webp)")) fileType = "IMAGE";
                savedFiles.add(EducationFile.builder()
                        .fileUrl("/" + path.toString().replace("\\", "/"))
                        .fileType(fileType)
                        .build());
            }
        }

        EducationSection section = EducationSection.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .content(content)
                .files(savedFiles)
                .build();

        EducationSection saved = sectionRepository.save(section);
        content.getSections().add(saved);
        repository.save(content);
        return toSectionDTO(saved);
    }

    @Transactional
    public EducationSectionResponseDTO updateSection(Long sectionId, EducationSectionRequestDTO dto) {
        EducationSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada con id: " + sectionId));
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        return toSectionDTO(sectionRepository.save(section));
    }

    @Transactional
    public void deleteSection(Long sectionId) {
        EducationSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada con id: " + sectionId));
        EducationContent content = section.getContent();
        if (content != null) {
            content.getSections().removeIf(s -> s.getId().equals(sectionId));
            repository.save(content);
        }
        sectionRepository.deleteById(sectionId);
    }

    @Transactional
    public EducationContentResponseDTO updateContent(Long id, EducationContentRequestDTO dto) {
        EducationContent content = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con id: " + id));

        content.setTitle(dto.getTitle());
        content.setDescription(dto.getDescription());
        content.setCategory(dto.getCategory());

        EducationContent saved = repository.save(content);
        List<EducationSection> sections = sectionRepository.findByContentId(saved.getId());
        saved.setSections(sections);
        return toContentDTO(saved);
    }

    public void deleteContent(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Contenido no encontrado con id: " + id);
        }
        repository.deleteById(id);
    }
}

