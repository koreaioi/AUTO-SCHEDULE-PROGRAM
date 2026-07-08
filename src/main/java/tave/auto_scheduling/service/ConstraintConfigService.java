package tave.auto_scheduling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tave.auto_scheduling.domain.ConstraintConfig;
import tave.auto_scheduling.dto.request.ConstraintConfigUpdateRequest;
import tave.auto_scheduling.repository.ConstraintConfigRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConstraintConfigService {

    private final ConstraintConfigRepository repository;

    public List<ConstraintConfig> findAll() {
        return repository.findAll();
    }

    @Transactional
    public ConstraintConfig update(String name, ConstraintConfigUpdateRequest request) {
        ConstraintConfig config = repository.findByConstraintName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제약 조건: " + name));

        if (request.enabled() != null) config.setEnabled(request.enabled());
        if (request.weight() != null) config.setWeight(request.weight());
        if (request.threshold() != null) config.setThreshold(request.threshold());
        if (request.preferredDays() != null) config.setPreferredDays(request.preferredDays());

        return config;
    }
}
