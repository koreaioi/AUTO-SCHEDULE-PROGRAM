package tave.auto_scheduling.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/*
* 면접 가능 시간 Excel에서 추출하는 지원자 기본 정보
* 이름, 성별, 이메일, 지원 파트, 대학명, 면접 가능 시간
* */

@Getter
@Builder
public class Applicant {
    private String name;
    private String sex;
    private String email;
    private String part;
    private String univ;
    private List<LocalDateTime> availableSlots;

    public Applicant of(String name, String sex, String email, String part, String univ, List<LocalDateTime> availableSlots) {
        return Applicant.builder()
                .name(name)
                .sex(sex)
                .email(email)
                .part(part)
                .univ(univ)
                .availableSlots(availableSlots)
                .build();
    }

}
