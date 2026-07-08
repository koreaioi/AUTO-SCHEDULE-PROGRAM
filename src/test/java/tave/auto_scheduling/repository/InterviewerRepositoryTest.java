package tave.auto_scheduling.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import tave.auto_scheduling.domain.Interviewer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.defer-datasource-initialization=true")
class InterviewerRepositoryTest {

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private Interviewer buildInterviewer(String name, String part) {
        Interviewer interviewer = new Interviewer();
        interviewer.setName(name);
        interviewer.setPart(part);
        return interviewer;
    }

    private LocalDateTime dt(int year, int month, int day, int hour, int minute) {
        // withNano(0): H2 TIMESTAMP 왕복 시 나노초 손실 방지
        return LocalDateTime.of(year, month, day, hour, minute).withNano(0);
    }

    // ── 테스트 ────────────────────────────────────────────────────────────────

    @Test
    void 면접관_저장_후_조회() {
        Interviewer interviewer = buildInterviewer("홍길동", "백엔드");

        Interviewer saved = interviewerRepository.save(interviewer);

        Optional<Interviewer> found = interviewerRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("홍길동");
        assertThat(found.get().getPart()).isEqualTo("백엔드");
    }

    @Test
    void 가용시간_포함_저장_검증() {
        Interviewer interviewer = buildInterviewer("김면접", "프론트엔드");
        LocalDateTime time1 = dt(2025, 7, 1, 10, 0);
        LocalDateTime time2 = dt(2025, 7, 2, 14, 0);
        interviewer.getAvailableTimes().add(time1);
        interviewer.getAvailableTimes().add(time2);

        Interviewer saved = interviewerRepository.save(interviewer);
        // flush & clear 를 통해 1차 캐시에서 꺼내지 않고 DB에서 재조회
        entityManager.flush();
        entityManager.clear();

        Optional<Interviewer> found = interviewerRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAvailableTimes())
                .hasSize(2)
                .containsExactlyInAnyOrder(time1, time2);
    }

    @Test
    void 가용시간_없는_면접관_저장() {
        Interviewer interviewer = buildInterviewer("이무시간", "디자인");
        // availableTimes 기본값 = 빈 ArrayList

        Interviewer saved = interviewerRepository.save(interviewer);

        Optional<Interviewer> found = interviewerRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAvailableTimes()).isEmpty();
    }

    @Test
    void 면접관_이름_수정() {
        Interviewer interviewer = buildInterviewer("구이름", "백엔드");
        Interviewer saved = interviewerRepository.save(interviewer);

        saved.setName("새이름");
        interviewerRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        Optional<Interviewer> found = interviewerRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("새이름");
    }

    @Test
    void 면접관_삭제시_가용시간도_삭제() {
        Interviewer interviewer = buildInterviewer("삭제대상", "딥러닝");
        interviewer.getAvailableTimes().add(dt(2025, 8, 1, 9, 0));
        interviewer.getAvailableTimes().add(dt(2025, 8, 2, 11, 0));
        Interviewer saved = interviewerRepository.save(interviewer);
        Long savedId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        // 삭제 전: availability 행 2개 존재 확인
        long preCount = ((Number) entityManager.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM interviewer_availability WHERE interviewer_id = :id")
                .setParameter("id", savedId)
                .getSingleResult()).longValue();
        assertThat(preCount).isEqualTo(2);

        // 삭제 실행
        interviewerRepository.deleteById(savedId);
        entityManager.flush();

        // 삭제 후: Interviewer도 사라지고, availability 행도 cascade 삭제됨
        assertThat(interviewerRepository.findById(savedId)).isEmpty();
        long postCount = ((Number) entityManager.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM interviewer_availability WHERE interviewer_id = :id")
                .setParameter("id", savedId)
                .getSingleResult()).longValue();
        assertThat(postCount).isZero();
    }

    @Test
    void 파트별_면접관_복수_저장_조회() {
        interviewerRepository.save(buildInterviewer("면접관A", "백엔드"));
        interviewerRepository.save(buildInterviewer("면접관B", "프론트엔드"));
        interviewerRepository.save(buildInterviewer("면접관C", "딥러닝"));

        List<Interviewer> all = interviewerRepository.findAll();

        // data.sql 시드 데이터가 없어도 저장한 항목은 반드시 포함
        assertThat(all).hasSizeGreaterThanOrEqualTo(3);
        assertThat(all).extracting(Interviewer::getPart)
                .contains("백엔드", "프론트엔드", "딥러닝");
    }

    @Test
    void 가용시간_중복_허용_검증() {
        Interviewer interviewer = buildInterviewer("중복시간면접관", "데이터분석");
        LocalDateTime sameTime = dt(2025, 9, 1, 10, 0);
        interviewer.getAvailableTimes().add(sameTime);
        interviewer.getAvailableTimes().add(sameTime); // 동일 시간 두 번 추가

        Interviewer saved = interviewerRepository.save(interviewer);
        entityManager.flush();
        entityManager.clear();

        Optional<Interviewer> found = interviewerRepository.findById(saved.getId());
        assertThat(found).isPresent();
        // @ElementCollection은 중복 값을 모두 저장한다
        assertThat(found.get().getAvailableTimes()).hasSize(2);
        assertThat(found.get().getAvailableTimes()).containsOnly(sameTime);
    }
}
