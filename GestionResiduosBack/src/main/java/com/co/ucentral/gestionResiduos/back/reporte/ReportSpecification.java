package com.co.ucentral.gestionResiduos.back.reporte;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications para filtros dinámicos de reportes.
 * Permite combinar filtros opcionales: estado, tipo, fecha, categoría.
 */
public class ReportSpecification {

    public static Specification<Report> withFilters(
            String status,
            String type,
            Date dateFrom,
            Date dateTo,
            Integer categoryId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // No hay filtro por barrio en la especificación (los reportes no están ligados directamente a barrio)

            // Ordenar por fecha de creación descendente
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

