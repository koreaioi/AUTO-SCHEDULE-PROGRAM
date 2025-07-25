package tave.auto_scheduling.config;

import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.context.annotation.Configuration;

/*
 * ONLY_HARD - Hard 조건만 충족하면 끝
 * OPTIMIZATION - 10초간 더 나은 해를 탐색, 최대 60초
 * HIGH_QUALITY - 20초간 더 나은 해를 탐색, 최대 60초, Hard 제약 보장,
 * */

@Configuration
public class TerminationPolicyConfig {

    public TerminationConfig provide(TerminateType type) {
        return switch (type) {
            case ONLY_HARD -> new TerminationConfig()
                    .withBestScoreLimit("0hard/0soft")
                    .withSecondsSpentLimit(60L)
                    .withUnimprovedSecondsSpentLimit(10L);
            case OPTIMIZATION -> new TerminationConfig()
                    .withSecondsSpentLimit(60L)
                    .withUnimprovedSecondsSpentLimit(10L);
            case HIGH_QUALITY -> new TerminationConfig()
                    .withBestScoreLimit("0hard/9999soft")
                    .withSecondsSpentLimit(60L)
                    .withUnimprovedSecondsSpentLimit(20L);
        };
    }

}
