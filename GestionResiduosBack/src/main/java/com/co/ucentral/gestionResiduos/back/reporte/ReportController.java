package com.co.ucentral.gestionResiduos.back.reporte;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

/**
 * Controller para gestión de reportes
 * Soporta múltiples HUs:
 * - HU10: Reporte de puntos críticos
 * - HU11: Clasificación de reportes (categoryId)
 * - HU15: Reporte de incumplimiento en calendario
 * - HU25: Visualización de reportes (admin)
 * - HU26: Gestión de estado de reportes (admin)
 * - HU28: Estadísticas básicas (admin)
 * - HU34: Consulta de estado por ciudadano
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * HU10 + HU11 + HU15: Crear reporte (punto crítico o incumplimiento)
     * POST /api/reports
     * Body: multipart/form-data con imagen
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ReportDTO> createReport(
            @Valid ReportCreateDTO dto) {
        
        // Obtener email del usuario autenticado (del JWT)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = auth.getName(); // El JWT contiene el email
        
        ReportDTO report = reportService.createReport(dto, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * HU34: Obtener mis reportes (ciudadano ve sus propios reportes)
     * GET /api/reports/my-reports
     */
    @GetMapping("/my-reports")
    public ResponseEntity<List<ReportDTO>> getMyReports() {
        // Obtener email del usuario autenticado (del JWT)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = auth.getName();
        
        List<ReportDTO> reports = reportService.getMyReports(emailUsuario);
        return ResponseEntity.ok(reports);
    }

    /**
     * HU25: Obtener todos los reportes (admin)
     * GET /api/reports
     */
    @GetMapping
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        List<ReportDTO> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * HU25: Obtener reportes pendientes (admin)
     * GET /api/reports/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ReportDTO>> getPendingReports() {
        List<ReportDTO> reports = reportService.getPendingReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Obtener reporte por ID
     * GET /api/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Long id) {
        ReportDTO report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * Obtener reportes por tipo
     * GET /api/reports/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportDTO>> getReportsByType(@PathVariable String type) {
        List<ReportDTO> reports = reportService.getReportsByType(type);
        return ResponseEntity.ok(reports);
    }

    /**
     * Obtener reportes por estado
     * GET /api/reports/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReportDTO>> getReportsByStatus(@PathVariable String status) {
        List<ReportDTO> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    /**
     * HU26: Cambiar estado de reporte (admin)
     * PATCH /api/reports/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam String newStatus) {
        
        ReportDTO report = reportService.changeStatus(id, newStatus);
        return ResponseEntity.ok(report);
    }

    /**
     * Búsqueda de reportes con filtros combinados y paginación.
     * Todos los filtros son opcionales.
     * GET /api/reports/search?status=pendiente&type=punto_critico&dateFrom=2026-01-01&dateTo=2026-12-31&categoryId=1&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ReportDTO>> searchReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Date dateFrom,
            @RequestParam(required = false) Date dateTo,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReportDTO> reports = reportService.searchReports(status, type, dateFrom, dateTo, categoryId, page, size);
        return ResponseEntity.ok(reports);
    }

    /**
     * HU28: Obtener estadísticas (admin)
     * GET /api/reports/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ReportStatisticsDTO> getStatistics() {
        ReportStatisticsDTO stats = reportService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Nuevo endpoint: GET /api/reports/stats
     * Query params: startDate, endDate, status (optional), barrioId (optional)
     * Solo ADMIN
     */
    @GetMapping("/stats")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ReportStatsDTO> getFilteredStats(
            @RequestParam java.sql.Date startDate,
            @RequestParam java.sql.Date endDate,
            @RequestParam(required = false) String status) {

        ReportStatsDTO stats = reportService.getStats(startDate, endDate, status);
        return ResponseEntity.ok(stats);
    }
}

