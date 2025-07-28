package tave.auto_scheduling.solver;

import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
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

        SolverJob<InterviewSchedule, UUID> solverJob = solverManager.solveAndListen(
                problemId,
                id -> problem,
                bestSolution -> {
                    HardSoftScore score = bestSolution.getScore();
                    log.info("🧠 새로운 최적 해 발견: hard = {}, soft = {}",
                            score.hardScore(), score.softScore());
                });

        try {

            InterviewSchedule solution = solverJob.getFinalBestSolution();
            checkHardConstraint(solution);
            isChosenTimeSlotByApplicant(solution);

            return solution;
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("문제를 해결하지 못했습니다.", e);
        }
    }

    public void checkHardConstraint(InterviewSchedule solution) {
        if (solution.getScore().hardScore() < 0) {
            throw new IllegalStateException("하드 제약을 만족하는 해를 찾지 못했습니다.");
        }
    }

    public void isChosenTimeSlotByApplicant(InterviewSchedule solution) {
        List<ApplicantAssignment> assignmentList = solution.getAssignmentList();

        for(ApplicantAssignment assignment : assignmentList) {
            if (!assignment.validAssignedTimeSlot()) {
                throw new IllegalStateException("지원자가 희망하는 시간에 배정되지 않았습니다");
            }
        }
    }

}
