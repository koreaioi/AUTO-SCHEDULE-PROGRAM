package tave.auto_scheduling.constraint;

import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;
import tave.auto_scheduling.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class InterviewConstraintProviderTest {

    private static final LocalDateTime MONDAY_10AM =
            LocalDateTime.of(2024, 6, 3, 10, 0);   // Monday
    private static final LocalDateTime MONDAY_2PM =
            LocalDateTime.of(2024, 6, 3, 14, 0);   // Monday
    private static final LocalDateTime WEDNESDAY_10AM =
            LocalDateTime.of(2024, 6, 5, 10, 0);   // Wednesday

    private final ConstraintVerifier<InterviewConstraintProvider, InterviewSchedule> constraintVerifier =
            ConstraintVerifier.build(
                    new InterviewConstraintProvider(),
                    InterviewSchedule.class,
                    ApplicantAssignment.class);

    // -------------------------------------------------------------------------
    // 헬퍼 메서드
    // -------------------------------------------------------------------------

    private ConstraintConfig makeConfig(String name, boolean enabled, int weight, Integer threshold) {
        ConstraintConfig cfg = new ConstraintConfig();
        cfg.setConstraintName(name);
        cfg.setEnabled(enabled);
        cfg.setWeight(weight);
        cfg.setThreshold(threshold);
        return cfg;
    }

    private ConstraintConfig makePartDaysConfig(String name, String targetPart, String preferredDays) {
        ConstraintConfig cfg = new ConstraintConfig();
        cfg.setConstraintName(name);
        cfg.setEnabled(true);
        cfg.setWeight(1);
        cfg.setTargetPart(targetPart);
        cfg.setPreferredDays(preferredDays);
        return cfg;
    }

    private Applicant makeApplicant(String part, LocalDateTime... slots) {
        return Applicant.of("홍길동", "M", "test@test.com", part, "서울대", Arrays.asList(slots));
    }

    private InterviewSlot slot(LocalDateTime time) {
        return InterviewSlot.of(time);
    }

    private ApplicantAssignment assign(Applicant applicant, InterviewSlot interviewSlot) {
        ApplicantAssignment assignment = new ApplicantAssignment(applicant);
        assignment.setAssignedSlot(interviewSlot);
        return assignment;
    }

    // -------------------------------------------------------------------------
    // mustBeAvailableSlot
    // -------------------------------------------------------------------------

    @Test
    void 지원자가_선택한_시간에_배정되면_패널티_없음() {
        Applicant applicant = makeApplicant("백엔드", MONDAY_10AM);
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makeConfig("MUST_BE_AVAILABLE_SLOT", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::mustBeAvailableSlot)
                .given(assignment, cfg)
                .penalizesBy(0);
    }

    @Test
    void 지원자가_선택하지않은_시간에_배정되면_Hard패널티() {
        Applicant applicant = makeApplicant("백엔드", MONDAY_2PM);
        InterviewSlot interviewSlot = slot(MONDAY_10AM);  // 선택하지 않은 시간
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makeConfig("MUST_BE_AVAILABLE_SLOT", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::mustBeAvailableSlot)
                .given(assignment, cfg)
                .penalizesBy(1);
    }

    @Test
    void mustBeAvailableSlot_disabled이면_패널티_없음() {
        Applicant applicant = makeApplicant("백엔드", MONDAY_2PM);
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makeConfig("MUST_BE_AVAILABLE_SLOT", false, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::mustBeAvailableSlot)
                .given(assignment, cfg)
                .penalizesBy(0);
    }

    // -------------------------------------------------------------------------
    // maxPerSlot
    // -------------------------------------------------------------------------

    @Test
    void maxPerSlot_disabled이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("프론트", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as3 = assign(makeApplicant("AI", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("MAX_PER_SLOT", false, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::maxPerSlot)
                .given(as1, as2, as3, cfg)
                .penalizesBy(0);
    }

    @Test
    void 슬롯에_4명_threshold2이면_Hard패널티_2() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("프론트", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as3 = assign(makeApplicant("AI", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as4 = assign(makeApplicant("디자인", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("MAX_PER_SLOT", true, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::maxPerSlot)
                .given(as1, as2, as3, as4, cfg)
                .penalizesBy(2);  // 4 - 2 = 2
    }

    @Test
    void 슬롯에_3명_threshold2이면_Hard패널티_1() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("프론트", MONDAY_10AM);
        Applicant a3 = makeApplicant("AI", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ApplicantAssignment as3 = assign(a3, interviewSlot);
        ConstraintConfig cfg = makeConfig("MAX_PER_SLOT", true, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::maxPerSlot)
                .given(as1, as2, as3, cfg)
                .penalizesBy(1);  // 3 - 2 = 1
    }

    @Test
    void 슬롯에_2명_threshold2이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("프론트", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ConstraintConfig cfg = makeConfig("MAX_PER_SLOT", true, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::maxPerSlot)
                .given(as1, as2, cfg)
                .penalizesBy(0);
    }

    // -------------------------------------------------------------------------
    // limitDistinctParts
    // -------------------------------------------------------------------------

    @Test
    void 슬롯에_3개파트_threshold2이면_Hard패널티_1() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("프론트", MONDAY_10AM);
        Applicant a3 = makeApplicant("AI", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ApplicantAssignment as3 = assign(a3, interviewSlot);
        ConstraintConfig cfg = makeConfig("LIMIT_DISTINCT_PARTS", true, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::limitDistinctParts)
                .given(as1, as2, as3, cfg)
                .penalizesBy(1);  // 3 - 2 = 1
    }

    @Test
    void 슬롯에_2개파트_threshold2이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("프론트", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ConstraintConfig cfg = makeConfig("LIMIT_DISTINCT_PARTS", true, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::limitDistinctParts)
                .given(as1, as2, cfg)
                .penalizesBy(0);
    }

    @Test
    void limitDistinctParts_disabled이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("프론트", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as3 = assign(makeApplicant("AI", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("LIMIT_DISTINCT_PARTS", false, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::limitDistinctParts)
                .given(as1, as2, as3, cfg)
                .penalizesBy(0);
    }

    @Test
    void 슬롯에_4개파트_threshold2이면_Hard패널티_2() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("프론트", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as3 = assign(makeApplicant("AI", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as4 = assign(makeApplicant("디자인", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("LIMIT_DISTINCT_PARTS", true, 1, 2);

        constraintVerifier.verifyThat(InterviewConstraintProvider::limitDistinctParts)
                .given(as1, as2, as3, as4, cfg)
                .penalizesBy(2);  // 4 - 2 = 2
    }

    // -------------------------------------------------------------------------
    // preferSamePartInSlot
    // -------------------------------------------------------------------------

    @Test
    void 같은슬롯에_다른파트_2명이면_Soft패널티_1() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("프론트", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ConstraintConfig cfg = makeConfig("PREFER_SAME_PART", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferSamePartInSlot)
                .given(as1, as2, cfg)
                .penalizesBy(1);
    }

    @Test
    void 같은슬롯에_같은파트_2명이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("백엔드", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ConstraintConfig cfg = makeConfig("PREFER_SAME_PART", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferSamePartInSlot)
                .given(as1, as2, cfg)
                .penalizesBy(0);
    }

    @Test
    void preferSamePartInSlot_disabled이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("프론트", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("PREFER_SAME_PART", false, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferSamePartInSlot)
                .given(as1, as2, cfg)
                .penalizesBy(0);
    }

    @Test
    void 같은슬롯에_다른파트_3명이면_Soft패널티_3() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("프론트", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as3 = assign(makeApplicant("AI", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("PREFER_SAME_PART", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferSamePartInSlot)
                .given(as1, as2, as3, cfg)
                .penalizesBy(3);  // forEachUniquePair: (as1,as2),(as1,as3),(as2,as3) = 3쌍
    }

    // -------------------------------------------------------------------------
    // preferFullInterviewSlots
    // -------------------------------------------------------------------------

    @Test
    void 슬롯에_2명_weight1이면_Soft보상_4() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("백엔드", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ConstraintConfig cfg = makeConfig("PREFER_FULL_SLOTS", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferFullInterviewSlots)
                .given(as1, as2, cfg)
                .rewardsWith(4);  // 2 * 2 * 1 = 4
    }

    @Test
    void preferFullInterviewSlots_disabled이면_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("PREFER_FULL_SLOTS", false, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferFullInterviewSlots)
                .given(as1, as2, cfg)
                .rewardsWith(0);
    }

    // -------------------------------------------------------------------------
    // bonusForSinglePartSlot
    // -------------------------------------------------------------------------

    @Test
    void 슬롯에_같은파트_2명_weight1이면_Soft보상_1() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("백엔드", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ConstraintConfig cfg = makeConfig("BONUS_SINGLE_PART", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::bonusForSinglePartSlot)
                .given(as1, as2, cfg)
                .rewardsWith(1);
    }

    @Test
    void bonusForSinglePartSlot_disabled이면_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment as1 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ApplicantAssignment as2 = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("BONUS_SINGLE_PART", false, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::bonusForSinglePartSlot)
                .given(as1, as2, cfg)
                .rewardsWith(0);
    }

    @Test
    void 슬롯에_다른파트_있으면_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant a1 = makeApplicant("백엔드", MONDAY_10AM);
        Applicant a2 = makeApplicant("프론트", MONDAY_10AM);
        ApplicantAssignment as1 = assign(a1, interviewSlot);
        ApplicantAssignment as2 = assign(a2, interviewSlot);
        ConstraintConfig cfg = makeConfig("BONUS_SINGLE_PART", true, 1, null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::bonusForSinglePartSlot)
                .given(as1, as2, cfg)
                .rewardsWith(0);
    }

    // -------------------------------------------------------------------------
    // preferPartOnDays
    // -------------------------------------------------------------------------

    @Test
    void 딥러닝파트_MONDAY_선호요일MONDAY_TUESDAY이면_Soft보상_1() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);  // Monday
        Applicant applicant = makeApplicant("딥러닝", MONDAY_10AM);
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", "MONDAY,TUESDAY");

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(1);
    }

    @Test
    void 딥러닝파트_WEDNESDAY_선호요일MONDAY_TUESDAY이면_보상_없음() {
        InterviewSlot interviewSlot = slot(WEDNESDAY_10AM);  // Wednesday
        Applicant applicant = makeApplicant("딥러닝", WEDNESDAY_10AM);
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", "MONDAY,TUESDAY");

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(0);
    }

    @Test
    void 다른파트_MONDAY_선호요일MONDAY이면_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);  // Monday
        Applicant applicant = makeApplicant("백엔드", MONDAY_10AM);  // targetPart 불일치
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", "MONDAY,TUESDAY");

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(0);
    }

    @Test
    void preferPartOnDays_disabled이면_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(makeApplicant("딥러닝", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", "MONDAY,TUESDAY");
        cfg.setEnabled(false);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(0);
    }

    @Test
    void preferredDays가_null이면_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(makeApplicant("딥러닝", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", null);

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(0);
    }

    @Test
    void preferredDays가_빈문자열이면_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(makeApplicant("딥러닝", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", "");

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(0);
    }

    @Test
    void preferredDays에_공백포함_MONDAY_TUESDAY이면_정상보상() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(makeApplicant("딥러닝", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", "MONDAY, TUESDAY");

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(1);  // trim() 처리로 정상 매칭
    }

    @Test
    void 잘못된_요일_문자열은_무시되고_보상_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(makeApplicant("딥러닝", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makePartDaysConfig("PREFER_PART_ON_DAYS_딥러닝", "딥러닝", "MONDAYY,TUESDAY");

        constraintVerifier.verifyThat(InterviewConstraintProvider::preferPartOnDays)
                .given(assignment, cfg)
                .rewardsWith(0);  // MONDAYY 무시, TUESDAY도 월요일 아님 → 0
    }

    // -------------------------------------------------------------------------
    // interviewerMustBeAvailable
    // -------------------------------------------------------------------------

    @Test
    void 면접관_0명이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant applicant = makeApplicant("백엔드", MONDAY_10AM);
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makeConfig("INTERVIEWER_MUST_BE_AVAILABLE", true, 1, null);

        // 면접관 없음 → ifExists(Interviewer.class) 조건 false → 제약 비활성화
        constraintVerifier.verifyThat(InterviewConstraintProvider::interviewerMustBeAvailable)
                .given(assignment, cfg)
                .penalizesBy(0);
    }

    @Test
    void 면접관_가용시간에_배정되면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant applicant = makeApplicant("백엔드", MONDAY_10AM);
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makeConfig("INTERVIEWER_MUST_BE_AVAILABLE", true, 1, null);

        Interviewer interviewer = new Interviewer();
        interviewer.setName("김면접관");
        interviewer.setAvailableTimes(List.of(MONDAY_10AM));

        constraintVerifier.verifyThat(InterviewConstraintProvider::interviewerMustBeAvailable)
                .given(assignment, cfg, interviewer)
                .penalizesBy(0);
    }

    @Test
    void 면접관_가용시간_외_배정이면_Soft패널티_1() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        Applicant applicant = makeApplicant("백엔드", MONDAY_10AM);
        ApplicantAssignment assignment = assign(applicant, interviewSlot);
        ConstraintConfig cfg = makeConfig("INTERVIEWER_MUST_BE_AVAILABLE", true, 1, null);

        Interviewer interviewer = new Interviewer();
        interviewer.setName("김면접관");
        interviewer.setAvailableTimes(List.of(MONDAY_2PM));  // 가용 시간이 다름

        constraintVerifier.verifyThat(InterviewConstraintProvider::interviewerMustBeAvailable)
                .given(assignment, cfg, interviewer)
                .penalizesBy(1);
    }

    @Test
    void interviewerMustBeAvailable_disabled이면_패널티_없음() {
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("INTERVIEWER_MUST_BE_AVAILABLE", false, 1, null);

        Interviewer interviewer = new Interviewer();
        interviewer.setName("김면접관");
        interviewer.setAvailableTimes(List.of(MONDAY_2PM));

        constraintVerifier.verifyThat(InterviewConstraintProvider::interviewerMustBeAvailable)
                .given(assignment, cfg, interviewer)
                .penalizesBy(0);
    }

    @Test
    void 면접관_여러명중_하나라도_가용하면_패널티_없음() {
        // A 면접관은 MONDAY_10AM 불가, B 면접관은 MONDAY_10AM 가용
        // ifNotExists: "가용한 면접관이 한 명도 없을 때" 패널티 → 하나라도 있으면 0
        InterviewSlot interviewSlot = slot(MONDAY_10AM);
        ApplicantAssignment assignment = assign(makeApplicant("백엔드", MONDAY_10AM), interviewSlot);
        ConstraintConfig cfg = makeConfig("INTERVIEWER_MUST_BE_AVAILABLE", true, 1, null);

        Interviewer interviewerA = new Interviewer();
        interviewerA.setName("김면접관");
        interviewerA.setAvailableTimes(List.of(MONDAY_2PM));   // 불가

        Interviewer interviewerB = new Interviewer();
        interviewerB.setName("이면접관");
        interviewerB.setAvailableTimes(List.of(MONDAY_10AM));  // 가용

        constraintVerifier.verifyThat(InterviewConstraintProvider::interviewerMustBeAvailable)
                .given(assignment, cfg, interviewerA, interviewerB)
                .penalizesBy(0);
    }
}
