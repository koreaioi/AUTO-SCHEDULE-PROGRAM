package tave.auto_scheduling.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import tave.auto_scheduling.domain.ConstraintConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.defer-datasource-initialization=true",
        "spring.sql.init.mode=never"  // data.sql 시드 로드 비활성화 → 정확한 카운트 검증 가능
})
class ConstraintConfigRepositoryTest {

    @Autowired
    private ConstraintConfigRepository constraintConfigRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private ConstraintConfig buildConfig(String name, String type) {
        ConstraintConfig config = new ConstraintConfig();
        config.setConstraintName(name);
        config.setConstraintType(type);
        config.setEnabled(true);
        config.setWeight(1);
        return config;
    }

    // ── 테스트 ────────────────────────────────────────────────────────────────

    @Test
    void constraintConfig_저장_후_조회() {
        ConstraintConfig config = buildConfig("TEST_CONSTRAINT", "HARD");

        ConstraintConfig saved = constraintConfigRepository.save(config);

        Optional<ConstraintConfig> found = constraintConfigRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getConstraintName()).isEqualTo("TEST_CONSTRAINT");
        assertThat(found.get().getConstraintType()).isEqualTo("HARD");
    }

    @Test
    void constraintName으로_조회() {
        ConstraintConfig config = buildConfig("FIND_BY_NAME", "SOFT");
        constraintConfigRepository.save(config);

        Optional<ConstraintConfig> found = constraintConfigRepository.findByConstraintName("FIND_BY_NAME");

        assertThat(found).isPresent();
        assertThat(found.get().getConstraintType()).isEqualTo("SOFT");
    }

    @Test
    void 존재하지않는_이름으로_조회하면_empty() {
        Optional<ConstraintConfig> found = constraintConfigRepository.findByConstraintName("NO_SUCH_CONSTRAINT");

        assertThat(found).isEmpty();
    }

    @Test
    void enabled_false로_업데이트() {
        ConstraintConfig config = buildConfig("UPDATE_TARGET", "HARD");
        ConstraintConfig saved = constraintConfigRepository.save(config);

        saved.setEnabled(false);
        constraintConfigRepository.save(saved);
        // flush/clear로 1차 캐시 우회 → 실제 DB 왕복 검증
        entityManager.flush();
        entityManager.clear();

        Optional<ConstraintConfig> found = constraintConfigRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isEnabled()).isFalse();
    }

    @Test
    void weight와_threshold_저장_검증() {
        ConstraintConfig config = buildConfig("WEIGHTED_CONSTRAINT", "SOFT");
        config.setWeight(5);
        config.setThreshold(10);

        ConstraintConfig saved = constraintConfigRepository.save(config);

        Optional<ConstraintConfig> found = constraintConfigRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getWeight()).isEqualTo(5);
        assertThat(found.get().getThreshold()).isEqualTo(10);
    }

    @Test
    void constraintName_중복이면_예외() {
        // @DataJpaTest는 기본 롤백 — @Rollback 불필요
        // 픽스처명 "DUPLICATE_NAME"은 data.sql 시드와 충돌하지 않아야 함 (init.mode=never로 시드 미로드)
        ConstraintConfig first = buildConfig("DUPLICATE_NAME", "HARD");
        constraintConfigRepository.saveAndFlush(first);

        ConstraintConfig second = buildConfig("DUPLICATE_NAME", "SOFT");

        assertThatThrownBy(() -> constraintConfigRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void targetPart_preferredDays_저장_검증() {
        ConstraintConfig config = buildConfig("PART_DAY_CONSTRAINT", "SOFT");
        config.setTargetPart("백엔드");
        config.setPreferredDays("MONDAY,TUESDAY");

        ConstraintConfig saved = constraintConfigRepository.save(config);

        Optional<ConstraintConfig> found = constraintConfigRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTargetPart()).isEqualTo("백엔드");
        assertThat(found.get().getPreferredDays()).isEqualTo("MONDAY,TUESDAY");
    }

    @Test
    void 전체_목록_조회() {
        constraintConfigRepository.save(buildConfig("LIST_CONSTRAINT_1", "HARD"));
        constraintConfigRepository.save(buildConfig("LIST_CONSTRAINT_2", "SOFT"));
        constraintConfigRepository.save(buildConfig("LIST_CONSTRAINT_3", "HARD"));

        List<ConstraintConfig> all = constraintConfigRepository.findAll();

        // spring.sql.init.mode=never 로 data.sql 시드 미로드 → 정확히 3개 검증
        assertThat(all).hasSize(3);
        assertThat(all).extracting(ConstraintConfig::getConstraintName)
                .containsExactlyInAnyOrder("LIST_CONSTRAINT_1", "LIST_CONSTRAINT_2", "LIST_CONSTRAINT_3");
    }

    @Test
    void 삭제_후_조회하면_empty() {
        ConstraintConfig config = buildConfig("DELETE_TARGET", "HARD");
        ConstraintConfig saved = constraintConfigRepository.save(config);
        Long savedId = saved.getId();

        constraintConfigRepository.deleteById(savedId);

        Optional<ConstraintConfig> found = constraintConfigRepository.findById(savedId);
        assertThat(found).isEmpty();
    }
}
