package tave.auto_scheduling.dto.response;

import lombok.Builder;
import tave.auto_scheduling.domain.Applicant;
import tave.auto_scheduling.domain.ApplicantAssignment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record ApplicantDto(
        String name,
        String sex,
        String email,
        String part,
        String univ,
        String interviewDate,
        String interviewTime
) {
    public static ApplicantDto from(ApplicantAssignment applicantAssignment) {

        Applicant applicant = applicantAssignment.getApplicant();
        LocalDateTime dateTime = applicantAssignment.getAssignedSlot().getTime();

        String interviewDate = dateTime.format(DateTimeFormatter.ofPattern("M월 d일"));
        String interviewTime = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        return ApplicantDto.builder()
                .name(applicant.getName())
                .sex(applicant.getSex())
                .email(applicant.getEmail())
                .part(applicant.getPart())
                .univ(applicant.getUniv())
                .interviewDate(interviewDate)
                .interviewTime(interviewTime)
                .build();
    }
}
