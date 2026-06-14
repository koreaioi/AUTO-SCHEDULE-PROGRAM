package tave.auto_scheduling.constraint;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;
import tave.auto_scheduling.domain.ApplicantAssignment;
import tave.auto_scheduling.domain.ConstraintConfig;
import tave.auto_scheduling.domain.Interviewer;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Objects;

public class InterviewConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                mustBeAvailableSlot(factory),
                maxPerSlot(factory),
                limitDistinctParts(factory),
                interviewerMustBeAvailable(factory),
                preferSamePartInSlot(factory),
                preferFullInterviewSlots(factory),
                bonusForSinglePartSlot(factory),
                preferPartOnDays(factory),
        };
    }

    // [Hard] 지원자가 선택한 시간에만 배정
    Constraint mustBeAvailableSlot(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .join(ConstraintConfig.class,
                        Joiners.equal(a -> "MUST_BE_AVAILABLE_SLOT", ConstraintConfig::getConstraintName))
                .filter((a, cfg) -> cfg.isEnabled()
                        && !a.getApplicant().getAvailableSlots().contains(a.getAssignedSlot().getTime()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("지원자가 선택하지 않은 시간에 배정");
    }

    // [Hard] 슬롯당 최대 인원 (threshold 동적)
    Constraint maxPerSlot(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot, ConstraintCollectors.count())
                .join(ConstraintConfig.class,
                        Joiners.equal((slot, count) -> "MAX_PER_SLOT", ConstraintConfig::getConstraintName))
                .filter((slot, count, cfg) -> cfg.isEnabled()
                        && cfg.getThreshold() != null
                        && count > cfg.getThreshold())
                .penalize(HardSoftScore.ONE_HARD, (slot, count, cfg) -> count - cfg.getThreshold())
                .asConstraint("시간대 최대 인원을 초과");
    }

    // [Hard] 슬롯당 파트 종류 최대 수 (threshold 동적)
    Constraint limitDistinctParts(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot,
                        ConstraintCollectors.toSet(a -> a.getApplicant().getPart()))
                .join(ConstraintConfig.class,
                        Joiners.equal((slot, partSet) -> "LIMIT_DISTINCT_PARTS", ConstraintConfig::getConstraintName))
                .filter((slot, partSet, cfg) -> cfg.isEnabled()
                        && cfg.getThreshold() != null
                        && partSet.size() > cfg.getThreshold())
                .penalize(HardSoftScore.ONE_HARD, (slot, partSet, cfg) -> partSet.size() - cfg.getThreshold())
                .asConstraint("한 시간대에 허용된 파트 수를 초과");
    }

    // [Soft] 면접관 가용 시간 외 배정 감점 (면접관 미등록 시 자동 비활성화)
    Constraint interviewerMustBeAvailable(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .join(ConstraintConfig.class,
                        Joiners.equal(a -> "INTERVIEWER_MUST_BE_AVAILABLE", ConstraintConfig::getConstraintName))
                .filter((a, cfg) -> cfg.isEnabled())
                .ifExists(Interviewer.class)  // 면접관이 한 명도 없으면 제약 전체 비활성화
                .ifNotExists(Interviewer.class,
                        Joiners.filtering((a, cfg, i) ->
                                i.getAvailableTimes().contains(a.getAssignedSlot().getTime())))
                .penalize(HardSoftScore.ONE_SOFT, (a, cfg) -> cfg.getWeight())
                .asConstraint("면접관 가용 시간 외 배정 불가");
    }

    // [Soft] 같은 파트일수록 보상 (weight 동적)
    Constraint preferSamePartInSlot(ConstraintFactory factory) {
        return factory.forEachUniquePair(ApplicantAssignment.class,
                        Joiners.equal(ApplicantAssignment::getAssignedSlot))
                .filter((a1, a2) -> !a1.getApplicant().getPart().equals(a2.getApplicant().getPart()))
                .join(ConstraintConfig.class,
                        Joiners.equal((a1, a2) -> "PREFER_SAME_PART", ConstraintConfig::getConstraintName))
                .filter((a1, a2, cfg) -> cfg.isEnabled())
                .penalize(HardSoftScore.ONE_SOFT, (a1, a2, cfg) -> cfg.getWeight())
                .asConstraint("같은 파트일수록 선호도 증가");
    }

    // [Soft] 슬롯 인원이 많을수록 보상 (weight 동적)
    Constraint preferFullInterviewSlots(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot, ConstraintCollectors.count())
                .join(ConstraintConfig.class,
                        Joiners.equal((slot, count) -> "PREFER_FULL_SLOTS", ConstraintConfig::getConstraintName))
                .filter((slot, count, cfg) -> cfg.isEnabled())
                .reward(HardSoftScore.ONE_SOFT, (slot, count, cfg) -> count * count * cfg.getWeight())
                .asConstraint("한 시간대에 인원이 많을수록 선호도 증가");
    }

    // [Soft] 동일 파트만 있는 슬롯 추가 보상 (weight 동적)
    Constraint bonusForSinglePartSlot(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .groupBy(ApplicantAssignment::getAssignedSlot,
                        ConstraintCollectors.toSet(a -> a.getApplicant().getPart()))
                .filter((slot, partSet) -> partSet.size() == 1)
                .join(ConstraintConfig.class,
                        Joiners.equal((slot, partSet) -> "BONUS_SINGLE_PART", ConstraintConfig::getConstraintName))
                .filter((slot, partSet, cfg) -> cfg.isEnabled())
                .reward(HardSoftScore.ONE_SOFT, (slot, partSet, cfg) -> cfg.getWeight())
                .asConstraint("한 시간대에 동일 파트만 있으면 추가 보상");
    }

    // [Soft] 파트별 선호 요일 보상 — DB의 PREFER_PART_ON_DAYS_* 설정 전체 동적 처리
    Constraint preferPartOnDays(ConstraintFactory factory) {
        return factory.forEach(ApplicantAssignment.class)
                .join(ConstraintConfig.class,
                        Joiners.filtering((a, cfg) ->
                                cfg.getConstraintName().startsWith("PREFER_PART_ON_DAYS")
                                        && cfg.isEnabled()
                                        && cfg.getTargetPart() != null
                                        && a.getApplicant().getPart().equals(cfg.getTargetPart())))
                .filter((a, cfg) -> {
                    if (cfg.getPreferredDays() == null || cfg.getPreferredDays().isBlank()) return false;
                    DayOfWeek day = a.getAssignedSlot().getTime().getDayOfWeek();
                    return Arrays.stream(cfg.getPreferredDays().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> { try { return DayOfWeek.valueOf(s); } catch (IllegalArgumentException e) { return null; } })
                            .filter(Objects::nonNull)
                            .anyMatch(d -> d == day);
                })
                .reward(HardSoftScore.ONE_SOFT, (a, cfg) -> cfg.getWeight())
                .asConstraint("파트별 선호 요일 보상");
    }
}
