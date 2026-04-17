package com.co.ucentral.gestionResiduos.back.calendar;

import com.co.ucentral.gestionResiduos.back.Geography.District.District;
import com.co.ucentral.gestionResiduos.back.Geography.District.DistrictRepository;
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
public class CollectionScheduleService {

    private final CollectionScheduleRepository scheduleRepository;
    private final DistrictRepository districtRepository;

    /**
     * Obtener calendario por localidad (público)
     */
    public List<CollectionScheduleDTO> getSchedulesByDistrict(int districtId) {
        districtRepository.findById(districtId)
                .orElseThrow(() -> new ResourceNotFoundException("Localidad no encontrada con ID: " + districtId));

        return scheduleRepository.findByDistrictDistrictIdAndStatus(districtId, "ACTIVO")
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener calendario por localidad y día de la semana
     */
    public List<CollectionScheduleDTO> getSchedulesByDistrictAndDay(int districtId, String dayOfWeek) {
        return scheduleRepository.findByDistrictDistrictIdAndDayOfWeek(districtId, dayOfWeek.toUpperCase())
                .stream()
                .filter(s -> "ACTIVO".equals(s.getStatus()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener calendario por localidad y tipo de residuo
     */
    public List<CollectionScheduleDTO> getSchedulesByDistrictAndResidueType(int districtId, String residueType) {
        return scheduleRepository.findByDistrictDistrictIdAndResidueType(districtId, residueType.toUpperCase())
                .stream()
                .filter(s -> "ACTIVO".equals(s.getStatus()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los calendarios (admin)
     */
    public List<CollectionScheduleDTO> getAllSchedules() {
        return scheduleRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener calendario por ID
     */
    public CollectionScheduleDTO getScheduleById(Long id) {
        CollectionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario no encontrado con ID: " + id));
        return toDTO(schedule);
    }

    /**
     * Crear calendario (admin)
     */
    public CollectionScheduleDTO createSchedule(CollectionScheduleCreateDTO dto) {
        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("Localidad no encontrada con ID: " + dto.getDistrictId()));

        validateDayOfWeek(dto.getDayOfWeek());

        CollectionSchedule schedule = new CollectionSchedule();
        schedule.setDistrict(district);
        schedule.setResidueType(dto.getResidueType().toUpperCase());
        schedule.setDayOfWeek(dto.getDayOfWeek().toUpperCase());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setDescription(dto.getDescription());
        schedule.setStatus("ACTIVO");

        CollectionSchedule saved = scheduleRepository.save(schedule);
        log.info("Calendario creado: ID {} para localidad {}", saved.getId(), district.getName());
        return toDTO(saved);
    }

    /**
     * Actualizar calendario (admin)
     */
    public CollectionScheduleDTO updateSchedule(Long id, CollectionScheduleCreateDTO dto) {
        CollectionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario no encontrado con ID: " + id));

        if (dto.getDistrictId() != null) {
            District district = districtRepository.findById(dto.getDistrictId())
                    .orElseThrow(() -> new ResourceNotFoundException("Localidad no encontrada con ID: " + dto.getDistrictId()));
            schedule.setDistrict(district);
        }
        if (dto.getResidueType() != null) {
            schedule.setResidueType(dto.getResidueType().toUpperCase());
        }
        if (dto.getDayOfWeek() != null) {
            validateDayOfWeek(dto.getDayOfWeek());
            schedule.setDayOfWeek(dto.getDayOfWeek().toUpperCase());
        }
        if (dto.getStartTime() != null) {
            schedule.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            schedule.setEndTime(dto.getEndTime());
        }
        if (dto.getDescription() != null) {
            schedule.setDescription(dto.getDescription());
        }

        CollectionSchedule saved = scheduleRepository.save(schedule);
        log.info("Calendario actualizado: ID {}", saved.getId());
        return toDTO(saved);
    }

    /**
     * Cambiar estado de calendario (admin)
     */
    public CollectionScheduleDTO changeStatus(Long id, String status) {
        CollectionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario no encontrado con ID: " + id));

        if (!"ACTIVO".equals(status) && !"INACTIVO".equals(status)) {
            throw new IllegalArgumentException("Estado inválido. Debe ser: ACTIVO o INACTIVO");
        }

        schedule.setStatus(status);
        CollectionSchedule saved = scheduleRepository.save(schedule);
        log.info("Estado del calendario {} cambiado a: {}", id, status);
        return toDTO(saved);
    }

    /**
     * Eliminar calendario (admin)
     */
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Calendario no encontrado con ID: " + id);
        }
        scheduleRepository.deleteById(id);
        log.info("Calendario eliminado: ID {}", id);
    }

    private void validateDayOfWeek(String day) {
        List<String> validDays = List.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO");
        if (!validDays.contains(day.toUpperCase())) {
            throw new IllegalArgumentException("Día inválido. Debe ser: " + String.join(", ", validDays));
        }
    }

    private CollectionScheduleDTO toDTO(CollectionSchedule schedule) {
        return CollectionScheduleDTO.builder()
                .id(schedule.getId())
                .districtId(schedule.getDistrict().getDistrictId())
                .districtName(schedule.getDistrict().getName())
                .residueType(schedule.getResidueType())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .description(schedule.getDescription())
                .status(schedule.getStatus())
                .build();
    }
}

