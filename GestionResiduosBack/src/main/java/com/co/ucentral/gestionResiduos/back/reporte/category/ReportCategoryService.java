package com.co.ucentral.gestionResiduos.back.reporte.category;

import com.co.ucentral.gestionResiduos.back.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportCategoryService {

    private final ReportCategoryRepository categoryRepository;

    /**
     * Obtener todas las categorías
     */
    public List<ReportCategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener solo categorías activas (para el formulario de reportes ciudadanos)
     */
    public List<ReportCategoryDTO> getActiveCategories() {
        return categoryRepository.findByStatus("ACTIVO")
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener categoría por ID
     */
    public ReportCategoryDTO getCategoryById(Integer id) {
        ReportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return toDTO(category);
    }

    /**
     * Crear nueva categoría
     */
    public ReportCategoryDTO createCategory(ReportCategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + dto.getName());
        }

        ReportCategory category = new ReportCategory();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVO");

        ReportCategory saved = categoryRepository.save(category);
        log.info("Categoría creada: {} (ID: {})", saved.getName(), saved.getId());
        return toDTO(saved);
    }

    /**
     * Actualizar categoría existente
     */
    public ReportCategoryDTO updateCategory(Integer id, ReportCategoryDTO dto) {
        ReportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Validar nombre duplicado solo si cambió
        if (dto.getName() != null && !dto.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + dto.getName());
            }
            category.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }

        ReportCategory saved = categoryRepository.save(category);
        log.info("Categoría actualizada: {} (ID: {})", saved.getName(), saved.getId());
        return toDTO(saved);
    }

    /**
     * Cambiar estado de categoría (ACTIVO/INACTIVO)
     */
    public ReportCategoryDTO changeStatus(Integer id, String status) {
        ReportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if (!"ACTIVO".equals(status) && !"INACTIVO".equals(status)) {
            throw new IllegalArgumentException("Estado inválido. Debe ser: ACTIVO o INACTIVO");
        }

        category.setStatus(status);
        ReportCategory saved = categoryRepository.save(category);
        log.info("Estado de categoría {} cambiado a: {}", id, status);
        return toDTO(saved);
    }

    /**
     * Eliminar categoría
     */
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
        }
        categoryRepository.deleteById(id);
        log.info("Categoría eliminada: ID {}", id);
    }

    /**
     * Mapper interno: Entity -> DTO
     */
    private ReportCategoryDTO toDTO(ReportCategory entity) {
        ReportCategoryDTO dto = new ReportCategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}

