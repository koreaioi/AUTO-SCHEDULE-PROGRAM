package tave.auto_scheduling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tave.auto_scheduling.domain.Interviewer;
import tave.auto_scheduling.dto.request.InterviewerCreateRequest;
import tave.auto_scheduling.service.InterviewerService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interviewers")
public class InterviewerController {

    private final InterviewerService interviewerService;

    @GetMapping
    public ResponseEntity<List<Interviewer>> getAll() {
        return ResponseEntity.ok(interviewerService.findAll());
    }

    @PostMapping
    public ResponseEntity<Interviewer> create(@RequestBody InterviewerCreateRequest request) {
        return ResponseEntity.ok(interviewerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Interviewer> update(
            @PathVariable Long id,
            @RequestBody InterviewerCreateRequest request) {
        return ResponseEntity.ok(interviewerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interviewerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
