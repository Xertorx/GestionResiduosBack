package com.co.ucentral.gestionResiduos.back.education;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class EducationContentServiceTest {

    private EducationContentService service;

    @Mock
    private EducationContentRepository repository;

    @Mock
    private EducationSectionRepository sectionRepository;

    // ── Helpers ──────────────────────────────────────────────────────

    private EducationContent buildContent(Long id) {
        return EducationContent.builder()
                .id(id)
                .title("Reciclaje Básico")
                .description("Guía de reciclaje")
                .category("reciclaje")
                .createdAt(LocalDateTime.now())
                .files(List.of(EducationFile.builder()
                        .fileUrl("/uploads/education/test.jpg")
                        .fileType("IMAGE")
                        .build()))
                .sections(new ArrayList<>())
                .build();
    }

    private EducationSection buildSection(Long id, EducationContent content) {
        return EducationSection.builder()
                .id(id)
                .title("Sección 1")
                .description("Descripción sección")
                .content(content)
                .files(new ArrayList<>())
                .build();
    }

    private MultipartFile mockImageFile() {
        return new MockMultipartFile(
                "files", "foto.jpg", "image/jpeg", "fake-image-bytes".getBytes());
    }

    private MultipartFile mockPdfFile() {
        return new MockMultipartFile(
                "files", "documento.pdf", "application/pdf", "fake-pdf-bytes".getBytes());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EducationContentService(repository, sectionRepository);
    }

    // ── saveContent ───────────────────────────────────────────────────

    @Test
    void saveContent_conImagenValida_retornaDTO() throws IOException {
        // Arrange
        EducationContentRequestDTO dto = new EducationContentRequestDTO();
        dto.setTitle("Reciclaje Básico");
        dto.setDescription("Guía de reciclaje");
        dto.setCategory("reciclaje");

        EducationContent saved = buildContent(1L);
        when(repository.save(any(EducationContent.class))).thenReturn(saved);

        // Act
        EducationContentResponseDTO result = service.saveContent(dto, new MultipartFile[]{mockImageFile()});

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Reciclaje Básico", result.getTitle());
        assertFalse(result.getFiles().isEmpty());
        verify(repository, times(1)).save(any(EducationContent.class));
    }

    @Test
    void saveContent_conPdf_asignaFileTypePdf() throws IOException {
        // Arrange
        EducationContentRequestDTO dto = new EducationContentRequestDTO();
        dto.setTitle("Manual PDF");
        dto.setDescription("Desc");
        dto.setCategory("educacion");

        EducationContent saved = EducationContent.builder()
                .id(2L).title("Manual PDF").description("Desc").category("educacion")
                .createdAt(LocalDateTime.now())
                .files(List.of(EducationFile.builder().fileUrl("/uploads/education/doc.pdf").fileType("PDF").build()))
                .sections(new ArrayList<>()).build();

        when(repository.save(any(EducationContent.class))).thenReturn(saved);

        // Act
        EducationContentResponseDTO result = service.saveContent(dto, new MultipartFile[]{mockPdfFile()});

        // Assert
        assertEquals("PDF", result.getFiles().get(0).getFileType());
    }

    @Test
    void saveContent_sinArchivosValidos_lanzaExcepcion() {
        // Arrange
        EducationContentRequestDTO dto = new EducationContentRequestDTO();
        dto.setTitle("Sin Archivo");

        MultipartFile emptyFile = new MockMultipartFile("files", "empty.jpg", "image/jpeg", new byte[0]);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> service.saveContent(dto, new MultipartFile[]{emptyFile}));
        verify(repository, never()).save(any());
    }

    @Test
    void saveContent_arregloVacio_lanzaExcepcion() {
        // Arrange
        EducationContentRequestDTO dto = new EducationContentRequestDTO();
        dto.setTitle("Sin Archivo");

        MultipartFile noExt = new MockMultipartFile("files", "sinextension", "image/jpeg", "data".getBytes());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> service.saveContent(dto, new MultipartFile[]{noExt}));
    }

    // ── getAllContents ────────────────────────────────────────────────

    @Test
    void getAllContents_retornaListaDTO() {
        // Arrange
        List<EducationContent> contents = List.of(buildContent(1L), buildContent(2L));
        when(repository.findAll()).thenReturn(contents);
        when(sectionRepository.findByContentId(anyLong())).thenReturn(new ArrayList<>());

        // Act
        List<EducationContentResponseDTO> result = service.getAllContents();

        // Assert
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllContents_sinContenidos_retornaListaVacia() {
        // Arrange
        when(repository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<EducationContentResponseDTO> result = service.getAllContents();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ── getContentById ────────────────────────────────────────────────

    @Test
    void getContentById_existente_retornaDTO() {
        // Arrange
        EducationContent content = buildContent(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(content));
        when(sectionRepository.findByContentId(1L)).thenReturn(new ArrayList<>());

        // Act
        Optional<EducationContentResponseDTO> result = service.getContentById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Reciclaje Básico", result.get().getTitle());
    }

    @Test
    void getContentById_noExistente_retornaVacio() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<EducationContentResponseDTO> result = service.getContentById(99L);

        // Assert
        assertFalse(result.isPresent());
    }

    // ── getSectionsByContent ──────────────────────────────────────────

    @Test
    void getSectionsByContent_retornaListaSecciones() {
        // Arrange
        EducationContent content = buildContent(1L);
        List<EducationSection> sections = List.of(
                buildSection(1L, content),
                buildSection(2L, content)
        );
        when(sectionRepository.findByContentId(1L)).thenReturn(sections);

        // Act
        List<EducationSectionResponseDTO> result = service.getSectionsByContent(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Sección 1", result.get(0).getTitle());
    }

    // ── addSection ────────────────────────────────────────────────────

    @Test
    void addSection_contenidoExistente_retornaSeccionDTO() throws IOException {
        // Arrange
        EducationContent content = buildContent(1L);
        EducationSectionRequestDTO dto = new EducationSectionRequestDTO();
        dto.setTitle("Nueva Sección");
        dto.setDescription("Descripción nueva");

        EducationSection saved = buildSection(10L, content);
        saved.setTitle("Nueva Sección");

        when(repository.findById(1L)).thenReturn(Optional.of(content));
        when(sectionRepository.save(any(EducationSection.class))).thenReturn(saved);
        when(repository.save(any(EducationContent.class))).thenReturn(content);

        // Act
        EducationSectionResponseDTO result = service.addSection(1L, dto, null);

        // Assert
        assertNotNull(result);
        assertEquals("Nueva Sección", result.getTitle());
        verify(sectionRepository, times(1)).save(any(EducationSection.class));
    }

    @Test
    void addSection_contenidoNoExistente_lanzaExcepcion() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());
        EducationSectionRequestDTO dto = new EducationSectionRequestDTO();
        dto.setTitle("Sección");

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> service.addSection(99L, dto, null));
        verify(sectionRepository, never()).save(any());
    }

    // ── updateSection ─────────────────────────────────────────────────

    @Test
    void updateSection_existente_actualizaYRetornaDTO() {
        // Arrange
        EducationContent content = buildContent(1L);
        EducationSection section = buildSection(5L, content);
        EducationSectionRequestDTO dto = new EducationSectionRequestDTO();
        dto.setTitle("Título Actualizado");
        dto.setDescription("Descripción Actualizada");

        when(sectionRepository.findById(5L)).thenReturn(Optional.of(section));
        when(sectionRepository.save(any(EducationSection.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        EducationSectionResponseDTO result = service.updateSection(5L, dto);

        // Assert
        assertEquals("Título Actualizado", result.getTitle());
        assertEquals("Descripción Actualizada", result.getDescription());
    }

    @Test
    void updateSection_noExistente_lanzaExcepcion() {
        // Arrange
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());
        EducationSectionRequestDTO dto = new EducationSectionRequestDTO();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.updateSection(99L, dto));
    }

    // ── deleteSection ─────────────────────────────────────────────────

    @Test
    void deleteSection_existente_eliminaCorrectamente() {
        // Arrange
        EducationContent content = buildContent(1L);
        EducationSection section = buildSection(5L, content);
        content.getSections().add(section);

        when(sectionRepository.findById(5L)).thenReturn(Optional.of(section));
        when(repository.save(any(EducationContent.class))).thenReturn(content);

        // Act
        assertDoesNotThrow(() -> service.deleteSection(5L));

        // Assert
        verify(sectionRepository, times(1)).deleteById(5L);
    }

    @Test
    void deleteSection_noExistente_lanzaExcepcion() {
        // Arrange
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.deleteSection(99L));
        verify(sectionRepository, never()).deleteById(any());
    }

    // ── updateContent ─────────────────────────────────────────────────

    @Test
    void updateContent_existente_actualizaMetadata() {
        // Arrange
        EducationContent content = buildContent(1L);
        EducationContentRequestDTO dto = new EducationContentRequestDTO();
        dto.setTitle("Título Nuevo");
        dto.setDescription("Descripción Nueva");
        dto.setCategory("compostaje");

        when(repository.findById(1L)).thenReturn(Optional.of(content));
        when(repository.save(any(EducationContent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sectionRepository.findByContentId(1L)).thenReturn(new ArrayList<>());

        // Act
        EducationContentResponseDTO result = service.updateContent(1L, dto);

        // Assert
        assertEquals("Título Nuevo", result.getTitle());
        assertEquals("compostaje", result.getCategory());
        verify(repository, times(1)).save(any(EducationContent.class));
    }

    @Test
    void updateContent_noExistente_lanzaExcepcion() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());
        EducationContentRequestDTO dto = new EducationContentRequestDTO();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.updateContent(99L, dto));
    }

    // ── deleteContent ─────────────────────────────────────────────────

    @Test
    void deleteContent_existente_eliminaCorrectamente() {
        // Arrange
        when(repository.existsById(1L)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> service.deleteContent(1L));

        // Assert
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteContent_noExistente_lanzaExcepcion() {
        // Arrange
        when(repository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.deleteContent(99L));
        verify(repository, never()).deleteById(any());
    }
}

