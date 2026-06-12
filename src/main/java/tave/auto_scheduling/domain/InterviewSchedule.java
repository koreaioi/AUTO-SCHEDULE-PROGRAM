package tave.auto_scheduling.domain;

import lombok.Getter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@Getter
@PlanningSolution
public class InterviewSchedule {

    @ValueRangeProvider(id = "timeSlotRange")
    private List<InterviewSlot> timeSlotList;

    @PlanningEntityCollectionProperty
    private List<ApplicantAssignment> assignmentList;

    @ProblemFactCollectionProperty
    private List<ConstraintConfig> constraintConfigs;

    @ProblemFactCollectionProperty
    private List<Interviewer> interviewerList;

    @PlanningScore
    private HardSoftScore score;

    public InterviewSchedule() {
    }

    public InterviewSchedule(List<InterviewSlot> timeSlotList,
                             List<ApplicantAssignment> assignmentList,
                             List<ConstraintConfig> constraintConfigs,
                             List<Interviewer> interviewerList) {
        this.timeSlotList = timeSlotList;
        this.assignmentList = assignmentList;
        this.constraintConfigs = constraintConfigs;
        this.interviewerList = interviewerList;
    }
}
