package com.co.ucentral.gestionResiduos.back.reporte;

import com.co.ucentral.gestionResiduos.back.reporte.category.ReportCategory;
import com.co.ucentral.gestionResiduos.back.reporte.category.ReportCategoryRepository;
import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import com.co.ucentral.gestionResiduos.back.user.User;
import com.co.ucentral.gestionResiduos.back.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final UserRepository userRepository;
    private final ReportCategoryRepository categoryRepository;

    @Value("${app.upload.dir:./uploads/reports}")
    private String uploadDir;

    /**
     * HU10: Crear reporte de punto crítico
     * HU15: Crear reporte de incumplimiento en calendario
     * Validaciones condicionales según tipo
     */
    public ReportDTO createReport(ReportCreateDTO dto, String emailUsuario) {
        log.info("Creando reporte tipo: {} para usuario: {}", dto.getType(), emailUsuario);

        // 1. Obtener usuario autenticado por email (del JWT)
        User user = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + emailUsuario));

        // 2. Validaciones básicas
        if (dto.getType() == null || (!dto.getType().equals("punto_critico") && !dto.getType().equals("incumplimiento_calendario"))) {
            throw new IllegalArgumentException("Tipo de reporte inválido. Debe ser: punto_critico o incumplimiento_calendario");
        }

        // 3. Validaciones condicionales por tipo
        if ("punto_critico".equals(dto.getType())) {
            validateCriticalPointReport(dto);
        } else if ("incumplimiento_calendario".equals(dto.getType())) {
            validateCalendarNonComplianceReport(dto);
        }

        // 4. Procesar imagen si existe
        String imageUrl = null;
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            imageUrl = saveImage(dto.getImage());
        }

        // 5. Buscar categoría
        ReportCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoryId()));

        if (!"ACTIVO".equals(category.getStatus())) {
            throw new IllegalArgumentException("La categoría seleccionada está inactiva");
        }

        // 6. Crear entidad Report
        Report report = new Report();
        report.setType(dto.getType());
        report.setCategory(category);
        report.setDescription(dto.getDescription());
        report.setLatitude(dto.getLatitude());
        report.setLongitude(dto.getLongitude());
        report.setImageUrl(imageUrl);
        report.setCalendarId(dto.getCalendarId());
        report.setUser(user);
        report.setStatus("pendiente");

        // 7. Guardar reporte
        Report reportSaved = reportRepository.save(report);
        log.info("Reporte creado exitosamente. ID: {}", reportSaved.getId());

        // 8. Convertir a DTO y retornar
        return reportMapper.toDTO(reportSaved);
    }

    /**
     * Validaciones específicas para HU10 - Reporte de punto crítico
     */
    private void validateCriticalPointReport(ReportCreateDTO dto) {
        if (dto.getLatitude() == null) {
            throw new IllegalArgumentException("La latitud es obligatoria para reportes de punto crítico (HU10)");
        }
        if (dto.getLongitude() == null) {
            throw new IllegalArgumentException("La longitud es obligatoria para reportes de punto crítico (HU10)");
        }
        if (dto.getImage() == null || dto.getImage().isEmpty()) {
            throw new IllegalArgumentException("La foto es obligatoria para reportes de punto crítico (HU10)");
        }
    }

    /**
     * Validaciones específicas para HU15 - Reporte de incumplimiento en calendario
     */
    private void validateCalendarNonComplianceReport(ReportCreateDTO dto) {
        if (dto.getCalendarId() == null) {
            throw new IllegalArgumentException("El ID del calendario es obligatorio para reportes de incumplimiento");
        }
        // La imagen es opcional para este tipo
    }

    /**
     * Guardar imagen en el servidor
     */
    private String saveImage(MultipartFile image) {
        try {
            if (!image.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("El archivo debe ser una imagen");
            }

            // Crear directorio si no existe
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            // Generar nombre único
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Guardar archivo
            Files.copy(image.getInputStream(), filePath);
            log.info("Imagen guardada: {}", fileName);

            return "/uploads/reports/" + fileName;
        } catch (IOException e) {
            log.error("Error al guardar imagen: ", e);
            throw new IllegalStateException("Error al procesar la imagen. Intenta nuevamente.", e);
        }
    }

    /**
     * HU25: Obtener todos los reportes (para admin)
     */
    public List<ReportDTO> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * HU34: Obtener reportes del usuario autenticado
     */
    public List<ReportDTO> getMyReports(String emailUsuario) {
        // Obtener usuario por email del JWT
        User user = userRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + emailUsuario));

        return reportRepository.findByUserDocumentNumber(user.getDocumentNumber())
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reporte por ID
     */
    public ReportDTO getReportById(Long id) {
        return reportRepository.findById(id)
                .map(reportMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + id));
    }

    /**
     * HU25: Obtener reportes pendientes
     */
    public List<ReportDTO> getPendingReports() {
        return reportRepository.findPending()
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * HU26: Cambiar estado de un reporte
     */
    public ReportDTO changeStatus(Long id, String newStatus) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + id));

        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Estado inválido. Debe ser: pendiente, en_revision, resuelto, rechazado");
        }

        report.setStatus(newStatus);
        if ("resuelto".equals(newStatus)) {
            report.setResolvedAt(new Date(System.currentTimeMillis()));
        }

        Report reportUpdated = reportRepository.save(report);
        log.info("Estado del reporte {} cambiado a: {}", id, newStatus);

        return reportMapper.toDTO(reportUpdated);
    }

    /**
     * Validar que el estado sea válido
     */
    private boolean isValidStatus(String status) {
        return status.equals("pendiente") || status.equals("en_revision") ||
               status.equals("resuelto") || status.equals("rechazado");
    }

    /**
     * HU28: Obtener estadísticas básicas de reportes
     */
    public ReportStatisticsDTO getStatistics() {
        List<Report> todos = reportRepository.findAll();

        ReportStatisticsDTO stats = new ReportStatisticsDTO();
        stats.setTotal(todos.size());
        stats.setPending((int) todos.stream().filter(r -> "pendiente".equals(r.getStatus())).count());
        stats.setInReview((int) todos.stream().filter(r -> "en_revision".equals(r.getStatus())).count());
        stats.setResolved((int) todos.stream().filter(r -> "resuelto".equals(r.getStatus())).count());
        stats.setRejected((int) todos.stream().filter(r -> "rechazado".equals(r.getStatus())).count());
        stats.setCriticalPoints((int) todos.stream().filter(r -> "punto_critico".equals(r.getType())).count());
        stats.setCalendarNonCompliance((int) todos.stream().filter(r -> "incumplimiento_calendario".equals(r.getType())).count());

        return stats;
    }

    /**
     * Obtener reportes por tipo
     */
    public List<ReportDTO> getReportsByType(String type) {
        return reportRepository.findByType(type)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reportes por estado
     */
    public List<ReportDTO> getReportsByStatus(String status) {
        return reportRepository.findByStatus(status)
                .stream()
                .map(reportMapper::toDTO)
                .collect(Collectors.toList());
    }
}

