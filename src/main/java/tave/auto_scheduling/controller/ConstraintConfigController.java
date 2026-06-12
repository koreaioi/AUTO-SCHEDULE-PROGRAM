package tave.auto_scheduling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tave.auto_scheduling.domain.ConstraintConfig;
import tave.auto_scheduling.dto.request.ConstraintConfigUpdateRequest;
import tave.auto_scheduling.service.ConstraintConfigService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/constraints")
public class ConstraintConfigController {

    private final ConstraintConfigService constraintConfigService;

    @GetMapping
    public ResponseEntity<List<ConstraintConfig>> getAll() {
        return ResponseEntity.ok(constraintConfigService.findAll());
    }

    @PutMapping("/{name}")
    public ResponseEntity<ConstraintConfig> update(
            @PathVariable String name,
            @RequestBody ConstraintConfigUpdateRequest request) {
        return ResponseEntity.ok(constraintConfigService.update(name, request));
    }
}
