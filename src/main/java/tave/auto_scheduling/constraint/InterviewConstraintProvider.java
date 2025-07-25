package tave.auto_scheduling.constraint;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;
import tave.auto_scheduling.domain.ApplicantAssignment;

import java.time.LocalDateTime;
import java.util.List;

public class InterviewConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                mustBeAvailableSlot(factory),
                maxThreePerSlot(factory),
                limitDistinctParts(factory),
                preferSamePartInSlot(factory),
                preferFullInterviewSlots(factory),
                bonusForSinglePartSlot(factory)
        };
    }

    // [Hard] 제약 1 - 지원자가 지원한 시간에만 배정되어야 함
    private Constraint mustBeAvailableSlot(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .filter(assignment -> {
                    LocalDateTime assignedTime = assignment.getAssignedSlot().getTime();
                    List<LocalDateTime> available = assignment.getApplicant().getAvailableSlots();
                    return !available.contains(assignedTime); // 가능한 시간에 포함되지 않으면 하드 패널티
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("지원자가 선택하지 않은 시간에 배정");
    }

    // [Hard] 제약 2 - 한 시간에 3명 이하
    private Constraint maxThreePerSlot(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot, ConstraintCollectors.count())
                .filter((slot, count) -> count > 3)
                .penalize(HardSoftScore.ONE_HARD, (slot, count) -> count - 3)
                .asConstraint("시간대 최대 인원(3명)을 초과");
    }

    // [Hard] 제약 3 - 한 면접시간에 파트 종류 3개 이상 초과하면 X
    private Constraint limitDistinctParts(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot,
                        ConstraintCollectors.toSet(a -> a.getApplicant().getPart()))
                .filter((slot, partSet) -> partSet.size() > 3)
                .penalize(HardSoftScore.ONE_HARD, (slot, partSet) -> partSet.size() - 3)
                .asConstraint("한 시간대에 허용된 파트 수(3개)를 초과");
    }


    // [Soft] 제약 1 - 같은 파트일수록 보상
    private Constraint preferSamePartInSlot(ConstraintFactory factory) {
        return factory.forEachUniquePair(ApplicantAssignment.class,
                        Joiners.equal(ApplicantAssignment::getAssignedSlot))
                .filter((a1, a2) -> !a1.getApplicant().getPart().equals(a2.getApplicant().getPart()))
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("같은 파트일수록 선호도 증가");
    }

    // [Soft] 제약 2- 한 면접 시간에 사람이 많을 수록 더 높은 선호도 (높은 점수를 부여함.) 단 하드 때문에 3명은 안넘음!
    private Constraint preferFullInterviewSlots(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot, ConstraintCollectors.count())
                .reward(HardSoftScore.ofSoft(2), (slot, count) -> count * count)
                .asConstraint("한 시간대에 인원이 많을수록 선호도 증가");
    }

    // [Soft] 제약 3 - 같은 쌍만 있을 경우 가중치
    private Constraint bonusForSinglePartSlot(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot,
                        ConstraintCollectors.toSet(a -> a.getApplicant().getPart()))
                .filter((slot, partSet) -> partSet.size() == 1)
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("한 시간대에 동일 파트만 있으면 추가 보상");
    }

}
