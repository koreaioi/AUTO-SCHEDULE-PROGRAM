package tave.auto_scheduling.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ApplicantAssignmentDto(
        List<ApplicantDto> dataList
) {
    public static ApplicantAssignmentDto from(List<ApplicantDto> dataList) {
        return ApplicantAssignmentDto.builder()
                .dataList(dataList)
                .build();
    }
}
