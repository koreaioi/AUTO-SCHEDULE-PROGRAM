package tave.auto_scheduling.domain;

import lombok.Getter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

/*
* 전체 배정 결과를 저장하는 객체
* @ValueRangeProvider는 면접 시간 전체 범위 (ex 08-10T10:00 ~ 08-13T17:00 이런 느낌)
* @PlanningScore는 이 결과의 score를 나타냄. (최적화 정도. 높을 수록 좋음!)
* score는 HARD, SOFT로 나뉨!
 * */

@Getter
@PlanningSolution
public class InterviewSchedule {
    @ValueRangeProvider(id = "timeSlotRange")
    private List<InterviewSlot> timeSlotList;

    @PlanningEntityCollectionProperty
    private List<ApplicantAssignment> assignmentList;

    @PlanningScore
    private HardSoftScore score;

    public InterviewSchedule() {
    }

    public InterviewSchedule(List<InterviewSlot> timeSlotList, List<ApplicantAssignment> assignmentList) {
        this.timeSlotList = timeSlotList;
        this.assignmentList = assignmentList;
    }

}
