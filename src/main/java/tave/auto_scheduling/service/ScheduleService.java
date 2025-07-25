package tave.auto_scheduling.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tave.auto_scheduling.config.TerminateType;
import tave.auto_scheduling.domain.Applicant;
import tave.auto_scheduling.domain.ApplicantAssignment;
import tave.auto_scheduling.domain.InterviewSchedule;
import tave.auto_scheduling.domain.InterviewSlot;
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

    public ApplicantAssignmentDto startSchedule(MultipartFile file, TerminateType type) {
        // Excel로부터 데이터 추출
        List<Applicant> applicants = excelExportService.loadApplicantsFromExcel(file);
        List<InterviewSlot> timeSlots = getUniqueTimeSlotList(applicants);

        // 문제 최적해 탐색 시작 ...
        SolverManager<InterviewSchedule, UUID> solverManager = solverManagerProvider.createSolverManager(type);
        InterviewSchedule solution = scheduleSolver.solve(timeSlots, applicants, solverManager);

        // 응답 Dto 정렬
        List<ApplicantAssignment> assignmentList = solution.getAssignmentList();
        assignmentList.sort(Comparator.comparing(a -> a.getAssignedSlot().getTime()));

        List<ApplicantDto> applicantDtoList = assignmentList.stream()
                .map(ApplicantDto::from)
                .toList();

        return ApplicantAssignmentDto.from(applicantDtoList);
    }

    /*
    * refactor
    * */

    private List<InterviewSlot> getUniqueTimeSlotList(List<Applicant> applicants) {
        Set<LocalDateTime> uniqueTimes = applicants.stream()
                .flatMap(a -> a.getAvailableSlots().stream())
                .collect(Collectors.toSet());

        List<InterviewSlot> timeSlots = uniqueTimes.stream()
                .sorted()
                .map(InterviewSlot::of)
                .toList();
        return timeSlots;
    }

}
