package tave.auto_scheduling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tave.auto_scheduling.domain.Interviewer;
import tave.auto_scheduling.dto.request.InterviewerCreateRequest;
import tave.auto_scheduling.repository.InterviewerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewerService {

    private final InterviewerRepository repository;

    public List<Interviewer> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Interviewer create(InterviewerCreateRequest request) {
        Interviewer interviewer = new Interviewer();
        interviewer.setName(request.name());
        interviewer.setPart(request.part());
        interviewer.setAvailableTimes(request.availableTimes());
        return repository.save(interviewer);
    }

    @Transactional
    public Interviewer update(Long id, InterviewerCreateRequest request) {
        Interviewer interviewer = findById(id);
        interviewer.setName(request.name());
        interviewer.setPart(request.part());
        interviewer.setAvailableTimes(request.availableTimes());
        return interviewer;
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Interviewer findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 면접관: " + id));
    }
}
