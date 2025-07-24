package tave.auto_scheduling.domain;

import lombok.Getter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.LocalDateTime;
import java.util.UUID;

/*
* OptaPlanner가 최적화하는 핵심 대상
* 하나의 지원자(Applicant)를 어떤 시간(InterviewSlot)에 배정할 지 결정하는 단위임.
* @PlanningVariable를 통해서 OptaPlanner가 자동으로 최적의 InterviewSlot을 넣어줌.
* */

@Getter
@PlanningEntity
public class ApplicantAssignment {

    @PlanningId
    private String id;

    private Applicant applicant;

    @PlanningVariable(valueRangeProviderRefs = {"timeSlotRange"})
    private InterviewSlot assignedSlot;

    public ApplicantAssignment() {

    }

    public ApplicantAssignment(Applicant applicant) {
        this.id = UUID.randomUUID().toString();
        this.applicant = applicant;
    }

    // TODO 추가 개선 필요. 검증이 아닌 경우 대비
    // Optaplanner가 배정한 시간이 지원자가 희망하는 면접시간대인지 검증함.
    public void validAssignedTimeSlot() {
        if (assignedSlot == null) {
            System.out.println("==== Assigned slot is null ====");
            return;
        }

        LocalDateTime assignedTime = assignedSlot.getTime();
        boolean isValid = applicant.getAvailableSlots().contains(assignedTime);

        if (!isValid) {
            System.out.println(" Applicant Name " + applicant.getName() + " has incorrect time slot");
        }
    }

}
