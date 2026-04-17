package com.co.ucentral.gestionResiduos.back.calendar;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para calendario de recolección de residuos.
 * GET por localidad es público (ciudadano consulta sin login).
 * CRUD completo solo para admin.
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class CollectionScheduleController {

    private final CollectionScheduleService scheduleService;

    // ========== Endpoints públicos (consulta ciudadano) ==========

    /**
     * GET /api/schedules/district/{districtId} - Calendario por localidad
     */
    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<CollectionScheduleDTO>> getByDistrict(@PathVariable int districtId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDistrict(districtId));
    }

    /**
     * GET /api/schedules/district/{districtId}/day/{dayOfWeek} - Por localidad y día
     */
    @GetMapping("/district/{districtId}/day/{dayOfWeek}")
    public ResponseEntity<List<CollectionScheduleDTO>> getByDistrictAndDay(
            @PathVariable int districtId,
            @PathVariable String dayOfWeek) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDistrictAndDay(districtId, dayOfWeek));
    }

    /**
     * GET /api/schedules/district/{districtId}/residue-type/{residueType} - Por localidad y tipo residuo
     */
    @GetMapping("/district/{districtId}/residue-type/{residueType}")
    public ResponseEntity<List<CollectionScheduleDTO>> getByDistrictAndResidueType(
            @PathVariable int districtId,
            @PathVariable String residueType) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDistrictAndResidueType(districtId, residueType));
    }

    /**
     * GET /api/schedules/{id} - Detalle de un calendario
     */
    @GetMapping("/{id}")
    public ResponseEntity<CollectionScheduleDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    // ========== Endpoints de administrador ==========

    /**
     * GET /api/schedules - Todos los calendarios (admin)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CollectionScheduleDTO>> getAll() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    /**
     * POST /api/schedules - Crear calendario (admin)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CollectionScheduleDTO> create(@Valid @RequestBody CollectionScheduleCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(dto));
    }

    /**
     * PUT /api/schedules/{id} - Actualizar calendario (admin)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CollectionScheduleDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CollectionScheduleCreateDTO dto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, dto));
    }

    /**
     * PATCH /api/schedules/{id}/status - Cambiar estado (admin)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CollectionScheduleDTO> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(scheduleService.changeStatus(id, status));
    }

    /**
     * DELETE /api/schedules/{id} - Eliminar calendario (admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}

