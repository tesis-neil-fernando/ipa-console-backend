package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ParameterEntity;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.N8nWorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import com.fernandoschilder.ipaconsolebackend.utils.ProcessMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepository processRepository;
    private final N8nWorkflowRepository workflowRepository;

    @Transactional
    public ProcessResponseDto create(ProcessCreateDto dto) {
        var workflow = workflowRepository.findById(dto.workflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + dto.workflowId()));

        var process = new ProcessEntity();
        process.setName(dto.name());
        process.setDescription(dto.description());
        process.setWorkflow(workflow);

        if (dto.parameters() != null && !dto.parameters().isEmpty()) {
            dto.parameters().forEach(pd -> {
                var pe = new ParameterEntity();
                pe.setName(pd.name());
                pe.setValue(pd.value());
                pe.setType(pd.type());
                process.addParameter(pe); // <-- mantiene ambos lados
            });
        }

        var saved = processRepository.save(process); // cascades -> guarda parámetros

        // Si quieres asegurarte de tener la colección inicializada para el mapper:
        // (opcional si el mapper corre dentro de la @Transactional actual)
        var reloaded = processRepository.findById(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Process not found after create: " + saved.getId()));

        return ProcessMapper.toResponseDto(reloaded);
    }

    @Transactional(readOnly = true)
    public ProcessResponseDto get(Long id) {
        var p = processRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Process not found: " + id));
        return ProcessMapper.toResponseDto(p);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponseDto> list() {
        return processRepository.findAll().stream()
                .map(ProcessMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!processRepository.existsById(id)) {
            throw new EntityNotFoundException("Process not found: " + id);
        }
        processRepository.deleteById(id); // orphanRemoval elimina parámetros asociados
    }
}
