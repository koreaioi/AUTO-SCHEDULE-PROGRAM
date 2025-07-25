package tave.auto_scheduling.solver;

import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.stereotype.Service;
import tave.auto_scheduling.domain.Applicant;
import tave.auto_scheduling.domain.ApplicantAssignment;
import tave.auto_scheduling.domain.InterviewSchedule;
import tave.auto_scheduling.domain.InterviewSlot;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleSolver {

    public InterviewSchedule solve(List<InterviewSlot> timeSlots, List<Applicant> applicants, SolverManager<InterviewSchedule, UUID> solverManager) {

        // TODO 고민 - List<Applicant> -> List<ApplicantAssignment>가 과연 Solver의 책임인가...?
        List<ApplicantAssignment> assignmentList = applicants.stream()
                .map(ApplicantAssignment::new)
                .collect(Collectors.toList());

        InterviewSchedule problem = new InterviewSchedule(timeSlots, assignmentList);

        UUID problemId = UUID.randomUUID();
        SolverJob<InterviewSchedule, UUID> solverJob = solverManager.solve(problemId, problem);

        // 최종 최적해를 동기적으로 기다림.
        try {
            InterviewSchedule solution = solverJob.getFinalBestSolution();
            return solution;
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("문제를 해결하지 못했습니다.", e);
        }
    }

}
