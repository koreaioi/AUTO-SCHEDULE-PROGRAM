package tave.auto_scheduling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tave.auto_scheduling.config.TerminateType;
import tave.auto_scheduling.domain.*;
import tave.auto_scheduling.dto.response.ApplicantAssignmentDto;
import tave.auto_scheduling.dto.response.ApplicantDto;
import tave.auto_scheduling.provider.SolverManagerProvider;
import tave.auto_scheduling.solver.ScheduleSolver;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ExcelExportService excelExportService;
    private final SolverManagerProvider solverManagerProvider;
    private final ScheduleSolver scheduleSolver;
    private final ConstraintConfigService constraintConfigService;
    private final InterviewerService interviewerService;

    public ApplicantAssignmentDto startSchedule(MultipartFile file, TerminateType type) {
        List<Applicant> applicants = excelExportService.loadApplicantsFromExcel(file);
        List<InterviewSlot> timeSlots = getUniqueTimeSlotList(applicants);
        List<ConstraintConfig> constraintConfigs = constraintConfigService.findAll();
        List<Interviewer> interviewers = interviewerService.findAll();

        SolverManager<InterviewSchedule, UUID> solverManager = solverManagerProvider.createSolverManager(type);
        InterviewSchedule solution = scheduleSolver.solve(timeSlots, applicants, constraintConfigs, interviewers, solverManager);

        List<ApplicantAssignment> assignmentList = solution.getAssignmentList();
        assignmentList.sort(Comparator.comparing(a -> a.getAssignedSlot().getTime()));

        List<ApplicantDto> applicantDtoList = assignmentList.stream()
                .map(ApplicantDto::from)
                .toList();

        HardSoftScore score = solution.getScore();
        return ApplicantAssignmentDto.from(applicantDtoList, score.hardScore(), score.softScore());
    }

    private List<InterviewSlot> getUniqueTimeSlotList(List<Applicant> applicants) {
        Set<LocalDateTime> uniqueTimes = applicants.stream()
                .flatMap(a -> a.getAvailableSlots().stream())
                .collect(Collectors.toSet());

        return uniqueTimes.stream()
                .sorted()
                .map(InterviewSlot::of)
                .toList();
    }
}
