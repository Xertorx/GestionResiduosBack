package com.co.ucentral.gestionResiduos.back.reporte.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-categories")
@RequiredArgsConstructor
public class ReportCategoryController {

    private final ReportCategoryService categoryService;

    /**
     * Obtener todas las categorías (admin)
     */
    @GetMapping
    public ResponseEntity<List<ReportCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Obtener solo categorías activas (para formulario de reportes ciudadanos)
     */
    @GetMapping("/active")
    public ResponseEntity<List<ReportCategoryDTO>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    /**
     * Obtener categoría por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportCategoryDTO> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Crear nueva categoría (admin)
     */
    @PostMapping
    public ResponseEntity<ReportCategoryDTO> createCategory(@Valid @RequestBody ReportCategoryDTO dto) {
        ReportCategoryDTO created = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar categoría existente (admin)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportCategoryDTO> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody ReportCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    /**
     * Cambiar estado de categoría (admin) - deshabilitar/habilitar
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportCategoryDTO> changeStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        return ResponseEntity.ok(categoryService.changeStatus(id, status));
    }

    /**
     * Eliminar categoría (admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

