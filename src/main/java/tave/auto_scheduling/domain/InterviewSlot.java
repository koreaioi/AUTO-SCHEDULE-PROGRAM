package tave.auto_scheduling.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/*
* 면접 가능 날짜 + 시간 단위를 나타냄.
* @PlanningVariable, @ValueRangeProvider에서 사용하기 위해 객체화를 거침
* */

@Getter
@Builder
public class InterviewSlot {
    private LocalDateTime time;

    public static InterviewSlot of(LocalDateTime time) {
        return InterviewSlot.builder()
                .time(time)
                .build();
    }
}
