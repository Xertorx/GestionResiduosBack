package com.co.ucentral.gestionResiduos.back.education;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(EducationContentService.class);
    private final EducationContentRepository repository;
    private final EducationSectionRepository sectionRepository;
    private static final String BASE_UPLOAD_DIR = "uploads/education/";

    @Autowired
    public EducationContentService(EducationContentRepository repository,
                                   EducationSectionRepository sectionRepository) {
        this.repository = repository;
        this.sectionRepository = sectionRepository;
        new File(BASE_UPLOAD_DIR).mkdirs();
    }

    // ──────────────────────────── HELPERS DE CARPETAS ────────────────────────────

    private String sanitizeFolderName(String name) {
        return name.trim()
                .replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s_-]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();
    }

    private String getContentDir(String contentTitle) {
        return BASE_UPLOAD_DIR + sanitizeFolderName(contentTitle) + "/";
    }

    private String getSectionDir(String contentTitle, String sectionTitle) {
        return getContentDir(contentTitle) + "sections/" + sanitizeFolderName(sectionTitle) + "/";
    }

    /** Elimina un archivo físico del disco dado su fileUrl relativa */
    private void deletePhysicalFile(String fileUrl) {
        if (fileUrl == null) return;
        try {
            // fileUrl viene como "/uploads/education/..." — quitar el "/" inicial
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path path = Paths.get(relativePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Archivo eliminado: {}", path);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo {}: {}", fileUrl, e.getMessage());
        }
    }

    /** Elimina todos los archivos físicos de una lista de EducationFile */
    private void deletePhysicalFiles(List<EducationFile> files) {
        if (files == null) return;
        files.forEach(f -> deletePhysicalFile(f.getFileUrl()));
    }

    /** Elimina una carpeta del disco (solo si está vacía; si tiene contenido, elimina todo recursivamente) */
    private void deleteDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) return;
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (f.isDirectory()) deleteDirectory(f.getAbsolutePath());
                else f.delete();
            }
        }
        dir.delete();
        log.info("Carpeta eliminada: {}", dirPath);
    }

    private List<EducationFile> saveFiles(MultipartFile[] files, String uploadDir) throws IOException {
        List<EducationFile> savedFiles = new ArrayList<>();
        if (files == null) return savedFiles;
        new File(uploadDir).mkdirs();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) continue;
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = System.currentTimeMillis() + "_" + savedFiles.size() + fileExtension;
            Path path = Paths.get(uploadDir + newFileName);
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
        return savedFiles;
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

    private EducationContentResponseDTO toContentDTOWithSections(EducationContent c, List<EducationSection> sections) {
        List<EducationFileDTO> files = c.getFiles() == null ? List.of() :
                c.getFiles().stream().map(this::toFileDTO).collect(Collectors.toList());
        List<EducationSectionResponseDTO> sectionDTOs = sections == null ? List.of() :
                sections.stream().map(this::toSectionDTO).collect(Collectors.toList());
        return EducationContentResponseDTO.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .createdAt(c.getCreatedAt())
                .files(files)
                .sections(sectionDTOs)
                .build();
    }

    // ──────────────────────────── CRUD CONTENIDO ────────────────────────────

    @Transactional
    public EducationContentResponseDTO saveContent(EducationContentRequestDTO dto, MultipartFile[] files) throws IOException {
        if (files == null || files.length == 0) {
            throw new RuntimeException("No se recibieron archivos válidos");
        }
        String contentDir = getContentDir(dto.getTitle());
        List<EducationFile> savedFiles = saveFiles(files, contentDir);
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
                    List<EducationSection> sections = sectionRepository.findByContentId(c.getId());
                    return toContentDTOWithSections(c, sections);
                })
                .collect(Collectors.toList());
    }

    public Optional<EducationContentResponseDTO> getContentById(Long id) {
        return repository.findById(id).map(content -> {
            List<EducationSection> sections = sectionRepository.findByContentId(id);
            return toContentDTOWithSections(content, sections);
        });
    }

    public List<EducationSectionResponseDTO> getSectionsByContent(Long contentId) {
        return sectionRepository.findByContentId(contentId).stream()
                .map(this::toSectionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza metadata del contenido.
     * Si se envían nuevos archivos, elimina los anteriores del disco y los reemplaza.
     * Si no se envían archivos, conserva los actuales.
     */
    @Transactional
    public EducationContentResponseDTO updateContent(Long id, EducationContentRequestDTO dto, MultipartFile[] newFiles) throws IOException {
        EducationContent content = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con id: " + id));

        String oldTitle = content.getTitle();
        content.setTitle(dto.getTitle());
        content.setDescription(dto.getDescription());
        content.setCategory(dto.getCategory());

        // Si se enviaron nuevos archivos: eliminar los viejos del disco y reemplazar
        if (newFiles != null && newFiles.length > 0) {
            deletePhysicalFiles(content.getFiles());
            content.getFiles().clear();

            String contentDir = getContentDir(dto.getTitle());
            // Si cambió el título, renombrar carpeta
            if (!sanitizeFolderName(oldTitle).equals(sanitizeFolderName(dto.getTitle()))) {
                File oldDir = new File(getContentDir(oldTitle));
                File newDir = new File(contentDir);
                if (oldDir.exists()) oldDir.renameTo(newDir);
            }

            List<EducationFile> savedFiles = saveFiles(newFiles, contentDir);
            content.getFiles().addAll(savedFiles);
        }

        EducationContent saved = repository.save(content);
        List<EducationSection> sections = sectionRepository.findByContentId(saved.getId());
        return toContentDTOWithSections(saved, sections);
    }

    /**
     * Elimina el contenido y todos sus archivos físicos (carpeta completa).
     */
    @Transactional
    public void deleteContent(Long id) {
        EducationContent content = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con id: " + id));

        // Eliminar carpeta completa del contenido (incluye secciones)
        deleteDirectory(getContentDir(content.getTitle()));

        repository.deleteById(id);
    }

    // ──────────────────────────── CRUD SECCIONES ────────────────────────────

    @Transactional
    public EducationSectionResponseDTO addSection(Long contentId, EducationSectionRequestDTO dto, MultipartFile[] files) throws IOException {
        EducationContent content = repository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con id: " + contentId));

        String sectionDir = getSectionDir(content.getTitle(), dto.getTitle());
        List<EducationFile> savedFiles = saveFiles(files, sectionDir);

        EducationSection section = EducationSection.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .content(content)
                .files(savedFiles)
                .build();

        return toSectionDTO(sectionRepository.save(section));
    }

    /**
     * Actualiza la sección.
     * Si se envían nuevos archivos, elimina los anteriores del disco y los reemplaza.
     * Si no se envían archivos, conserva los actuales.
     */
    @Transactional
    public EducationSectionResponseDTO updateSection(Long sectionId, EducationSectionRequestDTO dto, MultipartFile[] newFiles) throws IOException {
        EducationSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada con id: " + sectionId));

        String contentTitle = section.getContent().getTitle();
        String oldSectionTitle = section.getTitle();

        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());

        if (newFiles != null && newFiles.length > 0) {
            deletePhysicalFiles(section.getFiles());
            section.getFiles().clear();

            String sectionDir = getSectionDir(contentTitle, dto.getTitle());
            // Si cambió el título, renombrar carpeta
            if (!sanitizeFolderName(oldSectionTitle).equals(sanitizeFolderName(dto.getTitle()))) {
                File oldDir = new File(getSectionDir(contentTitle, oldSectionTitle));
                File newDir = new File(sectionDir);
                if (oldDir.exists()) oldDir.renameTo(newDir);
            }

            List<EducationFile> savedFiles = saveFiles(newFiles, sectionDir);
            section.getFiles().addAll(savedFiles);
        }

        return toSectionDTO(sectionRepository.save(section));
    }

    /**
     * Elimina la sección y sus archivos físicos del disco.
     */
    @Transactional
    public void deleteSection(Long sectionId) {
        EducationSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada con id: " + sectionId));

        EducationContent content = section.getContent();

        // Eliminar archivos físicos de la sección
        deletePhysicalFiles(section.getFiles());
        deleteDirectory(getSectionDir(content.getTitle(), section.getTitle()));

        if (content != null) {
            content.getSections().removeIf(s -> s.getId().equals(sectionId));
            repository.save(content);
        }
        sectionRepository.deleteById(sectionId);
    }
}

