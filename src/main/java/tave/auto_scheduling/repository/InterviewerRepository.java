package tave.auto_scheduling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tave.auto_scheduling.domain.Interviewer;

public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {
}
